package io.dataease.listener.util;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Configuration;
import java.util.Date;

@Configuration
public class CacheUtils {

    private static CacheManager manager;

    @Autowired
    public void setManager(CacheManager manager) {
        CacheUtils.manager = manager;
    }

    public static Object get(String cacheName, Object key) {
        Element element = cache(cacheName).get(key);
        if (null == element) return null;
        return element.getObjectValue();
    }

    public static void put(String cacheName, Object key, Object value, Integer ttl, Integer tti) {
        Element e = new Element(key, value);
        //不设置则使用xml配置
        if (ttl != null) {
            e.setEternal(false);
            e.setTimeToLive(ttl);
        }
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
        Cache cacheManagerCache = cacheManager.getCache(cacheName);
        return cacheManagerCache;
    }

    public static void updateLicCache(Date expDate){
        long time = expDate.getTime();
        long exp = (time - System.currentTimeMillis()) / 1000;
        int intExp = (int)exp;
        removeAll("lic_info");
        put("lic_info", "lic", "lic", intExp, intExp);
    }
}
