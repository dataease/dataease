package io.dataease.job.sechedule.strategy.impl;

import io.dataease.auth.service.AuthUserService;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.CronUtils;
import io.dataease.service.datafill.DataFillTaskService;
import io.dataease.job.sechedule.ScheduleManager;
import io.dataease.job.sechedule.strategy.TaskHandler;
import io.dataease.plugins.common.base.domain.DataFillTaskWithBLOBs;
import io.dataease.plugins.common.entity.GlobalTaskEntity;
import io.dataease.plugins.common.util.SpringContextUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Service("dataFillTaskHandler")
public class DataFillTaskHandler extends TaskHandler implements Job {


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
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 插件没有加载 空转
        if (!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded())
            return;

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        GlobalTaskEntity taskEntity = (GlobalTaskEntity) jobDataMap.get("taskEntity");

        ScheduleManager scheduleManager = SpringContextUtil.getBean(ScheduleManager.class);

        DataFillTaskService dataFillTaskService = SpringContextUtil.getBean(DataFillTaskService.class);

        DataFillTaskWithBLOBs task = dataFillTaskService.getTaskById(taskEntity.getTaskId());

        try {
            dataFillTaskService.createUserTasks(task, null);

            if (task.getRateType() == -1) {
                //一次性任务执行完之后需要修改为停止
                dataFillTaskService.finishTask(task.getId());
                removeTask(scheduleManager, taskEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void addTask(ScheduleManager scheduleManager, GlobalTaskEntity taskEntity) throws Exception {

        DataFillTaskService dataFillTaskService = SpringContextUtil.getBean(DataFillTaskService.class);
        // 1。首先看看是否过期
        Long endTime = taskEntity.getEndTime();
        removeTask(scheduleManager, taskEntity);
        if (CronUtils.taskExpire(endTime)) { // 过期了就删除任务
            dataFillTaskService.finishTask(taskEntity.getTaskId()); //状态置为已停止
            return;
        }
        if (!taskEntity.getStatus()) return;
        JobKey jobKey = new JobKey("DATA_FILL_TASK_" + taskEntity.getTaskId().toString());
        TriggerKey triggerKey = new TriggerKey("DATA_FILL_TASK_" + taskEntity.getTaskId());
        Date start = new Date(taskEntity.getStartTime());
        Date end = null;
        if (ObjectUtils.isNotEmpty(taskEntity.getEndTime())) {
            end = new Date(taskEntity.getEndTime());
        }
        Class<? extends TaskHandler> executor = this.getClass();
        String cron = cron(taskEntity);
        if (taskEntity.getRateType() == -1) {
            long currentTriggerTime = System.currentTimeMillis() + 30 * 1000; //一次性的任务，确保下发成功，增加一点延时
            if (taskEntity.getStartTime() >= currentTriggerTime) {
                currentTriggerTime = taskEntity.getStartTime();
                start = new Date(taskEntity.getStartTime() - 30 * 1000);
            }
            cron = cron(new Date(currentTriggerTime));
        }
        scheduleManager.addOrUpdateCronJob(jobKey, triggerKey, executor, cron, start, end, jobDataMap(taskEntity));
    }

    private static String cron(Date date) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);

        return instance.get(Calendar.SECOND) + " " +
                instance.get(Calendar.MINUTE) + " " +
                instance.get(Calendar.HOUR_OF_DAY) + " " +
                instance.get(Calendar.DAY_OF_MONTH) + " " +
                (instance.get(Calendar.MONTH) + 1) + " ? " +
                instance.get(Calendar.YEAR);
    }

    private static String cron(GlobalTaskEntity taskEntity) {
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
        java.util.Calendar instance = java.util.Calendar.getInstance();
        instance.setTime(date);

        if (taskEntity.getRateType() == 0) {
            return instance.get(java.util.Calendar.SECOND) + " " +
                    instance.get(java.util.Calendar.MINUTE) + " " +
                    instance.get(java.util.Calendar.HOUR_OF_DAY) + " * * ?";
        }
        if (taskEntity.getRateType() == 1) {
            return instance.get(java.util.Calendar.SECOND) + " " +
                    instance.get(java.util.Calendar.MINUTE) + " " +
                    instance.get(java.util.Calendar.HOUR_OF_DAY) + " ? * " +
                    getWeekDayStr(instance.get(Calendar.DATE)); //这里是拿日当作周来保存的
        }
        if (taskEntity.getRateType() == 2) {
            return instance.get(java.util.Calendar.SECOND) + " " +
                    instance.get(java.util.Calendar.MINUTE) + " " +
                    instance.get(java.util.Calendar.HOUR_OF_DAY) + " " +
                    instance.get(Calendar.DATE) + " * ?";
        }

        return null;
    }

    private static String getWeekDayStr(int day) {
        switch (day) {
            case 1:
                return "MON";
            case 2:
                return "TUE";
            case 3:
                return "WED";
            case 4:
                return "THU";
            case 5:
                return "FRI";
            case 6:
                return "SAT";
            case 7:
                return "SUN";
        }
        return "*";
    }

    @Override
    public void removeTask(ScheduleManager scheduleManager, GlobalTaskEntity taskEntity) {
        JobKey jobKey = new JobKey("DATA_FILL_TASK_" + taskEntity.getTaskId().toString());
        TriggerKey triggerKey = new TriggerKey("DATA_FILL_TASK_" + taskEntity.getTaskId());
        scheduleManager.removeJob(jobKey, triggerKey);
    }

    @Override
    public void executeTask(ScheduleManager scheduleManager, GlobalTaskEntity taskEntity) throws Exception {
        JobKey jobKey = new JobKey("DATA_FILL_TASK_" + taskEntity.getTaskId().toString());
        scheduleManager.fireNow(jobKey);
    }
}
