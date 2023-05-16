package io.dataease.utils;

/*import io.dataease.cache.CacheExpListener;
import io.dataease.exception.DEException;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.stereotype.Component;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ModifiedExpiryPolicy;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class CacheUtils {

    private static JCacheCacheManager jCacheCacheManager;


    @Resource
    public void setCacheManager(JCacheCacheManager jCacheCacheManager) {
        CacheUtils.jCacheCacheManager = jCacheCacheManager;
    }

    public static void setCache(String cacheName, String key, Object value) {
        Cache<String, Object> cache = getCache(cacheName);
        if (cache.containsKey(key)) {
            cache.remove(key);
        }
        cache.put(key, value);
    }

    private static Cache<String, Object> getCache(String cacheName) {
        CacheManager cacheManager = jCacheCacheManager.getCacheManager();
        Cache<String, Object> cache = null;
        if (ObjectUtils.isEmpty(cache = cacheManager.getCache(cacheName))) {
            cache = create(cacheName, null, null);
            if (ObjectUtils.isEmpty(cache)) DEException.throwException("获取缓存失败");
        }
        return cache;
    }

    private static Cache<String, Object> create(String cacheName, Long expSeconds, Function func) {
        javax.cache.CacheManager cacheManager = jCacheCacheManager.getCacheManager();

        MutableConfiguration<String, Object> configuration =
                new MutableConfiguration<String, Object>()
                        .setTypes(String.class, Object.class)
                        .setStatisticsEnabled(true)
                        .setManagementEnabled(true);
        if (ObjectUtils.isNotEmpty(expSeconds) && expSeconds > 0L) {
            configuration.setExpiryPolicyFactory(ModifiedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, expSeconds)));
        }
        MutableCacheEntryListenerConfiguration listenerConfiguration = null;
        if (ObjectUtils.isNotEmpty(func) && ObjectUtils.isNotEmpty(listenerConfiguration = initExpListener(func))) {
            configuration.addCacheEntryListenerConfiguration(listenerConfiguration);
        }
        Cache<String, Object> cache = cacheManager.createCache(cacheName, configuration);
        return cache;
    }

    private static MutableCacheEntryListenerConfiguration initExpListener(Function func) {
        FactoryBuilder.SingletonFactory singletonFactory = new FactoryBuilder.SingletonFactory(new CacheExpListener(func));
        MutableCacheEntryListenerConfiguration configuration = new MutableCacheEntryListenerConfiguration(singletonFactory, null, false, false);
        return configuration;
    }

    public static <T> T getCache(String cacheName, String key) {
        Cache<String, Object> cache = getCache(cacheName);
        return (T) cache.get(key);
    }
}*/
