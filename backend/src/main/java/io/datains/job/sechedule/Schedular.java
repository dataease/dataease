package io.datains.job.sechedule;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.datains.service.datasource.DatasourceService;
import io.datains.service.dataset.DataSetTableService;
import io.datains.service.kettle.KettleService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class Schedular {
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private KettleService kettleService;

    @QuartzScheduled(cron = "0 0/3 * * * ?")
    public void updateDatasetTableStatus() {
        dataSetTableService.updateDatasetTableStatus();
    }

    @QuartzScheduled(cron = "0 0/30 * * * ?")
    public void updateDatasourceStatus() {
        datasourceService.updateDatasourceStatus();
    }

    @QuartzScheduled(cron = "0 0/30 * * * ?")
    public void updateKettleStatus() {
        kettleService.updateKettleStatus();
    }

}
