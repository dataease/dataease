package io.dataease.listener;

import io.dataease.commons.utils.LogUtil;
import io.dataease.service.CleaningRebotService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class CleaningRobotListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private CleaningRebotService cleaningRebotService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            cleaningRebotService.execute();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }

    }
}
