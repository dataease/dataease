package io.dataease.datasource.manage;

import io.dataease.job.schedule.CheckDsStatusJob;
import io.dataease.job.schedule.ScheduleManager;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTask;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import jakarta.annotation.Resource;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 数据源状态检查调度管理：根据系统参数注册/更新 check_status 定时任务。
 * 不依赖任何 *Server。
 */
@Component
public class DatasourceScheduleManage {

    @Resource
    private ScheduleManager scheduleManager;

    public void addJob(List<CoreSysSetting> sysSettings) {
        String type = "minute";
        String interval = "30";
        for (CoreSysSetting sysSetting : sysSettings) {
            if (sysSetting.getPkey().equalsIgnoreCase("basic.dsExecuteTime")) {
                type = sysSetting.getPval();
            }
            if (sysSetting.getPkey().equalsIgnoreCase("basic.dsIntervalTime")) {
                interval = sysSetting.getPval();
            }
        }
        String cron = "";
        switch (type) {
            case "hour":
                cron = "0 0 0/hour *  * ? *".replace("hour", interval.toString());
                break;
            default:
                cron = "0 0/minute * *  * ? *".replace("minute", interval.toString());
        }
        scheduleManager.addOrUpdateCronJob(new JobKey("Datasource", "check_status"), new TriggerKey("Datasource", "check_status"), CheckDsStatusJob.class, cron, new Date(System.currentTimeMillis()), null, new JobDataMap());
    }

    public void deleteSchedule(CoreDatasourceTask datasourceTask) {
        scheduleManager.removeJob(new JobKey(datasourceTask.getId().toString(), datasourceTask.getDsId().toString()), new TriggerKey(datasourceTask.getId().toString(), datasourceTask.getDsId().toString()));
    }
}
