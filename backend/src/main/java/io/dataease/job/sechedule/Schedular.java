package io.dataease.job.sechedule;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.dataease.datasource.service.DatasourceService;
import io.dataease.service.dataset.DataSetTableService;

import javax.annotation.Resource;

public class Schedular {
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private DatasourceService datasourceService;

    @QuartzScheduled(cron = "0 0/3 * * * ?")
    public void updateDatasetTableStatus() {
        dataSetTableService.updateDatasetTableStatus();
    }

    @QuartzScheduled(cron = "0 0/30 * * * ?")
    public void updateDatasourceStatus() {
        datasourceService.updateDatasourceStatus();
    }

}
