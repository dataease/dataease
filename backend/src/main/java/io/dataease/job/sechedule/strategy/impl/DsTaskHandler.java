package io.dataease.job.sechedule.strategy.impl;

import com.google.gson.Gson;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.LogUtil;
import io.dataease.dto.TaskInstance;
import io.dataease.ext.ExtTaskInstanceMapper;
import io.dataease.job.sechedule.ScheduleManager;
import io.dataease.job.sechedule.strategy.TaskHandler;
import io.dataease.plugins.common.entity.GlobalTaskEntity;

import io.dataease.service.datasource.DatasourceService;
import org.quartz.*;

import org.springframework.stereotype.Service;

import java.util.Date;


@Service("dsTaskHandler")
public class DsTaskHandler extends TaskHandler implements Job {

    private static final String RUNNING = "RUNNING";
    private static final String SUCCESS = "SUCCESS";
    private static final String ERROR = "ERROR";

    @Override
    protected JobDataMap jobDataMap(GlobalTaskEntity taskEntity) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("taskEntity", taskEntity);
        return jobDataMap;
    }

    @Override
    public void resetRunningInstance(Long taskId) {

    }

    @Override
    protected Boolean taskIsRunning(Long taskId) {
        return null;
    }

    @Override
    public void addTask(ScheduleManager scheduleManager, GlobalTaskEntity taskEntity) throws Exception {
        JobKey jobKey = new JobKey(taskEntity.getJobKey());
        TriggerKey triggerKey = new TriggerKey(taskEntity.getJobKey());
        Date start = new Date(taskEntity.getStartTime());
        Date end = null;
        Class<? extends TaskHandler> executor = this.getClass();
        scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, executor, taskEntity.getCron(), start, end, jobDataMap(taskEntity));
    }

    @Override
    public void removeTask(ScheduleManager scheduleManager, GlobalTaskEntity taskEntity) {
        JobKey jobKey = new JobKey(taskEntity.getJobKey());
        TriggerKey triggerKey = new TriggerKey(taskEntity.getJobKey());
        scheduleManager.removeJob(jobKey, triggerKey);
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        GlobalTaskEntity taskEntity = (GlobalTaskEntity) jobDataMap.get("taskEntity");
        taskEntity.getJobKey();

        if (isRunning(taskEntity.getJobKey())) {
            LogUtil.info("Skip synchronization task: {} ,due to task status is {}", taskEntity.getJobKey(), "running");
            return;
        }

        LogUtil.info("start check datasource status...");
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskId("Datasource_check_status");
        taskInstance.setExecuteTime(System.currentTimeMillis());
        taskInstance.setFinishTime(null);
        taskInstance.setStatus(RUNNING);
        taskInstance.setQrtzInstance(context.getFireInstanceId());
        ExtTaskInstanceMapper extTaskInstanceMapper = CommonBeanFactory.getBean(ExtTaskInstanceMapper.class);
        extTaskInstanceMapper.update(taskInstance);

        DatasourceService datasourceService = CommonBeanFactory.getBean(DatasourceService.class);
        datasourceService.updateDatasourceStatus();

        taskInstance.setFinishTime(System.currentTimeMillis());
        taskInstance.setStatus(SUCCESS);
        extTaskInstanceMapper.update(taskInstance);
        LogUtil.info("end check datasource status.");

    }


    private Boolean isRunning(String taskId) {
        ExtTaskInstanceMapper  extTaskInstanceMapper = CommonBeanFactory.getBean(ExtTaskInstanceMapper.class);
        return extTaskInstanceMapper.runningCount(taskId) > 0;
    }

}
