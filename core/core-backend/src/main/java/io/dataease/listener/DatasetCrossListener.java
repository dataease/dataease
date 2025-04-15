package io.dataease.listener;

import io.dataease.chart.manage.ChartViewOldDataMergeService;
import io.dataease.dataset.manage.DatasetSQLManage;
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
@Order(value = 10)
public class DatasetCrossListener implements ApplicationListener<ApplicationReadyEvent> {
    private final Logger logger = LoggerFactory.getLogger(DatasetCrossListener.class);
    public static final String JOB_ID = "datasetCrossListener";
    @Resource
    private CoreSysStartupJobMapper coreSysStartupJobMapper;
    @Resource
    private DatasetSQLManage datasetSQLManage;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        logger.info("====dataset cross listener [start]====");

        CoreSysStartupJob sysStartupJob = coreSysStartupJobMapper.selectById(JOB_ID);
        if (ObjectUtils.isNotEmpty(sysStartupJob) && StringUtils.equalsIgnoreCase(sysStartupJob.getStatus(), "ready")) {
            logger.info("====dataset cross listener [doing]====");

            datasetSQLManage.datasetCrossDefault();

            sysStartupJob.setStatus("done");
            coreSysStartupJobMapper.updateById(sysStartupJob);
        }
        logger.info("====dataset cross listener [end]====");
    }
}
