package io.dataease.listener;

import io.dataease.plugins.common.base.domain.DatasetTable;
import io.dataease.plugins.common.base.domain.DatasetTableExample;
import io.dataease.plugins.common.base.mapper.DatasetTableMapper;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.loader.ClassloaderResponsity;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/12/22 10:01 上午
 */
@Component
public class DatasetCheckListener implements ApplicationListener<ApplicationReadyEvent> {
    private final Logger logger = LoggerFactory.getLogger(ClassloaderResponsity.class);
    public static final String CACHE_NAME = "check_ds";
    public static final String CACHE_KEY = "hide_custom_ds";
    @Resource
    private DatasetTableMapper datasetTableMapper;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        logger.info("Start check custom dataset");
        // 项目启动查找是否有'自定义数据集'
        DatasetTableExample datasetTableExample = new DatasetTableExample();
        datasetTableExample.createCriteria().andTypeEqualTo("custom");
        List<DatasetTable> datasetTables = datasetTableMapper.selectByExample(datasetTableExample);
        CacheUtils.put(CACHE_NAME, CACHE_KEY, CollectionUtils.isEmpty(datasetTables), null, null);
    }
}
