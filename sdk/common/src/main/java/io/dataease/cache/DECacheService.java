package io.dataease.cache;

import java.util.concurrent.TimeUnit;

public interface DECacheService<T> {

    void create(String cacheName, Long expTime, TimeUnit unit);

    void put(String cacheName, String key, T value);

    T get(String cacheName, String key);

    boolean cacheExist(String cacheName);

    boolean keyExist(String cacheName, String key);
}
