package io.dataease.job.schedule;


import io.dataease.datasource.server.DatasourceServer;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
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
        try {
            // 兼容同步管理
            // datasourceServer.updateDatasourceStatus() 前补 LicenseUtil.validate()，让 Quartz 线程先初始化 license ThreadLocal
            LicenseUtil.validate();
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
        }
        datasourceServer.updateDatasourceStatus();
    }

}
