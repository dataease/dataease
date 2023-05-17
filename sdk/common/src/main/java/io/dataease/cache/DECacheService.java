package io.dataease.cache;

import java.util.concurrent.TimeUnit;

public interface DECacheService<T> {


    void put(String cacheName, String key, T value, Long expTime, TimeUnit unit);

    T get(String cacheName, String key);

    boolean cacheExist(String cacheName);

    boolean keyExist(String cacheName, String key);

    void keyRemove(String cacheName, String key);
}
