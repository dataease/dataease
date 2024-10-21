package io.dataease.job.schedule;

import io.dataease.license.utils.LicenseUtil;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.LogUtil;
import org.quartz.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class DeXpackDataFillingScheduleJob implements Job {


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Trigger trigger = jobExecutionContext.getTrigger();
        JobKey jobKey = trigger.getJobKey();
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        DeDataFillingTaskExecutor deTaskExecutor = CommonBeanFactory.getBean(DeDataFillingTaskExecutor.class);
        assert deTaskExecutor != null;
        try {
            LicenseUtil.validate();
            boolean taskLoaded = deTaskExecutor.execute(jobDataMap);
            if (!taskLoaded) {
                Objects.requireNonNull(CommonBeanFactory.getBean(ScheduleManager.class)).removeJob(jobKey, trigger.getKey());
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e.getCause());
        }
    }
}
