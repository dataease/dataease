package io.dataease.job.schedule;

import io.dataease.license.utils.LicenseUtil;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.LogUtil;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
public class DeXpackDataSyncTaskScheduleJob implements Job {


    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        DeXpackDataSyncTaskExecutor deTaskExecutor = CommonBeanFactory.getBean(DeXpackDataSyncTaskExecutor.class);
        assert deTaskExecutor != null;
        try {
            LicenseUtil.validate();
            deTaskExecutor.execute(jobDataMap);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e.getCause());
        }
    }
}
