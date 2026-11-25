package io.dataease.datasource.strategy;

import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTask;
import io.dataease.datasource.manage.DatasourceInvokeManage;
import io.dataease.datasource.manage.DatasourceSaveOrchestrator;
import io.dataease.datasource.manage.DatasourceSyncManage;
import io.dataease.datasource.server.DatasourceTaskServer;
import io.dataease.exception.DEException;
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
 * API 策略：toCreate/toDelete/toRecreate 计划化，建表/删表/调度幂等。
 */
@Component
public class ApiWriteStrategy implements DatasourceWriteStrategy {

    @Resource
    private DatasourceInvokeManage datasourceInvokeManage;
    @Resource
    private DatasourceSyncManage datasourceSyncManage;
    @Resource
    private DatasourceTaskServer datasourceTaskServer;
    @Resource
    private DatasourceSaveOrchestrator orchestrator;

    @Override
    public boolean supports(String type) {
        return type.contains(DatasourceConfiguration.DatasourceType.API.name());
    }

    @Override
    public void executeCreate(DatasourceDTO dto, CoreDatasource coreDatasource, Long executionId) throws Exception {
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dto);
        List<DatasetTableDTO> tables = (List<DatasetTableDTO>) datasourceInvokeManage.invokeMethod(coreDatasource.getType(), "getApiTables", DatasourceRequest.class, datasourceRequest);
        for (DatasetTableDTO api : tables) {
            datasourceRequest.setTable(api.getTableName());
            List<TableField> tableFields = (List<TableField>) datasourceInvokeManage.invokeMethod(coreDatasource.getType(), "getTableFields", DatasourceRequest.class, datasourceRequest);
            try {
                datasourceSyncManage.createEngineTable(datasourceRequest.getTable(), tableFields);
            } catch (Exception e) {
                orchestrator.mark(executionId, "TABLE_CREATE", DatasourceSaveOrchestrator.STATUS_FAILED, "Failed to create table " + datasourceRequest.getTable() + ": " + e.getMessage());
                DEException.throwException("Failed to create table " + datasourceRequest.getTable() + ": " + e.getMessage());
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

        List<DatasetTableDTO> sourceTableDTOs = (List<DatasetTableDTO>) datasourceInvokeManage.invokeMethod(sourceData.getType(), "getApiTables", DatasourceRequest.class, sourceTableRequest);
        List<String> sourceTables = sourceTableDTOs.stream().map(DatasetTableDTO::getTableName).toList();
        List<DatasetTableDTO> datasetTableDTOS = (List<DatasetTableDTO>) datasourceInvokeManage.invokeMethod(dto.getType(), "getApiTables", DatasourceRequest.class, datasourceRequest);
        List<String> tables = datasetTableDTOS.stream().map(DatasetTableDTO::getTableName).collect(java.util.stream.Collectors.toList());

        List<String> toCreateTables = tables.stream().filter(table -> !sourceTables.contains(table)).collect(java.util.stream.Collectors.toList());
        List<String> toDeleteTables = sourceTables.stream().filter(table -> !tables.contains(table)).collect(java.util.stream.Collectors.toList());
        for (String table : tables) {
            for (String sourceTable : sourceTables) {
                if (table.equals(sourceTable)) {
                    datasourceRequest.setTable(table);
                    List<TableField> tableFieldList = (List<TableField>) datasourceInvokeManage.invokeMethod(dto.getType(), "getTableFields", DatasourceRequest.class, datasourceRequest);
                    List<String> tableFields = tableFieldList.stream().map(TableField::getName).sorted().collect(java.util.stream.Collectors.toList());
                    sourceTableRequest.setTable(sourceTable);
                    List<TableField> sourceTableFieldList = (List<TableField>) datasourceInvokeManage.invokeMethod(sourceData.getType(), "getTableFields", DatasourceRequest.class, sourceTableRequest);
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

        List<String> newTables = toCreateTables.stream().filter(t -> !toDeleteTables.contains(t)).collect(java.util.stream.Collectors.toList());
        List<String> replacedTables = toCreateTables.stream().filter(toDeleteTables::contains).collect(java.util.stream.Collectors.toList());

        // 新增表：先创建，失败不动旧表。
        for (String toCreateTable : newTables) {
            datasourceRequest.setTable(toCreateTable);
            try {
                datasourceSyncManage.createEngineTable(toCreateTable, (List<TableField>) datasourceInvokeManage.invokeMethod(dto.getType(), "getTableFields", DatasourceRequest.class, datasourceRequest));
            } catch (Exception e) {
                String message = "Failed to create table " + toCreateTable + ", " + e.getMessage();
                orchestrator.mark(executionId, "TABLE_CREATE", DatasourceSaveOrchestrator.STATUS_FAILED, message);
                DEException.throwException(message);
            }
        }

        // 结构变化替换表：先建临时表，成功后 replaceTable 切换；失败清理临时表并保留旧表。
        for (String replacedTable : replacedTables) {
            String tmpTable = TableUtils.tmpName(replacedTable);
            datasourceRequest.setTable(replacedTable);
            try {
                datasourceSyncManage.createEngineTable(tmpTable, (List<TableField>) datasourceInvokeManage.invokeMethod(dto.getType(), "getTableFields", DatasourceRequest.class, datasourceRequest));
                datasourceSyncManage.replaceEngineTable(replacedTable);
            } catch (Exception e) {
                try {
                    datasourceSyncManage.dropEngineTable(tmpTable);
                } catch (Exception ignore) {
                }
                datasourceSyncManage.restoreEngineTable(replacedTable);
                String message = "Failed to replace table " + replacedTable + ", " + e.getMessage();
                orchestrator.mark(executionId, "TABLE_CREATE", DatasourceSaveOrchestrator.STATUS_FAILED, message);
                DEException.throwException(message);
            }
        }

        // 删除旧表（纯删除，不在替换集合中）。
        for (String deleteTable : toDeleteTables) {
            if (!replacedTables.contains(deleteTable)) {
                datasourceSyncManage.dropEngineTable(deleteTable);
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
