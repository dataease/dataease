package io.dataease.listener;


import io.dataease.datasource.manage.DataSourceManage;
import io.dataease.datasource.manage.DatasourceSyncManage;
import io.dataease.datasource.manage.EngineManage;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.manage.SysParameterManage;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Order(value = 2)
public class DataSourceInitStartListener implements ApplicationListener<ApplicationReadyEvent> {
    @Resource
    private DatasourceSyncManage datasourceSyncManage;
    @Resource
    private DatasourceServer datasourceServer;

    @Resource
    private DataSourceManage dataSourceManage;
    @Resource
    private EngineManage engineManage;
    @Resource
    private SysParameterManage sysParameterManage;
    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        try {
            engineManage.initSimpleEngine();
        } catch (Exception e) {
            LogUtil.error("Init engine failed", e);
        }
        dataSourceManage.initDataSourceConnectionPool();
        datasourceSyncManage.initSyncTask();
        try {
            List<CoreSysSetting> coreSysSettings = sysParameterManage.groupList("basic.");
            datasourceServer.addJob(coreSysSettings);
        } catch (Exception e) {
            LogUtil.error("Init datasource checkStatus task failed: ", e);
        }

    }


}
