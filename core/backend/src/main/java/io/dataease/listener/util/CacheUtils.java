package io.dataease.listener.util;

import io.dataease.commons.utils.CommonBeanFactory;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CacheUtils {

    private static CacheManager cacheManager;


    private static CacheManager getCacheManager() {
        if (cacheManager == null)
            cacheManager = CommonBeanFactory.getBean(CacheManager.class);
        return cacheManager;
    }

    public static Object get(String cacheName, Object key) {
        if (getCacheManager() instanceof RedisCacheManager) {
            org.springframework.cache.Cache cache = getCacheManager().getCache(cacheName);
            if (null == cache || null == cache.get(key)) return null;
            return cache.get(key).get();
        }
        Element element = cache(cacheName).get(key);
        if (null == element) return null;
        return element.getObjectValue();
    }

    public static void put(String cacheName, Object key, Object value, Integer ttl, Integer tti) {
        if (getCacheManager() instanceof RedisCacheManager) {
            /*RedisTemplate redisTemplate = (RedisTemplate) CommonBeanFactory.getBean("redisTemplate");
            ValueOperations valueOperations = redisTemplate.opsForValue();
            valueOperations.set(cacheName + "::" + key , value );*/
            org.springframework.cache.Cache cache = getCacheManager().getCache(cacheName);
            if (null == cache) return;
            cache.put(key, value);
            return;
        }
        Element e = new Element(key, value);
        //不设置则使用xml配置
        if (ttl != null) {
            e.setEternal(false);
            e.setTimeToLive(ttl);
        }
        if (tti != null)
            e.setTimeToIdle(tti);
        Cache cache = cache(cacheName);
        if (null != cache)
            cache.put(e);
    }

    public static boolean remove(String cacheName, Object key) {
        if (getCacheManager() instanceof RedisCacheManager) {
            org.springframework.cache.Cache cache = getCacheManager().getCache(cacheName);
            if (null == cache) return false;
            return cache.evictIfPresent(key);
        }
        return cache(cacheName).remove(key);
    }

    public static void flush(String cacheName) {
        CacheManager manager = getCacheManager();
        if (manager instanceof RedisCacheManager) return;
        cache(cacheName).flush();
    }

    public static void removeAll(String cacheName) {
        if (getCacheManager() instanceof RedisCacheManager) {
            org.springframework.cache.Cache cache = getCacheManager().getCache(cacheName);
            if (null == cache) return;
            cache.clear();
            return;
        }
        cache(cacheName).removeAll();
    }

    private static Cache cache(String cacheName) {
        if (getCacheManager() instanceof RedisCacheManager) {
            return null;
        }
        net.sf.ehcache.CacheManager cacheManager = ((EhCacheCacheManager) getCacheManager()).getCacheManager();
        if (!cacheManager.cacheExists(cacheName))
            cacheManager.addCache(cacheName);
        Cache cacheManagerCache = cacheManager.getCache(cacheName);
        return cacheManagerCache;
    }

    public static void updateLicCache(Date expDate){
        long time = expDate.getTime();
        long exp = (time - System.currentTimeMillis()) / 1000;
        int intExp = (int)exp;
        removeAll("lic_info");
        if (getCacheManager() instanceof RedisCacheManager) {
            RedisTemplate redisTemplate = (RedisTemplate) CommonBeanFactory.getBean("redisTemplate");
            ValueOperations valueOperations = redisTemplate.opsForValue();
            valueOperations.set("lic_info::lic", "lic", exp, TimeUnit.SECONDS);
            return;
        }
        put("lic_info", "lic", "lic", intExp, intExp);
    }
}
