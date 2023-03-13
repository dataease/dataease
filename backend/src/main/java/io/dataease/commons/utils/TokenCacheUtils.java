package io.dataease.commons.utils;

import io.dataease.commons.model.OnlineUserModel;
import io.dataease.listener.util.CacheUtils;
import io.dataease.service.system.SystemParameterService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;


@Component
public class TokenCacheUtils {


    private static final String KEY = "sys_token_store";

    private static final String ONLINE_TOKEN_POOL_KEY = "online_token_store";

    private static String cacheType;

    private static Long expTime;

    @Value("${spring.cache.type:ehcache}")
    public void setCacheType(String cacheType) {
        TokenCacheUtils.cacheType = cacheType;
    }

    @Value("${dataease.login_timeout:480}")
    public void setExpTime(Long expTime) {
        TokenCacheUtils.expTime = expTime;
    }

    private static boolean useRedis() {
        return StringUtils.equals(cacheType, "redis");
    }


    private static ValueOperations cacheHandler() {
        RedisTemplate redisTemplate = (RedisTemplate) CommonBeanFactory.getBean("redisTemplate");
        ValueOperations valueOperations = redisTemplate.opsForValue();
        return valueOperations;
    }

    public static void add(String token, Long userId) {
        if (useRedis()) {
            ValueOperations valueOperations = cacheHandler();
            valueOperations.set(KEY + token, userId, expTime, TimeUnit.MINUTES);
            return;
        }

        Long time = expTime * 60;
        Double v = time * 0.6;
        CacheUtils.put(KEY, token, userId, time.intValue(), v.intValue());
        CacheUtils.flush(KEY);
    }

    public static void remove(String token) {
        if (useRedis()) {
            RedisTemplate redisTemplate = (RedisTemplate) CommonBeanFactory.getBean("redisTemplate");
            String key = KEY + token;
            if (redisTemplate.hasKey(key)) {
                redisTemplate.delete(key);
            }
            return;
        }
        CacheUtils.remove(KEY, token);
    }

    public static boolean invalid(String token) {
        if (useRedis()) {
            RedisTemplate redisTemplate = (RedisTemplate) CommonBeanFactory.getBean("redisTemplate");
            return redisTemplate.hasKey(KEY + token);
        }
        Object sys_token_store = CacheUtils.get(KEY, token);
        return ObjectUtils.isNotEmpty(sys_token_store) && StringUtils.isNotBlank(sys_token_store.toString());
    }

    public static void resetOnlinePools(Long userId, Set<OnlineUserModel> sets) {
        if (useRedis()) {
            RedisTemplate redisTemplate = (RedisTemplate) CommonBeanFactory.getBean("redisTemplate");
            redisTemplate.delete(ONLINE_TOKEN_POOL_KEY + userId);
            SetOperations setOperations = redisTemplate.opsForSet();
            Object[] modelArray = sets.stream().toArray();
            setOperations.add(ONLINE_TOKEN_POOL_KEY + userId, modelArray);
            return;
        }
        CacheUtils.removeAll(ONLINE_TOKEN_POOL_KEY);
        CacheUtils.put(ONLINE_TOKEN_POOL_KEY, userId, sets, null, null);
        CacheUtils.flush(ONLINE_TOKEN_POOL_KEY);
    }

    public static void add2OnlinePools(String token, Long userId) {
        if (useRedis()) {
            RedisTemplate redisTemplate = (RedisTemplate) CommonBeanFactory.getBean("redisTemplate");
            SetOperations setOperations = redisTemplate.opsForSet();
            setOperations.add(ONLINE_TOKEN_POOL_KEY + userId, buildModel(token));
            return;
        }
        Object listObj = null;
        Set<OnlineUserModel> models = null;
        if (ObjectUtils.isEmpty(listObj = CacheUtils.get(ONLINE_TOKEN_POOL_KEY, userId))) {
            models = new LinkedHashSet<>();
        } else {
            models = (Set<OnlineUserModel>) listObj;
        }
        models.add(buildModel(token));
        CacheUtils.put(ONLINE_TOKEN_POOL_KEY, userId, models, null, null);
        CacheUtils.flush(ONLINE_TOKEN_POOL_KEY);
    }

    public static String multiLoginType() {
        SystemParameterService service = CommonBeanFactory.getBean(SystemParameterService.class);
        return service.multiLoginType();
    }

    public static Set<OnlineUserModel> onlineUserTokens(Long userId) {
        if (useRedis()) {
            RedisTemplate redisTemplate = (RedisTemplate) CommonBeanFactory.getBean("redisTemplate");
            SetOperations setOperations = redisTemplate.opsForSet();
            Set tokens = setOperations.members(ONLINE_TOKEN_POOL_KEY + userId);
            return tokens;
        }
        Object o = CacheUtils.get(ONLINE_TOKEN_POOL_KEY, userId);
        if (ObjectUtils.isNotEmpty(o))
            return (Set<OnlineUserModel>) o;
        return null;
    }

    public static OnlineUserModel buildModel(String token) {
        OnlineUserModel model = new OnlineUserModel();
        model.setToken(token);
        model.setIp(IPUtils.get());
        model.setLoginTime(System.currentTimeMillis());
        return model;
    }

}
