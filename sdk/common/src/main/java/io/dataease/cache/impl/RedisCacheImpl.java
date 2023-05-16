package io.dataease.cache.impl;

import io.dataease.cache.DECacheService;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@ConditionalOnExpression("'${spring.cache.type}'.equals('redis')")
@Component
public class RedisCacheImpl implements DECacheService {

    @Resource
    private RedisTemplate redisTemplate;
    @Override
    public void create(String cacheName, Long expTime, TimeUnit unit) {

    }

    @Override
    public void put(String cacheName, String key, Object value) {

    }

    @Override
    public Object get(String cacheName, String key) {
        return null;
    }

    @Override
    public boolean cacheExist(String cacheName) {
        return false;
    }

    @Override
    public boolean keyExist(String cacheName, String key) {
        return false;
    }
}
