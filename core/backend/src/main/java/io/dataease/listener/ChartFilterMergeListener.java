package io.dataease.listener;

import io.dataease.plugins.common.base.domain.SysStartupJob;
import io.dataease.plugins.common.base.mapper.SysStartupJobMapper;
import io.dataease.plugins.common.util.ClassloaderResponsity;
import io.dataease.service.chart.ChartViewOldDataMergeService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author Junjun
 */
@Component
public class ChartFilterMergeListener implements ApplicationListener<ApplicationReadyEvent> {
    private final Logger logger = LoggerFactory.getLogger(ClassloaderResponsity.class);
    public static final String JOB_ID = "chartFilterMerge";
    @Resource
    private SysStartupJobMapper sysStartupJobMapper;
    @Resource
    private ChartViewOldDataMergeService chartViewOldDataMergeService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        logger.info("====chart filter merge [start]====");

        SysStartupJob sysStartupJob = sysStartupJobMapper.selectByPrimaryKey(JOB_ID);
        if (ObjectUtils.isNotEmpty(sysStartupJob) && StringUtils.equalsIgnoreCase(sysStartupJob.getStatus(), "ready")) {
            logger.info("====chart filter merge [doing]====");

            chartViewOldDataMergeService.mergeOldData();
            chartViewOldDataMergeService.mergeCacheOldData();

            sysStartupJob.setStatus("done");
            sysStartupJobMapper.updateByPrimaryKey(sysStartupJob);
        }
        logger.info("====chart filter merge [end]====");
    }
}
