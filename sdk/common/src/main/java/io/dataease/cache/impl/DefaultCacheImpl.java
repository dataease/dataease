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
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
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
            // XML/native caches keep their configured expiry and value types.
            DynamicCacheExpiryPolicy.Factory factory = dynamicExpiryFactory(cache);
            if (factory == null) {
                cache.put(key, value);
            } else {
                Duration duration = expTime > 0
                        ? new Duration(unit == null ? TimeUnit.SECONDS : unit, expTime)
                        : factory.getDefaultDuration();
                DynamicCacheExpiryPolicy.put(cache, key, value, duration);
            }
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
    public Object take(String cacheName, String key) {
        Cache<Object, Object> cache = cacheManager.getCache(cacheName);
        return cache == null ? null : cache.getAndRemove(key);
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

    private MutableConfiguration<String, Object> configuration(Long expTime, TimeUnit unit) {
        Duration duration = expTime <= 0 ? Duration.ONE_MINUTE
                : new Duration(unit == null ? TimeUnit.SECONDS : unit, expTime);
        return new MutableConfiguration<String, Object>()
                .setTypes(String.class, Object.class)
                .setStoreByValue(false)
                .setExpiryPolicyFactory(new DynamicCacheExpiryPolicy.Factory(duration));
    }

    private DynamicCacheExpiryPolicy.Factory dynamicExpiryFactory(Cache<String, Object> cache) {
        // Native/XML caches need not expose CompleteConfiguration.
        Object configuration = cache.getConfiguration(Configuration.class);
        if (configuration instanceof CompleteConfiguration<?, ?> completeConfiguration) {
            Object factory = completeConfiguration.getExpiryPolicyFactory();
            return factory instanceof DynamicCacheExpiryPolicy.Factory dynamicFactory ? dynamicFactory : null;
        }
        return null;
    }

    @Override
    public void keyRemove(String cacheName, String key) {
        Cache<Object, Object> cache = cacheManager.getCache(cacheName);
        if(cache != null){
            cache.remove(key);
        }
    }

    @PostConstruct
    public void init() {
        cacheManager = ((JCacheCacheManager) jcacheManager).getCacheManager();
    }

}
