package io.dataease.cache.impl;

import io.dataease.cache.DECacheService;
import io.dataease.utils.LogUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ModifiedExpiryPolicy;
import java.util.concurrent.TimeUnit;

@ConditionalOnExpression("'${spring.cache.type}'.equals('jcache')")
@Component("dECacheService")
public class DefaultCacheImpl implements DECacheService {


    private CacheManager cacheManager;

    @Resource
    private org.springframework.cache.CacheManager jcacheManager;


    @Override
    public void put(String cacheName, String key, Object value, Long expTime, TimeUnit unit) {
        Cache<String, Object> cache = null;
        if (ObjectUtils.isEmpty(cache = cacheManager.getCache(cacheName))) {
            try {
                cache = cacheManager.createCache(cacheName, configuration(expTime, unit));
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                cache = cacheManager.getCache(cacheName);
            }

        }
        if (ObjectUtils.isNotEmpty(cache)) {
            cache.put(key, value);
        }

    }

    @Override
    public Object get(String cacheName, String key) {
        Cache<Object, Object> cache = null;
        if (ObjectUtils.isEmpty(cache = cacheManager.getCache(cacheName))) {
            return null;
        }
        return cache.get(key);
    }

    @Override
    public boolean cacheExist(String cacheName) {
        return ObjectUtils.isNotEmpty(cacheManager.getCache(cacheName));
    }

    @Override
    public boolean keyExist(String cacheName, String key) {
        Cache<Object, Object> cache = null;
        if (ObjectUtils.isEmpty(cache = cacheManager.getCache(cacheName))) {
            return false;
        }
        return cache.containsKey(key);
    }

    private MutableConfiguration<String, Object> defaultConfiguration() {
        MutableConfiguration<String, Object> configuration =
                new MutableConfiguration<String, Object>()
                        .setTypes(String.class, Object.class)
                        .setStoreByValue(false)
                        .setExpiryPolicyFactory(ModifiedExpiryPolicy.factoryOf(Duration.ONE_MINUTE));
        return configuration;
    }

    private MutableConfiguration<String, Object> configuration(Long expTime, TimeUnit unit) {
        if (expTime <= 0) return defaultConfiguration();
        if (ObjectUtils.isEmpty(unit)) {
            unit = TimeUnit.SECONDS;
        }
        MutableConfiguration<String, Object> configuration =
                new MutableConfiguration<String, Object>()
                        .setTypes(String.class, Object.class)
                        .setStoreByValue(false)
                        .setExpiryPolicyFactory(ModifiedExpiryPolicy.factoryOf(new Duration(unit, expTime)));
        return configuration;
    }

    @Override
    public void keyRemove(String cacheName, String key) {
        Cache<Object, Object> cache = cacheManager.getCache(cacheName);
        cache.remove(key);
    }

    @PostConstruct
    public void init() {
        cacheManager = ((JCacheCacheManager) jcacheManager).getCacheManager();
    }

}
