package io.dataease.listener;

import io.dataease.base.domain.DatasetTableTask;
import io.dataease.datasource.service.DatasourceService;
import io.dataease.service.ScheduleService;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.dataset.DataSetTableTaskService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Order(value = 1)
public class DataSourceInitStartListener implements ApplicationListener<ApplicationReadyEvent> {
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private DataSetTableService dataSetTableService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        datasourceService.initAllDataSourceConnectionPool();
        dataSetTableService.updateDatasetTableStatus();
    }
}
