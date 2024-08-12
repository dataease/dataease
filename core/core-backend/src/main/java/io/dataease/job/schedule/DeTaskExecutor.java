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
import java.util.Map;

@Component("deTaskExecutor")
public class DeTaskExecutor {

    protected static final String IS_TEMP_TASK = "isTempTask";
    protected static final String IS_RETRY_TASK = "isRetryTask";

    private static final String JOB_GROUP = "REPORT_TASK";
    private static final String RETRY_JOB_GROUP = "RETRY_REPORT_TASK";
    private static final String TEMP_JOB_GROUP = "TEMP_REPORT_TASK";

    private static final String THRESHOLD_JOB_GROUP = "THRESHOLD_TASK";

    @Resource
    private ScheduleManager scheduleManager;

    @XpackInteract(value = "xpackTaskExecutor", replace = true)
    public boolean execute(Map<String, Object> taskData) {
        return false;
    }

    @XpackInteract(value = "xpackTaskExecutor", replace = true)
    public void init() {
    }

    public void addThresholdTask(Long taskId, String cron, Long startTime, Long endTime) {
        String key = taskId.toString();
        JobKey jobKey = new JobKey(key, THRESHOLD_JOB_GROUP);
        TriggerKey triggerKey = new TriggerKey(key, THRESHOLD_JOB_GROUP);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("taskId", taskId);
        jobDataMap.put("threshold", taskId);
        Date end = null;
        if (CronUtils.taskExpire(endTime)) {
            return;
        }
        if (ObjectUtils.isNotEmpty(endTime)) end = new Date(endTime);
        scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, DeXpackScheduleJob.class, cron, new Date(startTime), end, jobDataMap);
    }

    public void addOrUpdateTask(Long taskId, String cron, Long startTime, Long endTime) {
        if (CronUtils.taskExpire(endTime)) {
            return;
        }
        String key = taskId.toString();
        JobKey jobKey = new JobKey(key, JOB_GROUP);
        TriggerKey triggerKey = new TriggerKey(key, JOB_GROUP);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("taskId", taskId);
        jobDataMap.put(IS_TEMP_TASK, false);
        Date end = null;
        if (ObjectUtils.isNotEmpty(endTime)) end = new Date(endTime);
        scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, DeXpackScheduleJob.class, cron, new Date(startTime), end, jobDataMap);
    }

    public void addRetryTask(Long taskId, Integer retryLimit, Integer retryInterval, Object retryParam) {
        long saltTime = 3000L;
        long interval = retryInterval == null ? 5L : retryInterval;
        long intervalMill = interval * 60000L;
        long now = System.currentTimeMillis();
        String cron = "0 */" + interval + " * * * ?";
        long endTime = (retryLimit + 1) * intervalMill + now - saltTime;
        String key = taskId.toString();
        if (CronUtils.taskExpire(endTime)) {
            return;
        }
        JobKey jobKey = new JobKey(key, RETRY_JOB_GROUP);
        TriggerKey triggerKey = new TriggerKey(key, RETRY_JOB_GROUP);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("taskId", taskId);
        jobDataMap.put(IS_RETRY_TASK, true);
        jobDataMap.put("retryParam", retryParam);
        Date end = null;
        if (ObjectUtils.isNotEmpty(endTime)) end = new Date(endTime);
        scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, DeXpackScheduleJob.class, cron, new Date(now), end, jobDataMap);
    }

    public boolean fireNow(Long taskId) throws Exception {
        String key = taskId.toString();
        JobKey jobKey = new JobKey(key, JOB_GROUP);
        if (scheduleManager.exist(jobKey)) {
            scheduleManager.fireNow(jobKey);
            return true;
        }
        return false;
    }

    public void addTempTask(Long taskId, Long startTime) {
        String key = taskId.toString();
        JobKey jobKey = new JobKey(key, TEMP_JOB_GROUP);
        TriggerKey triggerKey = new TriggerKey(key, TEMP_JOB_GROUP);
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(IS_TEMP_TASK, true);
        String cron = CronUtils.tempCron();
        jobDataMap.put("taskId", taskId);
        scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, DeXpackScheduleJob.class, cron, new Date(startTime), null, jobDataMap);
    }

    public void removeTask(Long taskId, boolean isTemp) {
        String key = taskId.toString();
        JobKey jobKey = new JobKey(key, isTemp ? TEMP_JOB_GROUP : JOB_GROUP);
        TriggerKey triggerKey = new TriggerKey(key, isTemp ? TEMP_JOB_GROUP : JOB_GROUP);
        scheduleManager.removeJob(jobKey, triggerKey);
    }

    public void removeRetryTask(Long taskId) {
        String key = taskId.toString();
        JobKey jobKey = new JobKey(key, RETRY_JOB_GROUP);
        TriggerKey triggerKey = new TriggerKey(key, RETRY_JOB_GROUP);
        scheduleManager.removeJob(jobKey, triggerKey);
    }

    public void removeThresholdTask(Long taskId) {
        String key = taskId.toString();
        JobKey jobKey = new JobKey(key, THRESHOLD_JOB_GROUP);
        TriggerKey triggerKey = new TriggerKey(key, THRESHOLD_JOB_GROUP);
        scheduleManager.removeJob(jobKey, triggerKey);
    }

    public void clearRetryTask() throws Exception {
        scheduleManager.clearByGroup(RETRY_JOB_GROUP);
    }
}
