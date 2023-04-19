package io.dataease.job.sechedule;


import io.dataease.datasource.server.DatasourceServer;
import io.dataease.utils.CommonBeanFactory;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
public class ExtractDataJob extends DeScheduleJob{
    private DatasourceServer datasourceServer;

    public ExtractDataJob() {
        datasourceServer = (DatasourceServer) CommonBeanFactory.getBean(DatasourceServer.class);
    }

    @Override
    void businessExecute(JobExecutionContext context) {
        datasourceServer.extractData(datasetTableId, taskId, updateType, context);
    }

}
