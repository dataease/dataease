package io.dataease.job.sechedule.strategy;

import io.dataease.job.sechedule.ScheduleManager;
import io.dataease.plugins.common.entity.GlobalTaskEntity;
import org.apache.commons.lang3.ObjectUtils;
import org.quartz.*;
import org.springframework.beans.factory.InitializingBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class TaskHandler implements InitializingBean {

    private static final String[] week = {"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};


    public void addTask(ScheduleManager scheduleManager, GlobalTaskEntity taskEntity) throws Exception {
        // 1。首先看看是否过期
        Long endTime = taskEntity.getEndTime();
        removeTask(scheduleManager, taskEntity);
        if (taskExpire(endTime)) { // 过期了就删除任务
            return;
        }
        JobKey jobKey = new JobKey(taskEntity.getTaskId().toString());
        TriggerKey triggerKey = new TriggerKey(taskEntity.getTaskId().toString());
        Date start = new Date(taskEntity.getStartTime());
        Date end = null;
        if (ObjectUtils.isNotEmpty(taskEntity.getEndTime())) {
            new Date(taskEntity.getEndTime());
        }
        Class executor = this.getClass();
        String cron = cron(taskEntity);
        scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, executor, cron, start, end, jobDataMap(taskEntity));
    }


    protected abstract JobDataMap jobDataMap(GlobalTaskEntity taskEntity);

    private String cron(GlobalTaskEntity taskEntity) {
        if (taskEntity.getRateType() == -1) {
            return taskEntity.getRateVal();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = null;
        try {
            date = sdf.parse(taskEntity.getRateVal());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);

        if (taskEntity.getRateType() == 0) {
            return
                    instance.get(Calendar.SECOND) + " " +
                            instance.get(Calendar.MINUTE) + " " +
                            instance.get(Calendar.HOUR_OF_DAY) + " * * ?";
        }
        if (taskEntity.getRateType() == 1) {
            return
                    instance.get(Calendar.SECOND) + " " +
                            instance.get(Calendar.MINUTE) + " " +
                            instance.get(Calendar.HOUR_OF_DAY) + " ? * " +
                            getDayOfWeek(instance);
        }
        if (taskEntity.getRateType() == 2) {
            return
                    instance.get(Calendar.SECOND) + " " +
                            instance.get(Calendar.MINUTE) + " " +
                            instance.get(Calendar.HOUR_OF_DAY) + " " +
                            instance.get(Calendar.DATE) + " * ?";
        }

        return null;
    }

    private String getDayOfWeek(Calendar instance) {
        int index = instance.get(Calendar.DAY_OF_WEEK) - 1;
        return week[index];
    }


    public void removeTask(ScheduleManager scheduleManager, GlobalTaskEntity taskEntity) {
        JobKey jobKey = new JobKey(taskEntity.getTaskId().toString());
        TriggerKey triggerKey = new TriggerKey(taskEntity.getTaskId().toString());
        scheduleManager.removeJob(jobKey, triggerKey);
    }

    public void executeTask(ScheduleManager scheduleManager, GlobalTaskEntity taskEntity) throws Exception {
        JobKey jobKey = new JobKey(taskEntity.getTaskId().toString());
        scheduleManager.fireNow(jobKey);
    }


    //判断任务是否过期
    public Boolean taskExpire(Long endTime) {
        if (ObjectUtils.isEmpty(endTime)) return false;
        Long now = System.currentTimeMillis();
        return now > endTime;
    }

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
