package io.dataease.utils;


import io.dataease.cache.DECacheService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


@Component
public class CacheUtils {


    /*private static DECacheService deCacheService;

    @Resource
    public void setDeCacheService(DECacheService deCacheService) {
        CacheUtils.deCacheService = deCacheService;
    }

    public static void put(String cacheName, String key, Object val) {
        if (!deCacheService.cacheExist(cacheName)) {
            deCacheService.create(cacheName, 0L, null);
        }
        deCacheService.put(cacheName, key, val);
    }

    public static Object get(String cacheName, String key) {
        return deCacheService.get(cacheName, key);
    }*/
}
