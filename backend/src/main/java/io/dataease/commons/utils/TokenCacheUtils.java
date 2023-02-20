package io.dataease.commons.utils;

import io.dataease.listener.util.CacheUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Component
public class TokenCacheUtils {


    private static final String KEY = "sys_token_store";

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
        CacheUtils.put(KEY, token, userId, time.intValue(), null);

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

}
