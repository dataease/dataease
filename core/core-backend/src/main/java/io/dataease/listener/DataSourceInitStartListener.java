package io.dataease.listener;

import io.dataease.datasource.server.DatasourceServer;
import io.dataease.datasource.server.EngineServer;
import jakarta.annotation.Resource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@Order(value = 2)
public class DataSourceInitStartListener implements ApplicationListener<ApplicationReadyEvent> {
    @Resource
    private DatasourceServer datasourceServer;

    @Resource
    private EngineServer engineServer;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        engineServer.initSimpleEngine();
        datasourceServer.updateDemoDs();
    }


}
