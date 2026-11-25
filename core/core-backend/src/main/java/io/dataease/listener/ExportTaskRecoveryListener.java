package io.dataease.listener;

import io.dataease.dao.auto.entity.CoreExportTask;
import io.dataease.datasource.dao.auto.repository.QrtzSchedulerStateRepository;
import io.dataease.exportCenter.dao.auto.mapper.CoreExportTaskRepository;
import io.dataease.exportCenter.manage.ExportNodeIdentity;
import io.dataease.qrtz.dao.auto.repo.entity.QrtzSchedulerState;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 导出任务重启恢复：单节点内存句柄在重启后丢失，未完成导出不盲目重跑。
 * 按 QRTZ_SCHEDULER_STATE 心跳判断任务归属节点是否存活，失联节点上的任务统一标记为 FAILED，
 * 保留用户重试入口。
 */
@Component
@Order(value = 2)
public class ExportTaskRecoveryListener implements ApplicationListener<ApplicationReadyEvent> {

    private static final long NODE_GRACE_MILLIS = 60 * 1000L;

    @Resource
    private CoreExportTaskRepository coreExportTaskRepository;

    @Resource
    private QrtzSchedulerStateRepository qrtzSchedulerStateRepository;

    @Resource
    private ExportNodeIdentity exportNodeIdentity;

    @Value("${quartz.scheduler-name:deSyncJob}")
    private String schedulerName;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String currentInstanceId = exportNodeIdentity.currentInstanceId();
        List<String> liveInstances = liveInstances();
        List<CoreExportTask> pending = coreExportTaskRepository.findByExportStatusIn(List.of("PENDING", "IN_PROGRESS"));
        for (CoreExportTask task : pending) {
            String owner = task.getExportMachineName();
            boolean orphaned = owner == null || owner.isBlank()
                    || owner.equals(currentInstanceId)
                    || !ownerAlive(owner, liveInstances);
            if (!orphaned) {
                continue;
            }
            task.setExportStatus("FAILED");
            task.setExportProgress("0");
            coreExportTaskRepository.saveAndFlush(task);
            LogUtil.warn("Recovered export task " + task.getId() + " owned by " + owner + ", marked FAILED for manual retry");
        }
    }

    private List<String> liveInstances() {
        long now = System.currentTimeMillis();
        return qrtzSchedulerStateRepository.findAll().stream()
                .filter(state -> schedulerName.equals(state.getSchedName()))
                .filter(state -> state.getLastCheckinTime() != null && state.getCheckinInterval() != null)
                .filter(state -> now - state.getLastCheckinTime() <= state.getCheckinInterval() + NODE_GRACE_MILLIS)
                .map(QrtzSchedulerState::getInstanceName)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * owner 为稳定的节点名（配置值或 hostname）。QRTZ 中 AUTO 生成的实例名为
     * hostname + 13 位时间戳，两者都视为该节点存活。
     */
    private boolean ownerAlive(String owner, List<String> liveInstances) {
        for (String instanceName : liveInstances) {
            if (instanceName.equals(owner)) {
                return true;
            }
            if (instanceName.length() == owner.length() + 13
                    && instanceName.startsWith(owner)
                    && instanceName.substring(owner.length()).chars().allMatch(Character::isDigit)) {
                return true;
            }
        }
        return false;
    }
}
