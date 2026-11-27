package io.dataease.datasource.manage;

import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceSaveExecution;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceSaveExecutionRepository;
import io.dataease.exportCenter.manage.ExportNodeIdentity;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.utils.IDUtils;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据源保存编排：配置提交（短事务）与执行记录管理。
 * 外部建表/导入/调度等操作在提交之后由 DatasourceWriteStrategy 执行并逐步回写记录。
 */
@Component
public class DatasourceSaveOrchestrator {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";

    /** 保存执行租约时长：超过该时间未续约才允许其他节点启动恢复。 */
    public static final long LEASE_MILLIS = 30 * 60 * 1000L;

    @Resource
    private DataSourceManage dataSourceManage;
    @Resource
    private CoreDatasourceSaveExecutionRepository executionRepository;
    @Resource
    private ExportNodeIdentity exportNodeIdentity;

    @Transactional
    public Long commitCreate(DatasourceDTO dto) {
        Long executionId = IDUtils.snowID();
        dataSourceManage.innerSave(dto);
        saveExecution(executionId, dto.getId(), dto.getUpdateTime() == null ? null : String.valueOf(dto.getUpdateTime()), "COMMIT", STATUS_PENDING, null);
        return executionId;
    }

    /**
     * 在外部操作成功后提交新建数据源配置，复用已有执行记录，避免失败时残留新配置。
     */
    @Transactional
    public Long commitCreate(DatasourceDTO dto, Long executionId) {
        if (executionId == null) {
            return commitCreate(dto);
        }
        dataSourceManage.innerSave(dto);
        String configVersion = dto.getUpdateTime() == null ? null : String.valueOf(dto.getUpdateTime());
        CoreDatasourceSaveExecution execution = executionRepository.findById(executionId).orElse(null);
        if (execution == null) {
            saveExecution(executionId, dto.getId(), configVersion, "COMMIT", STATUS_PENDING, null);
        } else {
            execution.setStep("COMMIT");
            execution.setStatus(STATUS_PENDING);
            execution.setConfigVersion(configVersion);
            execution.setNodeId(currentNodeId());
            execution.setLeaseTime(newLeaseTime());
            execution.setUpdateTime(System.currentTimeMillis());
            executionRepository.save(execution);
        }
        return executionId;
    }

    @Transactional
    public Long begin(Long dsId, String configVersion) {
        Long executionId = IDUtils.snowID();
        saveExecution(executionId, dsId, configVersion, "PLAN", STATUS_PENDING, null);
        return executionId;
    }

    @Transactional
    public Long commitEdit(CoreDatasource record) {
        return commitEdit(record, null);
    }

    @Transactional
    public Long commitEdit(CoreDatasource record, Long executionId) {
        if (executionId == null) {
            executionId = IDUtils.snowID();
        }
        dataSourceManage.innerEdit(record);
        CoreDatasourceSaveExecution execution = executionRepository.findById(executionId).orElse(null);
        if (execution == null) {
            saveExecution(executionId, record.getId(), record.getUpdateTime() == null ? null : String.valueOf(record.getUpdateTime()), "COMMIT", STATUS_PENDING, null);
        } else {
            execution.setStep("COMMIT");
            execution.setStatus(STATUS_PENDING);
            execution.setConfigVersion(record.getUpdateTime() == null ? null : String.valueOf(record.getUpdateTime()));
            execution.setNodeId(currentNodeId());
            execution.setLeaseTime(newLeaseTime());
            execution.setUpdateTime(System.currentTimeMillis());
            executionRepository.save(execution);
        }
        return executionId;
    }

    @Transactional
    public void mark(Long executionId, String step, String status, String result) {
        if (executionId == null) {
            return;
        }
        long now = System.currentTimeMillis();
        executionRepository.markPendingExecution(executionId, currentNodeId(), step, status, result, now, now + LEASE_MILLIS);
    }

    /** 只在当前节点仍拥有该 PENDING 记录时写失败，保留首个失败步骤与原因。 */
    @Transactional
    public void markFailure(Long executionId, String step, String result) {
        if (executionId == null) {
            return;
        }
        executionRepository.markPendingExecutionFailed(executionId, currentNodeId(), step, result, System.currentTimeMillis());
    }

    public List<CoreDatasourceSaveExecution> expiredPendingExecutions(long now) {
        return executionRepository.findByStatusAndLeaseTimeLessThan(STATUS_PENDING, now);
    }

    /** 启动恢复：仅当租约已过期且记录仍为 PENDING 时原子标记失败。 */
    @Transactional
    public int markExpiredPendingFailed(Long executionId, String result, long now) {
        if (executionId == null) {
            return 0;
        }
        return executionRepository.markExpiredPendingFailed(executionId, result, now, now);
    }

    private void saveExecution(Long executionId, Long dsId, String configVersion, String step, String status, String result) {
        CoreDatasourceSaveExecution execution = new CoreDatasourceSaveExecution();
        execution.setId(executionId);
        execution.setDsId(dsId);
        execution.setConfigVersion(configVersion);
        execution.setStep(step);
        execution.setStatus(status);
        execution.setResult(result);
        execution.setRetryCount(0);
        long now = System.currentTimeMillis();
        execution.setCreateTime(now);
        execution.setUpdateTime(now);
        execution.setNodeId(currentNodeId());
        execution.setLeaseTime(now + LEASE_MILLIS);
        executionRepository.save(execution);
    }

    private String currentNodeId() {
        return exportNodeIdentity.currentInstanceId();
    }

    private long newLeaseTime() {
        return System.currentTimeMillis() + LEASE_MILLIS;
    }
}
