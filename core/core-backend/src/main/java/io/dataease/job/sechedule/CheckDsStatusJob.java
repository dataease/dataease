package io.dataease.job.sechedule;


import io.dataease.datasource.server.DatasourceServer;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.quartz.*;
import org.springframework.stereotype.Component;

@Component
public class CheckDsStatusJob implements Job {

    @Resource
    private DatasourceServer datasourceServer;

    public CheckDsStatusJob() {
        datasourceServer = (DatasourceServer) CommonBeanFactory.getBean(DatasourceServer.class);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LogUtil.info("Begin to check ds status...");
        datasourceServer.updateDatasourceStatus();
    }

}
