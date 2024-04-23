package io.dataease.job.schedule;

import io.dataease.utils.CommonBeanFactory;
import jakarta.annotation.Resource;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DeXpackScheduleJob implements Job {
    @Resource
    private DeTaskExecutor deTaskExecutor;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Trigger trigger = jobExecutionContext.getTrigger();
        JobKey jobKey = trigger.getJobKey();
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        Long taskId = jobDataMap.getLong("taskId");
        if (!deTaskExecutor.execute(taskId)) {
            Objects.requireNonNull(CommonBeanFactory.getBean(ScheduleManager.class)).removeJob(jobKey, trigger.getKey());
        }
    }
}
