package io.dataease.listener;

import io.dataease.chart.manage.ChartViewOldDataMergeService;
import io.dataease.startup.dao.auto.entity.CoreSysStartupJob;
import io.dataease.startup.dao.auto.mapper.CoreSysStartupJobMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * @Author Junjun
 */
@Component
@Order(value = 9)
public class ChartFilterDynamicListener implements ApplicationListener<ApplicationReadyEvent> {
    private final Logger logger = LoggerFactory.getLogger(ChartFilterDynamicListener.class);
    public static final String JOB_ID = "chartFilterDynamic";
    @Resource
    private CoreSysStartupJobMapper coreSysStartupJobMapper;
    @Resource
    private ChartViewOldDataMergeService chartViewOldDataMergeService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        logger.info("====chart filter dynamic [start]====");

        CoreSysStartupJob sysStartupJob = coreSysStartupJobMapper.selectById(JOB_ID);
        if (ObjectUtils.isNotEmpty(sysStartupJob) && StringUtils.equalsIgnoreCase(sysStartupJob.getStatus(), "ready")) {
            logger.info("====chart filter dynamic [doing]====");

            chartViewOldDataMergeService.refreshFilter();

            sysStartupJob.setStatus("done");
            coreSysStartupJobMapper.updateById(sysStartupJob);
        }
        logger.info("====chart filter dynamic [end]====");
    }
}
