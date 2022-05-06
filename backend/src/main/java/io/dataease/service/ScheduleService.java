package io.dataease.service;

import io.dataease.commons.constants.ScheduleType;
import io.dataease.job.sechedule.ExtractDataJob;
import io.dataease.job.sechedule.ScheduleManager;
import io.dataease.job.sechedule.strategy.TaskHandler;
import io.dataease.job.sechedule.strategy.TaskStrategyFactory;
import io.dataease.plugins.common.base.domain.DatasetTableTask;
import io.dataease.plugins.common.entity.GlobalTaskEntity;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Author gin
 * @Date 2021/3/5 2:38 下午
 */
@Service
public class ScheduleService {
    @Resource
    private ScheduleManager scheduleManager;

    public void addSchedule(DatasetTableTask datasetTableTask) throws Exception {
        if (StringUtils.equalsIgnoreCase(datasetTableTask.getRate(), ScheduleType.SIMPLE.toString())) {
            scheduleManager.addOrUpdateSingleJob(new JobKey(datasetTableTask.getId(), datasetTableTask.getTableId()),
                    new TriggerKey(datasetTableTask.getId(), datasetTableTask.getTableId()),
                    ExtractDataJob.class,
                    new Date(datasetTableTask.getStartTime()),
                    scheduleManager.getDefaultJobDataMap(datasetTableTask.getTableId(), datasetTableTask.getCron(), datasetTableTask.getId(), datasetTableTask.getType()));
        } else {
            Date endTime;
            if (StringUtils.equalsIgnoreCase(datasetTableTask.getEnd(), "1")) {
                if (datasetTableTask.getEndTime() == null || datasetTableTask.getEndTime() == 0) {
                    endTime = null;
                } else {
                    endTime = new Date(datasetTableTask.getEndTime());
                    if (endTime.before(new Date())) {
                        deleteSchedule(datasetTableTask);
                        return;
                    }
                }
            } else {
                endTime = null;
            }

            scheduleManager.addOrUpdateCronJob(new JobKey(datasetTableTask.getId(), datasetTableTask.getTableId()),
                    new TriggerKey(datasetTableTask.getId(), datasetTableTask.getTableId()),
                    ExtractDataJob.class,
                    datasetTableTask.getCron(), new Date(datasetTableTask.getStartTime()), endTime,
                    scheduleManager.getDefaultJobDataMap(datasetTableTask.getTableId(), datasetTableTask.getCron(), datasetTableTask.getId(), datasetTableTask.getType()));
        }
    }

    public void deleteSchedule(DatasetTableTask datasetTableTask) {
        scheduleManager.removeJob(new JobKey(datasetTableTask.getId(), datasetTableTask.getTableId()), new TriggerKey(datasetTableTask.getId(), datasetTableTask.getTableId()));
    }

    public void fireNow(DatasetTableTask datasetTableTask) throws Exception {
        scheduleManager.fireNow(datasetTableTask.getId(), datasetTableTask.getTableId());
    }

    public void addSchedule(GlobalTaskEntity task) throws Exception {
        TaskHandler taskHandler = TaskStrategyFactory.getInvokeStrategy(task.getTaskType());
        taskHandler.addTask(scheduleManager, task);
    }

    public void deleteSchedule(GlobalTaskEntity task) {
        TaskHandler taskHandler = TaskStrategyFactory.getInvokeStrategy(task.getTaskType());
        taskHandler.removeTask(scheduleManager, task);
    }

    public void fireNow(GlobalTaskEntity task) throws Exception {
        TaskHandler taskHandler = TaskStrategyFactory.getInvokeStrategy(task.getTaskType());
        taskHandler.executeTask(scheduleManager, task);
    }


}
