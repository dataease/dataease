package io.dataease.datasource.strategy;

import io.dataease.dao.auto.entity.CoreDatasource;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTask;
import io.dataease.datasource.manage.DatasourceSaveOrchestrator;
import io.dataease.datasource.manage.DatasourceSyncManage;
import io.dataease.datasource.provider.ExcelUtils;
import io.dataease.exception.DEException;
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

import java.util.ArrayList;
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
        List<String> createdTables = new ArrayList<>();
        CoreDatasourceTask coreDatasourceTask = null;
        boolean scheduleAdded = false;
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
            coreDatasourceTask = buildTask(dto, coreDatasource);
            datasourceTaskServer.insert(coreDatasourceTask);
            scheduleAdded = true;
            datasourceSyncManage.addSchedule(coreDatasourceTask);
            orchestrator.mark(executionId, "SCHEDULE", DatasourceSaveOrchestrator.STATUS_SUCCESS, null);
        } catch (Exception e) {
            if (scheduleAdded && coreDatasourceTask != null) {
                try {
                    datasourceSyncManage.deleteSchedule(coreDatasourceTask);
                } catch (Exception ignore) {
                }
            }
            if (coreDatasourceTask != null && coreDatasourceTask.getId() != null) {
                try {
                    datasourceTaskServer.delete(coreDatasourceTask.getId());
                } catch (Exception ignore) {
                }
            }
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
        DatasourceRequest sourceTableRequest = new DatasourceRequest();
        sourceTableRequest.setDatasource(sourceData);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dto);

        List<DatasetTableDTO> sourceTableDTOs = ExcelUtils.getTables(sourceTableRequest);
        List<String> sourceTables = sourceTableDTOs.stream().map(DatasetTableDTO::getTableName).toList();
        List<DatasetTableDTO> datasetTableDTOS = ExcelUtils.getTables(datasourceRequest);
        List<String> tables = datasetTableDTOS.stream().map(DatasetTableDTO::getTableName).collect(java.util.stream.Collectors.toList());

        List<String> toCreateTables = new ArrayList<>(tables.stream().filter(table -> !sourceTables.contains(table)).toList());
        List<String> toDeleteTables = new ArrayList<>(sourceTables.stream().filter(table -> !tables.contains(table)).toList());
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

        List<String> replacedTables = toCreateTables.stream().filter(toDeleteTables::contains).collect(java.util.stream.Collectors.toList());
        List<String> newTables = toCreateTables.stream().filter(table -> !replacedTables.contains(table)).collect(java.util.stream.Collectors.toList());
        List<String> preparedNewTables = new ArrayList<>();
        List<String> preparedTmpTables = new ArrayList<>();
        CoreDatasourceTask oldTask = datasourceTaskServer.selectByDSId(dto.getId());
        CoreDatasourceTask newTask = null;
        boolean taskUpdated = false;
        boolean scheduleAdded = false;
        boolean tablesSwitched = false;

        try {
            // 1. 先准备新增表；任何一步失败都未动旧表。
            for (String toCreateTable : newTables) {
                datasourceRequest.setTable(toCreateTable);
                try {
                    datasourceSyncManage.createEngineTable(toCreateTable, ExcelUtils.getTableFields(datasourceRequest));
                    preparedNewTables.add(toCreateTable);
                } catch (Exception e) {
                    String message = "Failed to create table " + toCreateTable + ", " + e.getMessage();
                    orchestrator.mark(executionId, "TABLE_CREATE", DatasourceSaveOrchestrator.STATUS_FAILED, message);
                    DEException.throwException(message);
                }
            }

            // 2. 结构变化表先建临时表，全部就绪后再切换，旧表在切换前保持可用。
            for (String replacedTable : replacedTables) {
                String tmpTable = TableUtils.tmpName(replacedTable);
                datasourceRequest.setTable(replacedTable);
                try {
                    datasourceSyncManage.createEngineTable(tmpTable, ExcelUtils.getTableFields(datasourceRequest));
                    preparedTmpTables.add(tmpTable);
                } catch (Exception e) {
                    String message = "Failed to create table " + replacedTable + ", " + e.getMessage();
                    orchestrator.mark(executionId, "TABLE_CREATE", DatasourceSaveOrchestrator.STATUS_FAILED, message);
                    DEException.throwException(message);
                }
            }

            // 3. 先准备任务与调度，再执行破坏性表切换；切换失败时可在 catch 中恢复旧任务/旧调度。
            newTask = buildTask(dto, requestDatasource);
            datasourceTaskServer.update(newTask);
            taskUpdated = true;
            scheduleAdded = true;
            datasourceSyncManage.addSchedule(newTask);

            // 4. 切换：批量 replace 结构变化表并删除纯删除表（同一事务，尽量原子化）。
            List<String> pureDeleteTables = toDeleteTables.stream()
                    .filter(table -> !replacedTables.contains(table))
                    .collect(java.util.stream.Collectors.toList());
            datasourceSyncManage.switchEngineTables(replacedTables, pureDeleteTables);
            tablesSwitched = true;

            // 5. 旧调度指向不同任务时才摘除；失败仅记录，不影响已就绪的新调度。
            if (oldTask != null && oldTask.getId() != null && !oldTask.getId().equals(newTask.getId())) {
                try {
                    datasourceSyncManage.deleteSchedule(oldTask);
                } catch (Exception ignore) {
                }
            }
            orchestrator.mark(executionId, "SCHEDULE", DatasourceSaveOrchestrator.STATUS_SUCCESS, null);
        } catch (Exception e) {
            // 清理本次新建但尚未切换的资源。
            for (String tmpTable : preparedTmpTables) {
                try {
                    datasourceSyncManage.dropEngineTable(tmpTable);
                } catch (Exception ignore) {
                }
            }
            for (String preparedNewTable : preparedNewTables) {
                try {
                    datasourceSyncManage.dropEngineTable(preparedNewTable);
                } catch (Exception ignore) {
                }
            }
            // 表切换前失败：尝试恢复被部分切换的旧表，并回滚任务与调度，保留旧表与旧调度。
            if (!tablesSwitched) {
                for (String replacedTable : replacedTables) {
                    try {
                        datasourceSyncManage.restoreEngineTable(replacedTable);
                    } catch (Exception ignore) {
                    }
                }
                if (scheduleAdded && newTask != null) {
                    try {
                        datasourceSyncManage.deleteSchedule(newTask);
                    } catch (Exception ignore) {
                    }
                }
                if (taskUpdated && newTask != null) {
                    if (oldTask != null && oldTask.getId() != null) {
                        try {
                            datasourceTaskServer.update(oldTask);
                        } catch (Exception ignore) {
                        }
                        try {
                            datasourceSyncManage.addSchedule(oldTask);
                        } catch (Exception ignore) {
                        }
                    } else {
                        try {
                            datasourceTaskServer.delete(newTask.getId());
                        } catch (Exception ignore) {
                        }
                    }
                }
            }
            throw e;
        }
    }

    @Override
    public void cleanupCreate(DatasourceDTO dto, CoreDatasource coreDatasource) {
        CoreDatasourceTask task = datasourceTaskServer.selectByDSId(dto.getId());
        if (task != null && task.getId() != null) {
            try {
                datasourceSyncManage.deleteSchedule(task);
            } catch (Exception ignore) {
            }
            try {
                datasourceTaskServer.delete(task.getId());
            } catch (Exception ignore) {
            }
        }
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
