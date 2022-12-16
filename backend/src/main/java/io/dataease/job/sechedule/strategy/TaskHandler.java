package io.dataease.job.sechedule.strategy;

import io.dataease.commons.utils.CronUtils;
import io.dataease.job.sechedule.ScheduleManager;
import io.dataease.plugins.common.entity.GlobalTaskEntity;
import org.apache.commons.lang3.ObjectUtils;
import org.quartz.*;
import org.springframework.beans.factory.InitializingBean;


import java.util.Date;

public abstract class TaskHandler implements InitializingBean {

    protected static final String IS_TEMP_TASK = "isTempTask";

    public void addTask(ScheduleManager scheduleManager, GlobalTaskEntity taskEntity) throws Exception {
        // 1。首先看看是否过期
        Long endTime = taskEntity.getEndTime();
        removeTask(scheduleManager, taskEntity);
        if (CronUtils.taskExpire(endTime)) { // 过期了就删除任务
            return;
        }
        if (!taskEntity.getStatus()) return;
        JobKey jobKey = new JobKey(taskEntity.getTaskId().toString());
        TriggerKey triggerKey = new TriggerKey(taskEntity.getTaskId().toString());
        Date start = new Date(taskEntity.getStartTime());
        Date end = null;
        if (ObjectUtils.isNotEmpty(taskEntity.getEndTime())) {
            end = new Date(taskEntity.getEndTime());
        }
        Class<? extends TaskHandler> executor = this.getClass();
        String cron = CronUtils.cron(taskEntity);
        scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, executor, cron, start, end, jobDataMap(taskEntity));
    }

    public void addTempTask(ScheduleManager scheduleManager, GlobalTaskEntity taskEntity) throws Exception {
        removeTask(scheduleManager, taskEntity);
        JobKey jobKey = new JobKey(taskEntity.getTaskId().toString());
        TriggerKey triggerKey = new TriggerKey(taskEntity.getTaskId().toString());
        Date start = new Date(taskEntity.getStartTime());
        Class<? extends TaskHandler> executor = this.getClass();
        String cron = CronUtils.cron();
        JobDataMap jobDataMap = jobDataMap(taskEntity);
        jobDataMap.put(IS_TEMP_TASK, true);
        scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, executor, cron, start, null, jobDataMap);
    }

    protected abstract JobDataMap jobDataMap(GlobalTaskEntity taskEntity);


    public abstract void resetRunningInstance(Long taskId);


    public void removeTask(ScheduleManager scheduleManager, GlobalTaskEntity taskEntity) {
        JobKey jobKey = new JobKey(taskEntity.getTaskId().toString());
        TriggerKey triggerKey = new TriggerKey(taskEntity.getTaskId().toString());
        scheduleManager.removeJob(jobKey, triggerKey);
    }

    public void executeTask(ScheduleManager scheduleManager, GlobalTaskEntity taskEntity) throws Exception {
        JobKey jobKey = new JobKey(taskEntity.getTaskId().toString());
        scheduleManager.fireNow(jobKey);
    }


    protected abstract Boolean taskIsRunning(Long taskId);

    @Override
    public void afterPropertiesSet() throws Exception {
        String beanName = null;
        String className = this.getClass().getName();
        className = className.substring(className.lastIndexOf(".") + 1);
        if (className.length() > 1) {
            beanName = className.substring(0, 1).toLowerCase() + className.substring(1);
        } else {
            beanName = className.toLowerCase();
        }
        TaskStrategyFactory.register(beanName, this);
    }

}
