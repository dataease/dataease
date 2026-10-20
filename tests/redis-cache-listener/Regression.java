package io.dataease.listener;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Runs the real listener with binary-key storage and controlled connection failures; no Redis. */
public class Regression {
    private static int assertions;

    private static void check(boolean condition, String message) {
        assertions++;
        if (!condition) throw new AssertionError(message);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static String binaryKey(byte[] key) {
        return Base64.getEncoder().encodeToString(key);
    }

    private static class Fixture {
        final List<byte[]> scanned = new ArrayList<>();
        final List<Integer> batches = new ArrayList<>();
        final Set<String> stored = new HashSet<>();
        final List<String> closed = new ArrayList<>();
        final RuntimeException failure = new IllegalStateException("controlled failure");
        String failAt = "";
        int position;
        int connections;
        boolean connectionClosed;
        boolean cursorClosesConnection;
        boolean stringSerializer;
        final byte[] unrelated = StringRedisSerializer.UTF_8.serialize("other_application::keep");

        void add(byte[] key) {
            scanned.add(key);
            stored.add(binaryKey(key));
        }

        void closeConnection() {
            if (!connectionClosed) {
                closed.add("connection");
                connectionClosed = true;
            }
        }

        void run() {
            stored.add(binaryKey(unrelated));
            Cursor<byte[]> cursor = proxy(Cursor.class, (p, method, args) -> {
                switch (method.getName()) {
                    case "hasNext":
                        if (failAt.equals("iterate")) throw failure;
                        return position < scanned.size();
                    case "next": return scanned.get(position++);
                    case "close":
                        closed.add("cursor");
                        if (cursorClosesConnection) closeConnection();
                        if (failAt.equals("close")) throw failure;
                        return null;
                    default: throw new AssertionError(method);
                }
            });
            RedisKeyCommands keys = proxy(RedisKeyCommands.class, (p, method, args) -> {
                if (!method.getName().equals("del")) throw new AssertionError(method);
                check(!connectionClosed && !closed.contains("cursor"), "delete after close");
                byte[][] batch = (byte[][]) args[0];
                check(batch.length > 0 && batch.length <= 1000, "unbounded or empty DEL");
                batches.add(batch.length);
                if (failAt.equals("delete")) throw failure;
                long removed = 0;
                for (byte[] key : batch) {
                    check(scanned.stream().anyMatch(raw -> Arrays.equals(raw, key)), "key bytes changed");
                    if (stored.remove(binaryKey(key))) removed++;
                }
                return removed;
            });
            RedisConnection connection = proxy(RedisConnection.class, (p, method, args) -> {
                switch (method.getName()) {
                    case "scan":
                        ScanOptions options = (ScanOptions) args[0];
                        check("*de_v3_*".equals(options.getPattern()), "match scope changed");
                        check(Long.valueOf(1000).equals(options.getCount()), "scan count changed");
                        if (failAt.equals("scan")) throw failure;
                        return cursor;
                    case "keyCommands": return keys;
                    case "close": closeConnection(); return null;
                    default: throw new AssertionError(method);
                }
            });
            RedisConnectionFactory factory = proxy(RedisConnectionFactory.class, (p, method, args) -> {
                if (!method.getName().equals("getConnection")) throw new AssertionError(method);
                connections++;
                return connection;
            });
            RedisTemplate<String, String> template = new RedisTemplate<>();
            template.setConnectionFactory(factory);
            if (stringSerializer) template.setKeySerializer(StringRedisSerializer.UTF_8);
            template.afterPropertiesSet();
            new RedisCacheListener().deleteKeysContainingString(template, "de_v3_");
        }

        void verifyClosed(boolean cursorCreated) {
            check(connections == 1, "must reuse one connection");
            check(closed.equals(cursorCreated ? List.of("cursor", "connection") : List.of("connection")),
                    "resources not closed in order: " + closed);
            check(stored.contains(binaryKey(unrelated)), "unscanned key deleted");
        }
    }

    public static void main(String[] args) {
        JdkSerializationRedisSerializer jdk = new JdkSerializationRedisSerializer();
        Fixture mixed = new Fixture();
        mixed.add(jdk.serialize("de_v3_dataset_table::中文"));
        mixed.add(StringRedisSerializer.UTF_8.serialize("de_v3_rsa::-de-"));
        mixed.add(new byte[]{(byte) 0xff, 0, 'd', 'e', '_', 'v', '3', '_', (byte) 0x80});
        mixed.run();
        check(mixed.stored.size() == 1, "mixed serialized keys remain");
        mixed.verifyClosed(true);

        Fixture strings = new Fixture();
        strings.stringSerializer = true;
        strings.add(StringRedisSerializer.UTF_8.serialize("de_v3_test::中文"));
        strings.run();
        check(strings.stored.size() == 1, "string serializer key remains");
        strings.verifyClosed(true);

        Fixture empty = new Fixture();
        empty.run();
        check(empty.batches.isEmpty(), "empty scan must not DEL");
        empty.verifyClosed(true);

        for (int size : new int[]{1000, 2501}) {
            Fixture many = new Fixture();
            many.cursorClosesConnection = true; // Lettuce cursor close also closes its connection.
            for (int i = 0; i < size; i++) many.add(jdk.serialize("de_v3_test::" + i));
            if (size == 2501) many.add(many.scanned.get(0)); // SCAN can return duplicates.
            many.run();
            check(many.batches.equals(size == 1000 ? List.of(1000) : List.of(1000, 1000, 502)),
                    "batch or tail missing");
            check(many.stored.size() == 1, "keys remain after batches");
            many.verifyClosed(true);
        }

        for (String stage : List.of("scan", "iterate", "delete", "close")) {
            Fixture broken = new Fixture();
            broken.add(jdk.serialize("de_v3_test::failure"));
            broken.failAt = stage;
            try {
                broken.run();
                throw new AssertionError("expected failure at " + stage);
            } catch (RuntimeException e) {
                check(e == broken.failure, "original exception lost");
            }
            broken.verifyClosed(!stage.equals("scan"));
        }
        System.out.println("PASS: " + assertions + " assertions (9 scenarios), no Redis connection");
    }
}
