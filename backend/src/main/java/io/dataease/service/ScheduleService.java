package io.dataease.service;

import io.dataease.base.domain.DatasetTableTask;
import io.dataease.job.sechedule.ScheduleManager;
import io.dataease.job.sechedule.TestJob;
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
        if (StringUtils.equalsIgnoreCase(datasetTableTask.getRate(), "0")) {
            scheduleManager.addOrUpdateSingleJob(new JobKey(datasetTableTask.getId(), datasetTableTask.getTableId()),
                    new TriggerKey(datasetTableTask.getId(), datasetTableTask.getTableId()),
                    TestJob.class,//TODO
                    new Date(datasetTableTask.getStartTime()));
        } else if (StringUtils.equalsIgnoreCase(datasetTableTask.getRate(), "1")) {
            Date endTime;
            if (datasetTableTask.getEndTime() == null || datasetTableTask.getEndTime() == 0) {
                endTime = null;
            } else {
                endTime = new Date(datasetTableTask.getEndTime());
            }

            scheduleManager.addOrUpdateCronJob(new JobKey(datasetTableTask.getId(), datasetTableTask.getTableId()),
                    new TriggerKey(datasetTableTask.getId(), datasetTableTask.getTableId()),
                    TestJob.class,// TODO
                    datasetTableTask.getCron(), new Date(datasetTableTask.getStartTime()), endTime);
        }
    }

    public void deleteSchedule(DatasetTableTask datasetTableTask) {
        scheduleManager.removeJob(new JobKey(datasetTableTask.getId(), datasetTableTask.getTableId()), new TriggerKey(datasetTableTask.getId(), datasetTableTask.getTableId()));
    }
}
