package io.dataease.utils;


import io.dataease.cache.DECacheService;
import org.apache.commons.lang3.ObjectUtils;

import java.util.concurrent.TimeUnit;


public class CacheUtils {

    private static DECacheService deCacheService;

    private DECacheService getService() {
        if (ObjectUtils.isEmpty(deCacheService)) {
            deCacheService = CommonBeanFactory.getBean(DECacheService.class);
        }
        return deCacheService;
    }

    public static void put(String cacheName, String key, Object val) {
        deCacheService.put(cacheName, key, val, 0L, null);
    }

    public static void put(String cacheName, String key, Object val, Long expTime, TimeUnit unit) {
        deCacheService.put(cacheName, key, val, expTime, unit);
    }

    public static Object get(String cacheName, String key) {
        return deCacheService.get(cacheName, key);
    }
}
