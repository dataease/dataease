package io.dataease.listener;

import io.dataease.map.service.MapTransferService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class GlobalMapTransferListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private MapTransferService mapTransferService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        mapTransferService.execute();
    }
}
