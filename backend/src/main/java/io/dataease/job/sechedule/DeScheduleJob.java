package io.dataease.job.sechedule;

import io.dataease.commons.utils.LogUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobDataMap;

public abstract class DeScheduleJob implements Job {

    protected String datasetTableId;
    protected String expression;
    protected String taskId;
    protected String updateType;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobKey jobKey = context.getTrigger().getJobKey();
        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        this.datasetTableId = jobDataMap.getString("datasetTableId");
        this.expression = jobDataMap.getString("expression");
        this.taskId = jobDataMap.getString("taskId");
        this.updateType = jobDataMap.getString("updateType");
        LogUtil.info(jobKey.getGroup() + " Running: " + datasetTableId);
        LogUtil.info("CronExpression: " + expression);
        businessExecute(context);
    }

    abstract void businessExecute(JobExecutionContext context);
}
