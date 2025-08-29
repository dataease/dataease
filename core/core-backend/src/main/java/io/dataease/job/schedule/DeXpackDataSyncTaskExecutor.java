package io.dataease.job.schedule;

import io.dataease.license.config.XpackInteract;
import jakarta.annotation.Resource;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component("deXpackDataSyncTaskExecutor")
public class DeXpackDataSyncTaskExecutor {

    private static final String SYNC_JOB_GROUP = "SYNC_TASK";

    @Resource
    private ScheduleManager scheduleManager;

    @XpackInteract(value = "dataSyncTaskExecutor", replace = true)
    public boolean execute(Map<String, Object> taskData) {
        return false;
    }

    @XpackInteract(value = "dataSyncTaskExecutor", replace = true)
    public void init() {
    }

    /**
     * 添加或更新定时任务
     *
     * @param taskId    任务ID
     * @param cron      cron表达式
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param jobData   任务数据
     */
    public void addOrUpdateSyncTask(String taskId, String cron, Long startTime, Long endTime, Map<String, Object> jobData) {
        JobKey jobKey = new JobKey(taskId, SYNC_JOB_GROUP);
        TriggerKey triggerKey = new TriggerKey(taskId, SYNC_JOB_GROUP);

        // 准备JobDataMap
        JobDataMap jobDataMap = jobData != null ? new JobDataMap(jobData) : new JobDataMap();
        jobDataMap.put("taskId", taskId);
        // 调度任务
        jobDataMap.put("cron", cron);
        jobDataMap.put("startTime", startTime);
        jobDataMap.put("endTime", endTime);
        jobDataMap.put("executeOnce", Boolean.FALSE);
        scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, DeXpackDataSyncTaskScheduleJob.class,
                cron, new Date(startTime), endTime != null ? new Date(endTime) : null, jobDataMap);
    }

    public void removeSyncTask(String taskId) {
        JobKey jobKey = new JobKey(taskId, SYNC_JOB_GROUP);
        TriggerKey triggerKey = new TriggerKey(taskId, SYNC_JOB_GROUP);
        if (scheduleManager.exist(jobKey)) {
            scheduleManager.removeJob(jobKey, triggerKey);
        }
    }

}
