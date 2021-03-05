package io.dataease.job.sechedule;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public abstract class DeScheduleJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

//        JobKey jobKey = context.getTrigger().getJobKey();
//        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
//        this.resourceId = jobDataMap.getString("resourceId");
//        this.userId = jobDataMap.getString("userId");
//        this.expression = jobDataMap.getString("expression");
//
//        LogUtil.info(jobKey.getGroup() + " Running: " + resourceId);
//        LogUtil.info("CronExpression: " + expression);
        businessExecute(context);
    }

    abstract void businessExecute(JobExecutionContext context);
}
