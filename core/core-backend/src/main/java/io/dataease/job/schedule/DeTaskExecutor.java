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

    protected static final String IS_TEMP_TASK = "isTempTask";

    @Resource
    private ScheduleManager scheduleManager;

    @XpackInteract(value = "xpackTaskExecutor", replace = true)
    public boolean execute(Long taskId) {
        return false;
    }

    @XpackInteract(value = "xpackTaskExecutor", replace = true)
    public boolean executeTemplate(Long taskId) {
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
        jobDataMap.put(IS_TEMP_TASK, false);
        Date end = null;
        if (ObjectUtils.isNotEmpty(endTime)) end = new Date(endTime);
        scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, DeXpackScheduleJob.class, cron, new Date(startTime), end, jobDataMap);
    }

    public void fireNow(Long taskId) throws Exception{
        String key = taskId.toString();
        JobKey jobKey = new JobKey(key, key);
        scheduleManager.fireNow(jobKey);
    }

    public void addTempTask(Long taskId, Long startTime) {
        String key = taskId.toString();
        JobKey jobKey = new JobKey(key, key);
        TriggerKey triggerKey = new TriggerKey(key, key);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(IS_TEMP_TASK, true);
        String cron = CronUtils.cron();
        jobDataMap.put("taskId", taskId);
        scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, DeXpackScheduleJob.class, cron, new Date(startTime), null, jobDataMap);
    }

    public void removeTask(Long taskId) {
        String key = taskId.toString();
        JobKey jobKey = new JobKey(key);
        TriggerKey triggerKey = new TriggerKey(key);
        scheduleManager.removeJob(jobKey, triggerKey);
    }
}
