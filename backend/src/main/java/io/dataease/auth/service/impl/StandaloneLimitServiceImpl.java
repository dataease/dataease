package io.dataease.auth.service.impl;


import com.google.common.util.concurrent.RateLimiter;
import io.dataease.auth.service.DeLimitService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@Service
public class StandaloneLimitServiceImpl implements DeLimitService {

    private static ConcurrentHashMap<String, RateLimiter> RATE_LIMITER = new ConcurrentHashMap<>();

    @Override
    public Boolean checkRestricted(String key, long max, long timeout, TimeUnit timeUnit) {
        RateLimiter rateLimiter = null;
        if (!RATE_LIMITER.containsKey(key)) {
            RATE_LIMITER.put(key, RateLimiter.create(max));
        }
        rateLimiter = RATE_LIMITER.get(key);
        return !rateLimiter.tryAcquire(timeout, timeUnit);
    }
}
