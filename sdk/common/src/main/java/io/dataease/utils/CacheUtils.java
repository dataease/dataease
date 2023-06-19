package io.dataease.utils;


import io.dataease.cache.DECacheService;
import io.dataease.exception.DEException;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


public class CacheUtils {

    private static DECacheService deCacheService;

    static {
        getService();
    }

    private static DECacheService getService() {
        if (ObjectUtils.isEmpty(deCacheService)) {
            deCacheService = (DECacheService) CommonBeanFactory.getBean("dECacheService");
        }
        return deCacheService;
    }

    public static void put(String cacheName, String key, Object val) {
        deCacheService.put(cacheName, key, val, 8L, TimeUnit.HOURS);
    }

    public static void put(String cacheName, String key, Object val, Long expTime, TimeUnit unit) {
        deCacheService.put(cacheName, key, val, expTime, unit);
    }

    public static Object get(String cacheName, String key) {
        return deCacheService.get(cacheName, key);
    }

    public static Boolean keyExist(String cacheName, String key) {
        return deCacheService.keyExist(cacheName, key);
    }

    public static void keyRemove(String cacheName, String key) {
        deCacheService.keyRemove(cacheName, key);
    }

    public static void remove(String cacheName, String key, Consumer consumer) {
        deCacheService.keyRemove(cacheName, key);
        consumer.accept(null);
        try {
            TimeUnit.MILLISECONDS.sleep(1000L);
            deCacheService.keyRemove(cacheName, key);
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }

    public static void remove(String[] cacheNames, String key, Consumer consumer) {
        Arrays.stream(cacheNames).forEach(cacheName -> {
            deCacheService.keyRemove(cacheName, key);
        });

        consumer.accept(null);
        try {
            TimeUnit.MILLISECONDS.sleep(1000L);
            Arrays.stream(cacheNames).forEach(cacheName -> {
                deCacheService.keyRemove(cacheName, key);
            });
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }

    public static void remove(String cacheName, List<String> keys, Consumer consumer) {
        keys.forEach(key -> deCacheService.keyRemove(cacheName, key));
        consumer.accept(null);
        try {
            TimeUnit.MILLISECONDS.sleep(1000L);
            keys.forEach(key -> deCacheService.keyRemove(cacheName, key));
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }
    public static void remove(String[] cacheNames, List<String> keys, Consumer consumer) {
        Arrays.stream(cacheNames).forEach(cacheName -> {
            keys.forEach(key -> deCacheService.keyRemove(cacheName, key));
        });
        consumer.accept(null);
        try {
            TimeUnit.MILLISECONDS.sleep(1000L);
            Arrays.stream(cacheNames).forEach(cacheName -> {
                keys.forEach(key -> deCacheService.keyRemove(cacheName, key));
            });
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }
}
