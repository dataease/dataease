package io.dataease.cache.impl;

import io.dataease.utils.CacheUtils;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.jcache.JCacheCacheManager;

import javax.cache.CacheManager;
import javax.cache.expiry.Duration;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Regression {
    private static int checks;

    private static void check(boolean result, String message) {
        if (!result) throw new AssertionError(message);
        checks++;
    }

    private static void inject(Object target, String field, Object value) throws Exception {
        Field declared = target.getClass().getDeclaredField(field);
        declared.setAccessible(true);
        declared.set(target, value);
    }

    public static void main(String[] args) throws Exception {
        try (var provider = new EhcacheCachingProvider();
             var manager = provider.getCacheManager()) {
            var service = new DefaultCacheImpl();
            var spring = new JCacheCacheManager(manager);
            inject(service, "jcacheManager", spring);
            service.init();
            mixedTtl(service, "short-first", false);
            mixedTtl(service, "long-first", true);
            service.put("overwrite", "key", "old", 100L, TimeUnit.MILLISECONDS);
            service.put("overwrite", "key", "new", 3L, TimeUnit.SECONDS);
            Thread.sleep(180);
            check("new".equals(service.get("overwrite", "key")), "overwrite extends TTL");
            service.put("overwrite", "key", "short", 100L, TimeUnit.MILLISECONDS);
            Thread.sleep(180);
            check(service.get("overwrite", "key") == null, "overwrite shortens TTL");
            service.put("read", "key", "value", 180L, TimeUnit.MILLISECONDS);
            Thread.sleep(100);
            check(service.get("read", "key") != null, "read before expiry");
            Thread.sleep(150);
            check(service.get("read", "key") == null && !service.keyExist("read", "key"), "read does not renew");

            service.put("legacy-zero", "key", "value", 0L, null);
            service.put("legacy-negative", "key", "value", -1L, null);
            service.put("legacy-positive-first", "seed", "value", 3L, TimeUnit.SECONDS);
            service.put("legacy-positive-first", "fallback", "value", 0L, null);
            var positiveFactory = manager.getCache("legacy-positive-first").getConfiguration(javax.cache.configuration.CompleteConfiguration.class)
                    .getExpiryPolicyFactory();
            check(new Duration(TimeUnit.SECONDS, 3).equals(((javax.cache.expiry.ExpiryPolicy) positiveFactory.create()).getExpiryForCreation()),
                    "nonpositive writes retain the cache's historical fallback");
            check(service.get("legacy-zero", "key") != null, "zero is not immediate expiry");
            check(service.get("legacy-negative", "key") != null, "negative is not immediate expiry");
            var zeroFactory = manager.getCache("legacy-zero").getConfiguration(javax.cache.configuration.CompleteConfiguration.class)
                    .getExpiryPolicyFactory();
            check(Duration.ONE_MINUTE.equals(((javax.cache.expiry.ExpiryPolicy) zeroFactory.create()).getExpiryForCreation()), "legacy one-minute fallback");
            service.put("null-unit", "key", "value", 1L, null);
            var nullFactory = manager.getCache("null-unit").getConfiguration(javax.cache.configuration.CompleteConfiguration.class)
                    .getExpiryPolicyFactory();
            check(new Duration(TimeUnit.SECONDS, 1).equals(((javax.cache.expiry.ExpiryPolicy) nullFactory.create()).getExpiryForCreation()), "null unit remains seconds");
            check(((javax.cache.expiry.ExpiryPolicy) nullFactory.create()).getExpiryForAccess() == null, "policy does not renew on access");

            // A rejected write must not leave its TTL on this thread for a later direct/Spring put.
            service.put("failure", "seed", "value", 3L, TimeUnit.SECONDS);
            try {
                service.put("failure", "null", null, 50L, TimeUnit.MILLISECONDS);
                throw new AssertionError("null must be rejected");
            } catch (NullPointerException expected) {
                manager.getCache("failure").put("direct", "value");
            }
            Thread.sleep(100);
            check(service.get("failure", "direct") != null, "failed put clears expiry context");
            legacyCaches(manager, service, spring);
            concurrent(manager, service);

            var values = new ArrayList<>(List.of("before"));
            service.put("reference", "key", values, 3L, TimeUnit.SECONDS);
            values.add("after");
            check(((List<?>) service.get("reference", "key")).size() == 2, "no global object copy introduced");
            service.keyRemove("reference", "key");
            check(service.get("reference", "key") == null, "delete remains effective");
            service.put("take", "key", "once", 3L, TimeUnit.SECONDS);
            var pool = Executors.newFixedThreadPool(8);
            try {
                var tasks = new ArrayList<Callable<Object>>();
                for (int i = 0; i < 16; i++) tasks.add(() -> service.take("take", "key"));
                long consumed = 0;
                for (var result : pool.invokeAll(tasks)) if (result.get() != null) consumed++;
                check(consumed == 1, "take remains atomic");
            } finally {
                pool.shutdownNow();
            }
            // Exercise the real CacheUtils default, without starting Spring.
            inject(null, CacheUtils.class, "deCacheService", service);
            CacheUtils.put("default-eight-hours", "key", "value");
            var defaultFactory = manager.getCache("default-eight-hours").getConfiguration(javax.cache.configuration.CompleteConfiguration.class)
                    .getExpiryPolicyFactory();
            check(new Duration(TimeUnit.HOURS, 8).equals(((javax.cache.expiry.ExpiryPolicy) defaultFactory.create()).getExpiryForCreation()), "default remains eight hours");
        }
        System.out.println("PASS local cache compatibility: " + checks + " checks");
    }

    private static void inject(Object target, Class<?> type, String field, Object value) throws Exception {
        var declared = type.getDeclaredField(field);
        declared.setAccessible(true);
        declared.set(target, value);
    }

    private static void mixedTtl(DefaultCacheImpl service, String name, boolean reverse) throws Exception {
        if (reverse) service.put(name, "long", "long", 3L, TimeUnit.SECONDS);
        service.put(name, "short", "short", 100L, TimeUnit.MILLISECONDS);
        if (!reverse) service.put(name, "long", "long", 3L, TimeUnit.SECONDS);
        Thread.sleep(180);
        check(service.get(name, "short") == null, "mixed TTL short " + name);
        check("long".equals(service.get(name, "long")), "mixed TTL long " + name);
    }

    private static void legacyCaches(CacheManager manager, DefaultCacheImpl service, JCacheCacheManager spring) throws Exception {
        // Native typed configurations take the same bridge as the predeclared XML caches.
        manager.createCache("language", Eh107Configuration.fromEhcacheCacheConfiguration(
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, String.class,
                        ResourcePoolsBuilder.heap(20)).withExpiry(ExpiryPolicyBuilder.noExpiration())));
        service.put("language", "de", "en-US", 50L, TimeUnit.MILLISECONDS);
        manager.createCache("login", Eh107Configuration.fromEhcacheCacheConfiguration(
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Integer.class,
                        ResourcePoolsBuilder.heap(20)).withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(java.time.Duration.ofMillis(100)))));
        service.put("login", "uid:token", 1, 8L, TimeUnit.HOURS);
        manager.createCache("blacklist", Eh107Configuration.fromEhcacheCacheConfiguration(
                CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Integer.class,
                        ResourcePoolsBuilder.heap(20)).withExpiry(ExpiryPolicyBuilder.noExpiration())));
        service.put("blacklist", "token", 1, 50L, TimeUnit.MILLISECONDS);
        manager.createCache("pre-existing", new javax.cache.configuration.MutableConfiguration<String, Object>()
                .setTypes(String.class, Object.class).setStoreByValue(false)
                .setExpiryPolicyFactory(javax.cache.expiry.ModifiedExpiryPolicy.factoryOf(new Duration(TimeUnit.MILLISECONDS, 100))));
        service.put("pre-existing", "key", "value", 8L, TimeUnit.HOURS);
        Thread.sleep(180);
        check("en-US".equals(service.get("language", "de")), "native language does not expire by put TTL");
        check(service.get("login", "uid:token") == null, "native login uses configured TTL");
        check(service.keyExist("blacklist", "token"), "native blacklist is retained");
        check(service.get("pre-existing", "key") == null, "pre-existing JCache policy is retained");
        check("en-US".equals(spring.getCache("language").get("de").get()), "Spring and service share native values");
        spring.getCache("language").evict("de");
        check(service.get("language", "de") == null, "Spring eviction invalidates service read");
    }

    private static void concurrent(CacheManager manager, DefaultCacheImpl service) throws Exception {
        var pool = Executors.newFixedThreadPool(8);
        var start = new CountDownLatch(1);
        try {
            var tasks = new ArrayList<Callable<Void>>();
            for (int i = 0; i < 16; i++) {
                int key = i;
                tasks.add(() -> {
                    start.await();
                    service.put("concurrent", "key" + key, key, key % 2 == 0 ? 200L : 3000L, TimeUnit.MILLISECONDS);
                    return null;
                });
            }
            var futures = new ArrayList<Future<Void>>();
            for (var task : tasks) futures.add(pool.submit(task));
            start.countDown();
            for (var future : futures) future.get();
            Thread.sleep(300);
            for (int i = 0; i < 16; i++) check((service.get("concurrent", "key" + i) != null) == (i % 2 != 0), "concurrent per-key expiry " + i);
        } finally {
            pool.shutdownNow();
        }
    }
}
