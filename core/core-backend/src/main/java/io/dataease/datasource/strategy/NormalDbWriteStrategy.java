package io.dataease.datasource.strategy;

import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.manage.DatasourceSaveOrchestrator;
import io.dataease.datasource.manage.DataSourceManage;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.datasource.dao.auto.repository.CoreDatasourceRepository;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 普通数据库策略：Commit 只写元数据，Execute 提交后异步更新 Calcite 连接池。
 */
@Component
public class NormalDbWriteStrategy implements DatasourceWriteStrategy {

    @Resource
    private CalciteProvider calciteProvider;
    @Resource
    private CoreDatasourceRepository coreDatasourceRepository;
    @Resource
    private DataSourceManage dataSourceManage;
    @Resource
    private DatasourceSaveOrchestrator orchestrator;

    @Override
    public boolean supports(String type) {
        return !type.equals(DatasourceConfiguration.DatasourceType.Excel.name())
                && !type.contains(DatasourceConfiguration.DatasourceType.API.name())
                && !type.contains(DatasourceConfiguration.DatasourceType.ExcelRemote.name());
    }

    @Override
    public void executeCreate(DatasourceDTO dto, CoreDatasource coreDatasource, Long executionId) throws Exception {
        try {
            calciteProvider.update(dto);
            orchestrator.mark(executionId, "CALCITE_UPDATE", DatasourceSaveOrchestrator.STATUS_SUCCESS, null);
        } catch (Exception e) {
            markError(coreDatasource.getId(), executionId, "CALCITE_UPDATE", e);
        }
    }

    @Override
    public void executeUpdate(DatasourceDTO dto, DatasourceDTO sourceData, CoreDatasource requestDatasource, Long executionId) throws Exception {
        try {
            calciteProvider.update(dto);
            orchestrator.mark(executionId, "CALCITE_UPDATE", DatasourceSaveOrchestrator.STATUS_SUCCESS, null);
        } catch (Exception e) {
            markError(requestDatasource.getId(), executionId, "CALCITE_UPDATE", e);
        }
    }

    private void markError(Long dsId, Long executionId, String step, Exception e) {
        CoreDatasource ds = coreDatasourceRepository.findById(dsId).orElse(null);
        if (ds != null) {
            ds.setStatus("Error");
            dataSourceManage.innerEditStatus(ds);
        }
        orchestrator.mark(executionId, step, DatasourceSaveOrchestrator.STATUS_FAILED, e.getMessage());
        LogUtil.error(e);
    }
}
