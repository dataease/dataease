package io.dataease.job.schedule;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.exportCenter.manage.ExportCenterManage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;


@Component
public class Schedular {

    @Resource
    private DatasourceServer datasourceServer;
    @Resource
    private ExportCenterManage exportCenterManage;

    @QuartzScheduled(cron = "0 0/3 * * * ?")
    public void updateStopJobStatus() {
        datasourceServer.updateStopJobStatus();
    }

    @QuartzScheduled(cron = "0 0/3 * * * ?")
    public void recoverOrphanExportTasks() {
        exportCenterManage.recoverOrphanExportTasks();
    }

}
