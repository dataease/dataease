package io.dataease.datasource.manage;

import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceSaveExecution;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceSaveExecutionRepository;
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

    @Resource
    private DataSourceManage dataSourceManage;
    @Resource
    private CoreDatasourceSaveExecutionRepository executionRepository;

    @Transactional
    public Long commitCreate(DatasourceDTO dto) {
        Long executionId = IDUtils.snowID();
        dataSourceManage.innerSave(dto);
        saveExecution(executionId, dto.getId(), dto.getUpdateTime() == null ? null : String.valueOf(dto.getUpdateTime()), "COMMIT", STATUS_PENDING, null);
        return executionId;
    }

    @Transactional
    public Long commitEdit(CoreDatasource record) {
        Long executionId = IDUtils.snowID();
        dataSourceManage.innerEdit(record);
        saveExecution(executionId, record.getId(), record.getUpdateTime() == null ? null : String.valueOf(record.getUpdateTime()), "COMMIT", STATUS_PENDING, null);
        return executionId;
    }

    @Transactional
    public void mark(Long executionId, String step, String status, String result) {
        if (executionId == null) {
            return;
        }
        CoreDatasourceSaveExecution execution = executionRepository.findById(executionId).orElse(null);
        if (execution == null) {
            return;
        }
        execution.setStep(step);
        execution.setStatus(status);
        execution.setResult(result);
        execution.setUpdateTime(System.currentTimeMillis());
        executionRepository.save(execution);
    }

    /** 只在当前不是 FAILED 时写失败，保留首个失败步骤与原因。 */
    @Transactional
    public void markFailure(Long executionId, String step, String result) {
        if (executionId == null) {
            return;
        }
        CoreDatasourceSaveExecution execution = executionRepository.findById(executionId).orElse(null);
        if (execution == null || STATUS_FAILED.equals(execution.getStatus())) {
            return;
        }
        execution.setStep(step);
        execution.setStatus(STATUS_FAILED);
        execution.setResult(result);
        execution.setUpdateTime(System.currentTimeMillis());
        executionRepository.save(execution);
    }

    public List<CoreDatasourceSaveExecution> pendingExecutions() {
        return executionRepository.findByStatus(STATUS_PENDING);
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
        execution.setCreateTime(System.currentTimeMillis());
        execution.setUpdateTime(System.currentTimeMillis());
        executionRepository.save(execution);
    }
}
