package io.dataease.job.schedule;

import io.dataease.license.utils.LicenseUtil;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.LogUtil;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * 同步管理专用任务
 * 防止同一个任务执行时间超过调度周期时重叠运行，避免重复同步和重复生成 RUNNING 日志
 * 该限制只作用于相同 JobKey；不同同步任务及其他类型的 Quartz 任务仍可并行执行
 * 错过触发后的处理继续沿用 Quartz 原有 SmartPolicy，不改变已有任务的恢复补偿语义
 */
@Component
@DisallowConcurrentExecution
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
