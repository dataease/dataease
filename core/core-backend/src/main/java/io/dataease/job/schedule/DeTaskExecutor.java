package io.dataease.job.schedule;

import io.dataease.commons.utils.CronUtils;
import io.dataease.license.config.XpackInteract;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class DeTaskExecutor {

    @Resource
    private ScheduleManager scheduleManager;

    @XpackInteract(value = "xpackTaskExecutor", replace = true)
    public boolean execute(Long taskId) {
        return false;
    }

    public void addOrUpdateTask(Long taskId, String cron, Long startTime, Long endTime) {
        if (CronUtils.taskExpire(endTime)) {
            return;
        }
        String key = taskId.toString();
        JobKey jobKey = new JobKey(key, key);
        TriggerKey triggerKey = new TriggerKey(key, key);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("taskId", taskId);
        Date end = null;
        if (ObjectUtils.isNotEmpty(endTime)) end = new Date(endTime);
        scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, DeXpackScheduleJob.class, cron, new Date(startTime), end, jobDataMap);
    }

    public void removeTask(Long taskId) {
        String key = taskId.toString();
        JobKey jobKey = new JobKey(key);
        TriggerKey triggerKey = new TriggerKey(key);
        scheduleManager.removeJob(jobKey, triggerKey);
    }
}
