package io.dataease.datasource.strategy;

import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.manage.DatasourceSaveOrchestrator;
import io.dataease.datasource.manage.DatasourceSyncManage;
import io.dataease.datasource.provider.ExcelUtils;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
        List<String> createdTables = new ArrayList<>();
        try {
            for (DatasetTableDTO table : tables) {
                datasourceRequest.setTable(table.getTableName());
                List<TableField> tableFields = ExcelUtils.getTableFields(datasourceRequest);
                try {
                    datasourceSyncManage.createEngineTable(datasourceRequest.getTable(), tableFields);
                    createdTables.add(datasourceRequest.getTable());
                } catch (Exception e) {
                    String message;
                    if (e.getMessage() != null && e.getMessage().toLowerCase().contains("Row size too large".toLowerCase())) {
                        message = "文本内容超出最大支持范围： " + datasourceRequest.getTable() + ", " + e.getMessage();
                    } else {
                        message = "Failed to create table " + datasourceRequest.getTable() + ", " + e.getMessage();
                    }
                    orchestrator.mark(executionId, "TABLE_CREATE", DatasourceSaveOrchestrator.STATUS_FAILED, message);
                    DEException.throwException(message);
                }
            }
            datasourceSyncManage.extractExcelData(coreDatasource, "all_scope");
            orchestrator.mark(executionId, "IMPORT", DatasourceSaveOrchestrator.STATUS_SUCCESS, null);
        } catch (Exception e) {
            for (String createdTable : createdTables) {
                try {
                    datasourceSyncManage.dropEngineTable(createdTable);
                } catch (Exception ignore) {
                }
            }
            throw e;
        }
    }

    @Override
    public void executeUpdate(DatasourceDTO dto, DatasourceDTO sourceData, CoreDatasource requestDatasource, Long executionId) throws Exception {
        requestDatasource.setEnableDataFill(null);
        if (Objects.equals(dto.getEditType(), REPLACE)) {
            datasourceSyncManage.extractExcelData(requestDatasource, "all_scope");
        } else {
            datasourceSyncManage.extractExcelData(requestDatasource, "add_scope");
        }
        orchestrator.mark(executionId, "IMPORT", DatasourceSaveOrchestrator.STATUS_SUCCESS, null);
    }

    @Override
    public void cleanupCreate(DatasourceDTO dto, CoreDatasource coreDatasource) {
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dto);
        try {
            for (DatasetTableDTO table : ExcelUtils.getTables(datasourceRequest)) {
                try {
                    datasourceSyncManage.dropEngineTable(table.getTableName());
                } catch (Exception ignore) {
                }
            }
        } catch (Exception ignore) {
        }
    }
}
