package io.dataease.job.schedule;


import io.dataease.datasource.manage.DatasourceSyncManage;
import io.dataease.utils.CommonBeanFactory;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
public class ExtractDataJob extends DeScheduleJob {
    private DatasourceSyncManage datasourceSyncManage;

    public ExtractDataJob() {
        datasourceSyncManage = (DatasourceSyncManage) CommonBeanFactory.getBean(DatasourceSyncManage.class);
    }

    @Override
    void businessExecute(JobExecutionContext context) {
        datasourceSyncManage.extractData(datasetTableId, taskId, context);
    }

}
