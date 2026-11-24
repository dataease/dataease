package io.dataease.listener;

import io.dataease.dao.auto.entity.CoreExportTask;
import io.dataease.exportCenter.dao.auto.mapper.CoreExportTaskRepository;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 导出任务重启恢复：单节点内存句柄在重启后丢失，未完成导出不盲目重跑，
 * 统一标记为 FAILED，保留用户重试入口。
 */
@Component
@Order(value = 2)
public class ExportTaskRecoveryListener implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private CoreExportTaskRepository coreExportTaskRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        List<CoreExportTask> pending = coreExportTaskRepository.findByExportStatusIn(List.of("PENDING", "IN_PROGRESS"));
        for (CoreExportTask task : pending) {
            task.setExportStatus("FAILED");
            task.setExportProgress("0");
            coreExportTaskRepository.saveAndFlush(task);
            LogUtil.warn("Recovered export task " + task.getId() + " after restart, marked FAILED for manual retry");
        }
    }
}
