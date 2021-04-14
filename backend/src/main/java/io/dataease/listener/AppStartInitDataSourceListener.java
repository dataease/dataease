package io.dataease.listener;

import io.dataease.base.domain.DatasetTable;
import io.dataease.base.domain.DatasetTableExample;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.mapper.DatasetTableMapper;
import io.dataease.commons.utils.CommonThreadPool;
import io.dataease.datasource.service.DatasourceService;
import io.dataease.service.dataset.DataSetTableFieldsService;
import io.dataease.service.spark.SparkCalc;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Order(value = 2)
public class AppStartInitDataSourceListener implements ApplicationListener<ApplicationReadyEvent> {
    @Resource
    private DatasourceService datasourceService;
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        System.out.println("================= Init datasource connection pool =================");
        // 项目启动，从数据集中找到定时抽取的表，从HBase中读取放入缓存
        datasourceService.initAllDataSourceConnectionPool();
    }
}
