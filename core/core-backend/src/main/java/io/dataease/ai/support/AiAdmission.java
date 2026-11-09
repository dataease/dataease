package io.dataease.ai.support;

import io.dataease.ai.dao.entity.CoreAiSetting;
import io.dataease.ai.dao.repository.CoreAiSettingRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.*;

/** 独立 I/B 池。Redis 模式故障关闭，绝不退回 JVM 计数。 */
@Component
public class AiAdmission {
    public record State(long revision, String name) { }
    public interface Permit extends AutoCloseable { @Override void close(); }
    private final boolean cluster;
    private final StringRedisTemplate redis;
    private final CoreAiSettingRepository settings;
    private final Map<String, Long> interactive = new HashMap<>(), background = new HashMap<>();
    private String updating;
    private long appliedRevision = -1;
    private final List<String> keys;
    private final org.springframework.transaction.support.TransactionTemplate transactions;
    @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager em;
    private static final String SCRIPT = """
            local t=redis.call('TIME'); local now=t[1]*1000+math.floor(t[2]/1000)
            local op=ARGV[1]; local rev=tonumber(ARGV[2]); local p=KEYS[1]
            if redis.call('EXISTS',p)==0 then
              redis.call('HSET',p,'state','RECOVERING','ready',now+310000,'revision',rev,'epoch',ARGV[#ARGV])
            end
            local state=redis.call('HGET',p,'state'); local epoch=redis.call('HGET',p,'epoch')
            local intact=redis.call('ZSCORE',KEYS[2],'__sentinel:'..epoch) and redis.call('ZSCORE',KEYS[3],'__sentinel:'..epoch)
            if not intact and state~='RECOVERING' then
              redis.call('HSET',p,'state','RECOVERING','ready',now+310000); state='RECOVERING'
            end
            if op=='state' then return state..':'..redis.call('HGET',p,'revision') end
            if op=='release' then
              if ARGV[5]~=epoch then return 'STALE' end
              redis.call('ZREM',KEYS[tonumber(ARGV[3])],ARGV[4]); return 'OK'
            end
            if op=='recover' then
              if state=='ACTIVE' and tonumber(redis.call('HGET',p,'revision'))~=rev then
                redis.call('HSET',p,'state','RECOVERING','ready',now+310000); return 'WAIT'
              end
              if (state=='RECOVERING' or state=='UPDATING') and now>=tonumber(redis.call('HGET',p,'ready')) then return 'NEEDS_DB' end
              return 'WAIT'
            end
            if op=='recover_locked' then
              if (state~='RECOVERING' and state~='UPDATING') or now<tonumber(redis.call('HGET',p,'ready')) then return 'WAIT' end
              if state=='RECOVERING' then
                epoch=ARGV[#ARGV]; redis.call('DEL',KEYS[2],KEYS[3])
                redis.call('ZADD',KEYS[2],'+inf','__sentinel:'..epoch); redis.call('ZADD',KEYS[3],'+inf','__sentinel:'..epoch)
              end
              redis.call('HSET',p,'state','ACTIVE','revision',rev,'epoch',epoch); redis.call('HDEL',p,'owner'); return 'OK'
            end
            if op=='assert_owner' then
              if state~='UPDATING' or redis.call('HGET',p,'owner')~=ARGV[3] then return 'BLOCKED' end
              return 'OK'
            end
            if op=='finish' then
              if state~='UPDATING' or redis.call('HGET',p,'owner')~=ARGV[3] then return 'BLOCKED' end
              redis.call('HSET',p,'state','ACTIVE','revision',rev); redis.call('HDEL',p,'owner'); return 'OK'
            end
            if state~='ACTIVE' or tonumber(redis.call('HGET',p,'revision'))~=rev then return 'BLOCKED' end
            if op=='begin' then
              redis.call('HSET',p,'state','UPDATING','owner',ARGV[3],'ready',now+60000); return 'OK'
            end
            local pool=KEYS[tonumber(ARGV[3])]; redis.call('ZREMRANGEBYSCORE',pool,'-inf',now)
            if op=='acquire' then
              if tonumber(ARGV[5])<=0 or tonumber(ARGV[5])>300000 then return 'TIMEOUT' end
              if redis.call('ZSCORE',pool,ARGV[4]) then return 'BLOCKED' end
              if redis.call('ZCARD',pool)-1>=tonumber(ARGV[6]) then return 'LIMIT' end
              redis.call('ZADD',pool,now+tonumber(ARGV[5])+10000,ARGV[4]); return 'OK:'..epoch
            end
            return 'BLOCKED'
            """;
    public AiAdmission(@Value("${spring.cache.type:ehcache}") String cache,
                       @Value("${spring.profiles.active:standalone}") String profiles,
                       ObjectProvider<RedisConnectionFactory> factories, CoreAiSettingRepository settings,
                       @Value("${dataease.ai.namespace:}") String namespace,
                       org.springframework.transaction.PlatformTransactionManager manager) {
        cluster = "redis".equalsIgnoreCase(cache) || Arrays.asList(profiles.split(",")).contains("distributed");
        RedisConnectionFactory factory = factories.getIfAvailable();
        redis = factory == null ? null : new StringRedisTemplate(factory);
        this.settings = settings;
        String prefix = namespace.matches("[A-Za-z0-9_-]{1,64}") ? "de:ai:{" + namespace + ":admission}:" : null;
        keys = prefix == null ? List.of() : List.of(prefix + "policy", prefix + "I", prefix + "B");
        transactions = new org.springframework.transaction.support.TransactionTemplate(manager);
        transactions.setTimeout(30);
    }
    private String run(String operation, CoreAiSetting s, String... rest) {
        if (redis == null || keys.isEmpty()) throw AiErrors.error("policy_blocked");
        List<String> args = new ArrayList<>(List.of(operation, s.getRevision().toString())); args.addAll(List.of(rest)); args.add(UUID.randomUUID().toString());
        try { return redis.execute(new DefaultRedisScript<>(SCRIPT, String.class), keys, args.toArray()); }
        catch (Exception e) { throw AiErrors.error("policy_blocked"); }
    }
    public State state(CoreAiSetting s) {
        if (!cluster) { synchronized (this) { if (appliedRevision < 0) appliedRevision = s.getRevision(); return new State(appliedRevision, updating == null ? "ACTIVE" : "UPDATING"); } }
        try { String[] parts = run("state", s).split(":"); long applied = Long.parseLong(parts[1]); return new State(applied, "ACTIVE".equals(parts[0]) && applied != s.getRevision() ? "BLOCKED" : parts[0]); }
        catch (RuntimeException e) { return new State(0, "BLOCKED"); }
    }
    public String beginUpdate(CoreAiSetting s) {
        String owner = UUID.randomUUID().toString();
        if (cluster) { if (!"OK".equals(run("begin", s, owner))) throw AiErrors.error("policy_blocked"); }
        else synchronized (this) { if (updating != null) throw AiErrors.error("conflict"); updating = owner; }
        return owner;
    }
    /** 必须在配置事务 CAS 已实际执行并持有配置行锁后调用。 */
    public void assertUpdateOwner(String owner, CoreAiSetting s) {
        if (cluster) { if (!"OK".equals(run("assert_owner", s, owner))) throw AiErrors.error("policy_blocked"); }
        else synchronized (this) { if (!Objects.equals(owner, updating)) throw AiErrors.error("conflict"); }
    }
    public void finishUpdate(String owner, CoreAiSetting current, boolean committed) {
        if (cluster) { if (!"OK".equals(run("finish", current, owner))) throw AiErrors.error("policy_blocked"); }
        else synchronized (this) { if (Objects.equals(owner, updating)) { appliedRevision = current.getRevision(); updating = null; } }
    }
    public Permit acquire(CoreAiSetting s, boolean task, long deadline) {
        if (deadline <= System.currentTimeMillis()) throw AiErrors.error("timeout");
        String owner = UUID.randomUUID().toString();
        int limit = task ? s.getBackgroundConcurrency() : s.getInteractiveConcurrency();
        if (cluster) {
            String result = run("acquire", s, task ? "3" : "2", owner, "" + Math.min(300000, deadline - System.currentTimeMillis()), "" + limit);
            if (result == null || !result.startsWith("OK:")) throw AiErrors.error("LIMIT".equals(result) ? "concurrency" : "policy_blocked");
            return () -> { try { run("release", s, task ? "3" : "2", owner, result.substring(3)); } catch (RuntimeException ignored) { /* 有界租期兜底；新请求仍失败关闭。 */ } };
        }
        synchronized (this) {
            if (appliedRevision < 0) appliedRevision = s.getRevision();
            if (updating != null || appliedRevision != s.getRevision()) throw AiErrors.error("policy_blocked");
            Map<String, Long> pool = task ? background : interactive;
            long now = System.currentTimeMillis(); pool.values().removeIf(expiry -> expiry <= now);
            if (pool.size() >= limit) throw AiErrors.error("concurrency");
            pool.put(owner, deadline + 10000);
            return () -> { synchronized (AiAdmission.this) { pool.remove(owner); } };
        }
    }
    @Scheduled(fixedDelay = 10000)
    public void recover() {
        if (!cluster) return;
        settings.findById(1L).ifPresent(s -> {
            try {
                if (!"NEEDS_DB".equals(run("recover", s))) return;
                transactions.executeWithoutResult(tx -> {
                    CoreAiSetting before = settings.findById(1L).orElseThrow();
                    int changed = em.createQuery("update CoreAiSetting s set s.rowVersion = s.rowVersion + 1 where s.id = 1 and s.rowVersion = :v")
                            .setParameter("v", before.getRowVersion()).executeUpdate();
                    if (changed != 1) throw AiErrors.error("conflict");
                    em.refresh(before);
                    run("recover_locked", before);
                });
            } catch (RuntimeException ignored) { /* 不解除屏障；设置接口持续报告运行状态。 */ }
        });
    }
}
