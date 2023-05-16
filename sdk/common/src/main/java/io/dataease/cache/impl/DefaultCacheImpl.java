package io.dataease.cache.impl;

import io.dataease.cache.DECacheService;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.stereotype.Component;

import javax.cache.CacheManager;
import java.util.concurrent.TimeUnit;

@ConditionalOnExpression("'${spring.cache.type}'.equals('jcache')")
@Component
public class DefaultCacheImpl implements DECacheService {

    /*@Resource
    private JCacheCacheManager jCacheCacheManager;*/
    /*@Resource
    private EhcacheManager ehcacheManager;*/

    @Override
    public void create(String cacheName, Long expTime, TimeUnit unit) {
        // CacheManager cacheManager = jCacheCacheManager.getCacheManager();

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
