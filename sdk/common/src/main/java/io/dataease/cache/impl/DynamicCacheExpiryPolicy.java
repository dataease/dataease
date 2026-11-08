package io.dataease.cache.impl;

import javax.cache.Cache;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;

/** Per-write expiry for dynamically created caches only; values remain unwrapped. */
final class DynamicCacheExpiryPolicy implements ExpiryPolicy {

    private static final ThreadLocal<Duration> WRITE_DURATION = new ThreadLocal<>();

    private final Duration defaultDuration;

    private DynamicCacheExpiryPolicy(Duration defaultDuration) {
        this.defaultDuration = defaultDuration;
    }

    static void put(Cache<String, Object> cache, String key, Object value, Duration duration) {
        Duration previous = WRITE_DURATION.get();
        WRITE_DURATION.set(duration);
        try {
            // The local JCache provider evaluates creation/update expiry within put.
            cache.put(key, value);
        } finally {
            if (previous == null) {
                WRITE_DURATION.remove();
            } else {
                WRITE_DURATION.set(previous);
            }
        }
    }

    @Override
    public Duration getExpiryForCreation() {
        Duration duration = WRITE_DURATION.get();
        return duration == null ? defaultDuration : duration;
    }

    @Override
    public Duration getExpiryForAccess() {
        return null;
    }

    @Override
    public Duration getExpiryForUpdate() {
        return getExpiryForCreation();
    }

    static final class Factory implements javax.cache.configuration.Factory<ExpiryPolicy> {

        private static final long serialVersionUID = 1L;

        private final Duration defaultDuration;

        Factory(Duration defaultDuration) {
            this.defaultDuration = defaultDuration;
        }

        Duration getDefaultDuration() {
            return defaultDuration;
        }

        @Override
        public ExpiryPolicy create() {
            return new DynamicCacheExpiryPolicy(defaultDuration);
        }
    }
}
