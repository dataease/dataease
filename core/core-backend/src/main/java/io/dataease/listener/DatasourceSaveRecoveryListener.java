package io.dataease.listener;

import io.dataease.datasource.dao.auto.entity.CoreDatasourceSaveExecution;
import io.dataease.datasource.manage.DatasourceSaveOrchestrator;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 重启恢复：启动时把上次进程未完成的保存执行记录标记为失败，提供可定位的恢复入口。
 */
@Component
@Order(value = 1)
public class DatasourceSaveRecoveryListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private DatasourceSaveOrchestrator orchestrator;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        long now = System.currentTimeMillis();
        List<CoreDatasourceSaveExecution> expired = orchestrator.expiredPendingExecutions(now);
        for (CoreDatasourceSaveExecution execution : expired) {
            int updated = orchestrator.markExpiredPendingFailed(execution.getId(), "进程重启导致执行中断，请根据步骤与结果重试", now);
            if (updated > 0) {
                LogUtil.warn("Recovered datasource save execution: " + execution.getId() + " for ds " + execution.getDsId());
            }
        }
    }
}
