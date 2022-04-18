package io.datains.listener;

import io.datains.service.datasource.DatasourceService;
import io.datains.service.dataset.DataSetTableService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

@Component
@Order(value = 2)
public class DataSourceInitStartListener implements ApplicationListener<ApplicationReadyEvent> {
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private DataSetTableService dataSetTableService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        datasourceService.initAllDataSourceConnectionPool();
//        dataSetTableService.updateDatasetTableStatus();
    }


}
