package io.dataease.datasource.strategy;

import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTask;
import io.dataease.datasource.manage.DatasourceSaveOrchestrator;
import io.dataease.datasource.manage.DatasourceSyncManage;
import io.dataease.datasource.provider.ExcelUtils;
import io.dataease.datasource.server.DatasourceTaskServer;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.utils.BeanUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 远程 Excel 策略：建表 + 调度 upsert 幂等。
 */
@Component
public class ExcelRemoteWriteStrategy implements DatasourceWriteStrategy {

    @Resource
    private DatasourceSyncManage datasourceSyncManage;
    @Resource
    private DatasourceTaskServer datasourceTaskServer;
    @Resource
    private DatasourceSaveOrchestrator orchestrator;

    @Override
    public boolean supports(String type) {
        return type.contains(DatasourceConfiguration.DatasourceType.ExcelRemote.name());
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
        CoreDatasourceTask coreDatasourceTask = buildTask(dto, coreDatasource);
        datasourceTaskServer.insert(coreDatasourceTask);
        datasourceSyncManage.addSchedule(coreDatasourceTask);
        orchestrator.mark(executionId, "SCHEDULE", DatasourceSaveOrchestrator.STATUS_SUCCESS, null);
    }

    @Override
    public void executeUpdate(DatasourceDTO dto, DatasourceDTO sourceData, CoreDatasource requestDatasource, Long executionId) throws Exception {
        DatasourceRequest sourceTableRequest = new DatasourceRequest();
        sourceTableRequest.setDatasource(sourceData);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dto);

        List<DatasetTableDTO> sourceTableDTOs = ExcelUtils.getTables(sourceTableRequest);
        List<String> sourceTables = sourceTableDTOs.stream().map(DatasetTableDTO::getTableName).toList();
        List<DatasetTableDTO> datasetTableDTOS = ExcelUtils.getTables(datasourceRequest);
        List<String> tables = datasetTableDTOS.stream().map(DatasetTableDTO::getTableName).collect(java.util.stream.Collectors.toList());

        List<String> toCreateTables = tables.stream().filter(table -> !sourceTables.contains(table)).collect(java.util.stream.Collectors.toList());
        List<String> toDeleteTables = sourceTables.stream().filter(table -> !tables.contains(table)).collect(java.util.stream.Collectors.toList());
        for (String table : tables) {
            for (String sourceTable : sourceTables) {
                if (table.equals(sourceTable)) {
                    datasourceRequest.setTable(table);
                    List<TableField> tableFieldList = ExcelUtils.getTableFields(datasourceRequest);
                    List<String> tableFields = tableFieldList.stream().map(TableField::getName).sorted().collect(java.util.stream.Collectors.toList());
                    sourceTableRequest.setTable(sourceTable);
                    List<TableField> sourceTableFieldList = ExcelUtils.getTableFields(sourceTableRequest);
                    List<String> sourceTableFields = sourceTableFieldList.stream().map(TableField::getName).sorted().collect(java.util.stream.Collectors.toList());
                    if (!String.join(",", tableFields).equals(String.join(",", sourceTableFields))) {
                        toDeleteTables.add(table);
                        toCreateTables.add(table);
                    }
                }
            }
        }
        CoreDatasourceTask coreDatasourceTask = buildTask(dto, requestDatasource);
        datasourceTaskServer.update(coreDatasourceTask);
        for (String deleteTable : toDeleteTables) {
            datasourceSyncManage.dropEngineTable(deleteTable);
        }
        for (String toCreateTable : toCreateTables) {
            datasourceRequest.setTable(toCreateTable);
            try {
                datasourceSyncManage.createEngineTable(toCreateTable, ExcelUtils.getTableFields(datasourceRequest));
            } catch (Exception e) {
                orchestrator.mark(executionId, "TABLE_CREATE", DatasourceSaveOrchestrator.STATUS_FAILED, "Failed to create table " + toCreateTable + ", " + e.getMessage());
                return;
            }
        }
        datasourceSyncManage.deleteSchedule(datasourceTaskServer.selectByDSId(dto.getId()));
        datasourceSyncManage.addSchedule(coreDatasourceTask);
        orchestrator.mark(executionId, "SCHEDULE", DatasourceSaveOrchestrator.STATUS_SUCCESS, null);
    }

    private CoreDatasourceTask buildTask(DatasourceDTO dto, CoreDatasource coreDatasource) {
        CoreDatasourceTask coreDatasourceTask = new CoreDatasourceTask();
        BeanUtils.copyBean(coreDatasourceTask, dto.getSyncSetting());
        coreDatasourceTask.setName(coreDatasource.getName() + "-task");
        coreDatasourceTask.setDsId(coreDatasource.getId());
        if (StringUtils.equalsIgnoreCase(coreDatasourceTask.getSyncRate(), DatasourceTaskServer.ScheduleType.RIGHTNOW.toString())) {
            coreDatasourceTask.setStartTime(System.currentTimeMillis() - 20 * 1000);
            coreDatasourceTask.setCron(null);
        } else {
            if (coreDatasourceTask.getStartTime() == null) {
                coreDatasourceTask.setStartTime(System.currentTimeMillis() - 20 * 1000);
            }
        }
        coreDatasourceTask.setTaskStatus("WaitingForExecution");
        return coreDatasourceTask;
    }
}
