package io.dataease.cache;

import java.util.concurrent.TimeUnit;

public interface DECacheService<T> {


    void put(String cacheName, String key, T value, Long expTime, TimeUnit unit);

    T get(String cacheName, String key);

    /** Atomically retrieve and remove a value. Missing or expired entries return null. */
    default T take(String cacheName, String key) {
        throw new UnsupportedOperationException("Atomic cache consumption is not supported");
    }

    boolean cacheExist(String cacheName);

    boolean keyExist(String cacheName, String key);

    void keyRemove(String cacheName, String key);
}
