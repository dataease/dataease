package io.dataease.listener;

import io.dataease.commons.utils.LogUtil;
import net.sf.ehcache.CacheManager;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationCloseEventListener implements ApplicationListener<ContextClosedEvent> {

    @Autowired(required = false)
    CacheManager cacheManager;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {

        if (ObjectUtils.isNotEmpty(cacheManager))
            cacheManager.shutdown();
        LogUtil.info("DataEase is stopping");
    }
}
