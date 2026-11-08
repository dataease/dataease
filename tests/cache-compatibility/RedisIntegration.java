package io.dataease.cache.impl;

import io.dataease.utils.CacheUtils;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Only connects to the runner-owned, loopback Redis after verifying its process ID. */
public class RedisIntegration {
    private static int checks;

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }

    private static void inject(Object target, Class<?> type, String field, Object value) throws Exception {
        var declared = type.getDeclaredField(field);
        declared.setAccessible(true);
        declared.set(target, value);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        var configuration = new RedisStandaloneConfiguration("127.0.0.1", Integer.parseInt(args[0]));
        var factory = new LettuceConnectionFactory(configuration);
        try {
            factory.afterPropertiesSet();
            factory.start();
            try (var connection = factory.getConnection()) {
                var info = connection.serverCommands().info("server");
                check(args[1].equals(info.getProperty("process_id")), "Redis process belongs to this runner");
                check(info.getProperty("redis_version").startsWith("7.2."), "tested Redis version recorded");
            }
            var template = new RedisTemplate<String, Object>();
            template.setConnectionFactory(factory);
            template.afterPropertiesSet();
            var manager = RedisCacheManager.create(factory);
            manager.afterPropertiesSet();
            var service = new RedisCacheImpl();
            inject(service, RedisCacheImpl.class, "redisTemplate", template);
            inject(null, RedisCacheImpl.class, "cacheManager", manager);
            inject(null, CacheUtils.class, "deCacheService", service);
            String name = "de_v3_isolated_contract";
            service.put(name, "short", "short", 350L, TimeUnit.MILLISECONDS);
            service.put(name, "long", "long", 5L, TimeUnit.SECONDS);
            check(template.getExpire(name + "::short", TimeUnit.MILLISECONDS) > 0, "short entry has TTL");
            check(template.getExpire(name + "::long", TimeUnit.MILLISECONDS) > 3500, "same cache permits longer TTL");
            Thread.sleep(500);
            check(service.get(name, "short") == null && !service.keyExist(name, "short"), "real server expires short entry");
            check("long".equals(service.get(name, "long")), "long entry retained");
            service.put(name, "renew", "old", 350L, TimeUnit.MILLISECONDS);
            service.put(name, "renew", "new", 5L, TimeUnit.SECONDS);
            Thread.sleep(500);
            check("new".equals(service.get(name, "renew")), "overwrite extends expiry");
            service.put(name, "renew", "shortened", 250L, TimeUnit.MILLISECONDS);
            Thread.sleep(400);
            check(service.get(name, "renew") == null, "overwrite shortens expiry");
            service.put(name, "read", "value", 1000L, TimeUnit.MILLISECONDS);
            long before = template.getExpire(name + "::read", TimeUnit.MILLISECONDS);
            Thread.sleep(150);
            check(service.get(name, "read") != null, "read before deadline");
            long after = template.getExpire(name + "::read", TimeUnit.MILLISECONDS);
            check(after < before - 80, "read does not reset server TTL");
            service.put(name, "persistent", 1, 350L, TimeUnit.MILLISECONDS);
            service.put(name, "persistent", 1, 0L, null);
            check(template.getExpire(name + "::persistent", TimeUnit.MILLISECONDS) == -1, "zero removes previous expiry as before");
            service.put(name, "negative", 1, -1L, null);
            check(template.getExpire(name + "::negative", TimeUnit.MILLISECONDS) == -1, "negative remains permanent");
            CacheUtils.put(name, "default", 1);
            long defaultTtl = template.getExpire(name + "::default", TimeUnit.SECONDS);
            check(defaultTtl > 28790 && defaultTtl <= 28800, "default remains eight hours on real server");
            service.put(name, "seconds", 1, 5L, null);
            check(template.getExpire(name + "::seconds", TimeUnit.MILLISECONDS) > 3500, "null unit remains seconds");
            var original = new ArrayList<>(List.of(new ArrayList<>(List.of("before"))));
            service.put(name, "nested", original, 5L, TimeUnit.SECONDS);
            original.getFirst().add("writer-change");
            var first = (List<List<String>>) service.get(name, "nested");
            check(first.getFirst().size() == 1, "put serializes nested writer state");
            first.getFirst().add("reader-change");
            check(((List<List<String>>) service.get(name, "nested")).getFirst().size() == 1, "get returns independent nested state");
            manager.getCache(name).put("dual", "spring-value");
            service.put(name, "dual", "template-value", 5L, TimeUnit.SECONDS);
            check("spring-value".equals(manager.getCache(name).get("dual").get()), "existing Spring namespace works");
            service.keyRemove(name, "dual");
            check(service.get(name, "dual") == null && manager.getCache(name).get("dual") == null, "deletion clears both existing key formats");
            service.put(name, "once", "single-use", 5L, TimeUnit.SECONDS);
            var pool = Executors.newFixedThreadPool(8);
            try {
                var tasks = new ArrayList<Callable<Object>>();
                for (int i = 0; i < 24; i++) tasks.add(() -> service.take(name, "once"));
                int winners = 0;
                for (var future : pool.invokeAll(tasks)) {
                    Object value = future.get();
                    if (value != null) {
                        check("single-use".equals(value), "Lua result deserializes with original template");
                        winners++;
                    }
                }
                check(winners == 1, "Lua take has exactly one winner under concurrency");
            } finally {
                pool.shutdownNow();
            }
            check(service.take(name, "missing") == null, "missing take returns null");
            check(service.take(name, "short") == null, "expired take returns null");
            service.put(name, "uid1:oid1", "first-user", 5L, TimeUnit.SECONDS);
            service.put(name, "uid2:oid1", "second-user", 5L, TimeUnit.SECONDS);
            service.keyRemove(name, "uid1:oid1");
            check("second-user".equals(service.get(name, "uid2:oid1")), "one identity invalidation preserves another key");
            // The actual logout/login business callers use keyExist on these configured durations.
            service.put("de_v3_user_token_cache", "synthetic-user:login", 1, 350L, TimeUnit.MILLISECONDS);
            service.put("de_v3_embedded_token_cache", "synthetic-revoked", 1, 5L, TimeUnit.SECONDS);
            Thread.sleep(500);
            check(!service.keyExist("de_v3_user_token_cache", "synthetic-user:login"), "expired login membership is rejected");
            check(service.keyExist("de_v3_embedded_token_cache", "synthetic-revoked"), "logout membership remains for its supplied lifetime");
        } finally {
            factory.destroy();
        }
        System.out.println("PASS real Redis integration: " + checks + " checks");
    }
}
