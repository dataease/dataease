package io.dataease.commons.auth.util;


import cn.hutool.core.util.ObjectUtil;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Configuration;

/*@Configuration
public class CacheUtil {
    private static CacheManager manager;

    @Autowired
    public void setManager(CacheManager manager) {
        CacheUtil.manager = manager;
    }

    public static Object get(String cacheName, Object key) {
        Cache cache = cache(cacheName);
        if (ObjectUtil.isNull(cache))return null;
        Element element = cache.get(key);
        if (ObjectUtil.isNull(element))return null;
        Object objectValue = element.getObjectValue();
        return objectValue;
    }

    public static void put(String cacheName, Object key, Object value, Integer ttl, Integer tti) {
        Element e = new Element(key, value);
        //不设置则使用xml配置
        if (ttl != null)
            e.setTimeToLive(ttl);
        if (tti != null)
            e.setTimeToIdle(tti);
        cache(cacheName).put(e);
    }

    public static boolean remove(String cacheName, Object key) {
        return cache(cacheName).remove(key);
    }

    public static void removeAll(String cacheName) {
        cache(cacheName).removeAll();
    }

    private static Cache cache(String cacheName) {
        net.sf.ehcache.CacheManager cacheManager = ((EhCacheCacheManager) manager).getCacheManager();
        if (!cacheManager.cacheExists(cacheName))
            cacheManager.addCache(cacheName);
        Cache cache = cacheManager.getCache(cacheName);
        return cache;
    }
}*/
