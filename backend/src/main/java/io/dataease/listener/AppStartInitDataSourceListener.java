package io.dataease.listener;

import io.dataease.datasource.service.DatasourceService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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
