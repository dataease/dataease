package io.dataease.datasource.strategy;

import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.manage.DatasourceSaveOrchestrator;
import io.dataease.datasource.manage.DatasourceSyncManage;
import io.dataease.datasource.provider.ExcelUtils;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Excel 策略：建表与导入幂等，replace 全量导入后 mergeSheets，add_scope 追加导入后 mergeSheets。
 */
@Component
public class ExcelWriteStrategy implements DatasourceWriteStrategy {

    private static final Integer REPLACE = 0;

    @Resource
    private DatasourceSyncManage datasourceSyncManage;
    @Resource
    private DatasourceSaveOrchestrator orchestrator;

    @Override
    public boolean supports(String type) {
        return DatasourceConfiguration.DatasourceType.Excel.name().equals(type);
    }

    @Override
    public void executeCreate(DatasourceDTO dto, CoreDatasource coreDatasource, Long executionId) throws Exception {
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dto);
        List<DatasetTableDTO> tables = ExcelUtils.getTables(datasourceRequest);
        for (DatasetTableDTO table : tables) {
            datasourceRequest.setTable(table.getTableName());
            List<TableField> tableFields = ExcelUtils.getTableFields(datasourceRequest);
            try {
                datasourceSyncManage.createEngineTable(datasourceRequest.getTable(), tableFields);
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().toLowerCase().contains("Row size too large".toLowerCase())) {
                    orchestrator.mark(executionId, "TABLE_CREATE", DatasourceSaveOrchestrator.STATUS_FAILED, "文本内容超出最大支持范围： " + datasourceRequest.getTable() + ", " + e.getMessage());
                } else {
                    orchestrator.mark(executionId, "TABLE_CREATE", DatasourceSaveOrchestrator.STATUS_FAILED, "Failed to create table " + datasourceRequest.getTable() + ", " + e.getMessage());
                }
                return;
            }
        }
        datasourceSyncManage.extractExcelData(coreDatasource, "all_scope");
        orchestrator.mark(executionId, "IMPORT", DatasourceSaveOrchestrator.STATUS_SUCCESS, null);
    }

    @Override
    public void executeUpdate(DatasourceDTO dto, DatasourceDTO sourceData, CoreDatasource requestDatasource, Long executionId) throws Exception {
        requestDatasource.setEnableDataFill(null);
        if (Objects.equals(dto.getEditType(), REPLACE)) {
            datasourceSyncManage.extractExcelData(requestDatasource, "all_scope");
            ExcelUtils.mergeSheets(requestDatasource, sourceData);
            orchestrator.mark(executionId, "IMPORT", DatasourceSaveOrchestrator.STATUS_SUCCESS, null);
        } else {
            datasourceSyncManage.extractExcelData(requestDatasource, "add_scope");
            ExcelUtils.mergeSheets(requestDatasource, sourceData);
            orchestrator.mark(executionId, "IMPORT", DatasourceSaveOrchestrator.STATUS_SUCCESS, null);
        }
    }
}
