package io.dataease.job.sechedule;

import io.dataease.commons.utils.LogUtil;
import org.quartz.JobExecutionContext;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author gin
 * @Date 2021/3/5 11:37 上午
 */
public class TestJob extends DeScheduleJob {
    @Override
    void businessExecute(JobExecutionContext context) {
        LogUtil.info("Test Job -- " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
    }
}
