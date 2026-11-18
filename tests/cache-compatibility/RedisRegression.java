package io.dataease.cache.impl;

import io.dataease.utils.CacheUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/** Client contract only: Redis transport is mocked, not a Redis integration test. */
public class RedisRegression {
    private static int checks;

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }

    private static void inject(Object target, Class<?> type, String field, Object value) throws Exception {
        var declared = type.getDeclaredField(field);
        declared.setAccessible(true);
        declared.set(target, value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void main(String[] args) throws Exception {
        var template = mock(RedisTemplate.class);
        var ops = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(ops);
        var service = new RedisCacheImpl();
        inject(service, RedisCacheImpl.class, "redisTemplate", template);
        service.put("cache", "a", "short", 60L, TimeUnit.SECONDS);
        service.put("cache", "b", "long", 1L, TimeUnit.HOURS);
        verify(ops).set("cache::a", "short", 60L, TimeUnit.SECONDS);
        verify(ops).set("cache::b", "long", 1L, TimeUnit.HOURS);
        checks += 2;
        service.put("cache", "zero", 1, 0L, null);
        service.put("cache", "negative", 1, -1L, null);
        verify(ops).set("cache::zero", 1);
        verify(ops).set("cache::negative", 1);
        checks += 2;
        service.put("cache", "seconds", 1, 2L, null);
        verify(ops).set("cache::seconds", 1, 2L, TimeUnit.SECONDS);
        checks++;
        inject(null, CacheUtils.class, "deCacheService", service);
        CacheUtils.put("cache", "default", 1);
        verify(ops).set("cache::default", 1, 8L, TimeUnit.HOURS);
        checks++;
        when(ops.get("cache::a")).thenReturn("short");
        check("short".equals(service.get("cache", "a")), "get uses existing key namespace");
        verify(ops).get("cache::a");
        verifyNoMoreInteractions(ops);
        checks++;
        var manager = mock(CacheManager.class);
        var spring = mock(Cache.class);
        when(manager.getCache("cache")).thenReturn(spring);
        inject(null, RedisCacheImpl.class, "cacheManager", manager);
        service.keyRemove("cache", "a");
        verify(spring).evictIfPresent("a");
        verify(template).delete("cache::a");
        checks += 2;
        when(template.execute(any(RedisScript.class), eq(Collections.singletonList("cache::consume")), any(Object[].class)))
                .thenReturn("once");
        // Lua execution and server atomicity require a real isolated Redis; inspect dispatch only here.
        service.take("cache", "consume");
        verify(template).execute(argThat((RedisScript<?> script) -> script.getScriptAsString().contains("redis.call('GET'")
                && script.getScriptAsString().contains("redis.call('DEL'")), eq(Collections.singletonList("cache::consume")));
        checks++;
        var serializer = new JdkSerializationRedisSerializer();
        var original = new ArrayList<>(List.of(new ArrayList<>(List.of("before"))));
        byte[] stored = serializer.serialize(original);
        original.getFirst().add("after-put");
        var first = (List<List<String>>) serializer.deserialize(stored);
        check(first.getFirst().size() == 1, "serialized nested object independent of writer");
        first.getFirst().add("after-get");
        var second = (List<List<String>>) serializer.deserialize(stored);
        check(second.getFirst().size() == 1 && second.getFirst() instanceof ArrayList,
                "nested value and concrete type survive round trip");
        System.out.println("PASS Redis client compatibility: " + checks + " checks (mock transport)");
    }
}
