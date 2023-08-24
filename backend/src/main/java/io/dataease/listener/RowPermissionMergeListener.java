package io.dataease.listener;

import io.dataease.plugins.common.base.domain.SysStartupJob;
import io.dataease.plugins.common.base.mapper.SysStartupJobMapper;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.loader.ClassloaderResponsity;
import io.dataease.plugins.xpack.auth.service.RowPermissionTreeService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Author gin
 * @Date 2022/07/10 10:01 上午
 */
@Component
public class RowPermissionMergeListener implements ApplicationListener<ApplicationReadyEvent> {
    private final Logger logger = LoggerFactory.getLogger(ClassloaderResponsity.class);
    public static final String JOB_ID = "rowPermissionsMerge";
    @Resource
    private SysStartupJobMapper sysStartupJobMapper;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        Map<String, RowPermissionTreeService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((RowPermissionTreeService.class));
        if (beansOfType.keySet().size() > 0) {
            logger.info("====row permissions merge [start]====");

            SysStartupJob sysStartupJob = sysStartupJobMapper.selectByPrimaryKey(JOB_ID);
            if (ObjectUtils.isNotEmpty(sysStartupJob) && StringUtils.equalsIgnoreCase(sysStartupJob.getStatus(), "ready")) {
                logger.info("====row permissions merge [doing]====");

                RowPermissionTreeService rowPermissionTreeService = SpringContextUtil.getBean(RowPermissionTreeService.class);
                rowPermissionTreeService.mergeOldPermissions();
                sysStartupJob.setStatus("done");
                sysStartupJobMapper.updateByPrimaryKey(sysStartupJob);
            }
            logger.info("====row permissions merge [end]====");
        }
    }
}
