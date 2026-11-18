package io.dataease.cache.impl;

import org.ehcache.jsr107.EhcacheCachingProvider;
import org.springframework.cache.jcache.JCacheCacheManager;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Load the unmodified product XML in a separate JVM for each persistence phase. */
public class XmlIntegration {
    private static int checks;
    private static final String LOGIN = "de_v3_user_token_cache";
    private static final String BLACKLIST = "de_v3_embedded_token_cache";
    private static final String LANGUAGE = "de_v3_user_community_language";

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }

    private static void inject(Object target, String name, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    public static void main(String[] args) throws Exception {
        Path xml = Path.of(args[0]).toAbsolutePath();
        Path directory = Path.of(args[1]).toAbsolutePath();
        String phase = args[2];
        System.setProperty("dataease.path.ehcache", directory.toString());
        // Product XML specifies minutes; retain the real unit and wait for expiry across JVMs.
        System.setProperty("dataease.login_timeout", "1");
        try (var provider = new EhcacheCachingProvider();
             var manager = provider.getCacheManager(xml.toUri(), XmlIntegration.class.getClassLoader())) {
            var spring = new JCacheCacheManager(manager);
            var service = new DefaultCacheImpl();
            inject(service, "jcacheManager", spring);
            service.init();
            int count = 0;
            for (String ignored : manager.getCacheNames()) count++;
            check(count >= 20, "product XML creates all predeclared caches");
            check(manager.getCache(LOGIN).getConfiguration(javax.cache.configuration.Configuration.class).getValueType() == Integer.class,
                    "XML token type retained");
            if (phase.equals("seed")) {
                service.put(LANGUAGE, "de", "en-US", 50L, TimeUnit.MILLISECONDS);
                service.put("de_v3_dataset_table", "synthetic-datasource", new ArrayList<>(List.of("table_a")), 50L, TimeUnit.MILLISECONDS);
                long written = System.currentTimeMillis();
                service.put(LOGIN, "synthetic-user:valid-token", 1, 50L, TimeUnit.MILLISECONDS);
                Files.writeString(directory.resolve("written-at"), Long.toString(written));
                service.put(LOGIN, "synthetic-user:removed-token", 1, 8L, TimeUnit.HOURS);
                service.keyRemove(LOGIN, "synthetic-user:removed-token");
                service.put(BLACKLIST, "synthetic-revoked-token", 1, 50L, TimeUnit.MILLISECONDS);
                service.put(BLACKLIST, "synthetic-consumed-token", 1, 8L, TimeUnit.HOURS);
                check(Integer.valueOf(1).equals(service.take(BLACKLIST, "synthetic-consumed-token")), "consume blacklist fixture");
                check(service.take(BLACKLIST, "synthetic-consumed-token") == null, "consumed entry stays absent");
                service.put(BLACKLIST, "synthetic-removed-token", 1, 8L, TimeUnit.HOURS);
                service.keyRemove(BLACKLIST, "synthetic-removed-token");
                Thread.sleep(250);
                check("en-US".equals(service.get(LANGUAGE, "de")), "language ignores supplied short TTL");
                check(service.keyExist(LOGIN, "synthetic-user:valid-token"), "login follows XML minute TTL");
                check(service.keyExist(BLACKLIST, "synthetic-revoked-token"), "blacklist ignores supplied short TTL");
                check(List.of("table_a").equals(service.get("de_v3_dataset_table", "synthetic-datasource")), "dataset cache follows XML policy");
                check("en-US".equals(spring.getCache(LANGUAGE).get("de").get()), "Spring reads the same XML cache");
                spring.getCache(LANGUAGE).put("eviction-test", "zh-CN");
                service.keyRemove(LANGUAGE, "eviction-test");
                check(spring.getCache(LANGUAGE).get("eviction-test") == null, "service invalidation reaches Spring cache");
            } else {
                long written = Long.parseLong(Files.readString(directory.resolve("written-at")));
                check(service.keyExist(BLACKLIST, "synthetic-revoked-token"), "persisted logout blacklist survives JVM restart");
                check(service.get(BLACKLIST, "synthetic-consumed-token") == null, "consumed disk entry does not reappear");
                check(service.get(BLACKLIST, "synthetic-removed-token") == null, "removed disk entry does not reappear");
                check(service.get(LOGIN, "synthetic-user:removed-token") == null, "removed login does not reappear");
                check(service.get(LANGUAGE, "de") == null, "nonpersistent language cache is cold after restart as before");
                check(service.get("de_v3_dataset_table", "synthetic-datasource") == null, "nonpersistent dataset cache is cold after restart");
                if (phase.equals("recover")) {
                    check(System.currentTimeMillis() - written < 55000, "recovery runs before login deadline");
                    check(service.keyExist(LOGIN, "synthetic-user:valid-token"), "unexpired login survives JVM restart");
                    service.put(LANGUAGE, "de", "en-US", 10L, TimeUnit.MILLISECONDS);
                    Thread.sleep(80);
                    check("en-US".equals(service.get(LANGUAGE, "de")), "recovered manager still retains XML expiry");
                } else {
                    check(System.currentTimeMillis() - written >= 61000, "expiry verification crosses original minute deadline");
                    check(!service.keyExist(LOGIN, "synthetic-user:valid-token"), "restart does not extend original login deadline");
                }
            }
        }
        System.out.println("PASS product XML " + phase + ": " + checks + " checks");
    }
}
