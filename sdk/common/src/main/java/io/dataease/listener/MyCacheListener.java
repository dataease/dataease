package io.dataease.listener;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyCacheListener implements CacheEventListener<String, Object> {

    private static final Logger log = LoggerFactory.getLogger(MyCacheListener.class);

    public MyCacheListener() {
        log.info("MyCacheListener: init");
    }

    @Override
    public void onEvent(CacheEvent<? extends String, ?> cacheEvent) {
        log.info("'{}' : [{}] --> {}", cacheEvent.getType(), cacheEvent.getKey(), cacheEvent.getNewValue());
    }
}
