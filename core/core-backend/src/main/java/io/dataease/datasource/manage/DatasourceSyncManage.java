package io.dataease.datasource.manage;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.vo.TableField;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTask;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTaskLog;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.datasource.provider.ApiUtils;
import io.dataease.datasource.provider.EngineProvider;
import io.dataease.datasource.provider.ExcelUtils;
import io.dataease.datasource.provider.ProviderUtil;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.datasource.request.EngineRequest;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.datasource.server.DatasourceTaskServer;
import io.dataease.datasource.server.EngineServer;
import io.dataease.job.sechedule.ExtractDataJob;
import io.dataease.job.sechedule.ScheduleManager;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class DatasourceSyncManage {

    @Resource
    private CoreDatasourceMapper datasourceMapper;
    @Resource
    private EngineServer engineServer;
    @Resource
    private DatasourceTaskServer datasourceTaskServer;
    @Resource
    private ScheduleManager scheduleManager;

    public void extractExcelData(Long datasourceId, String type) {
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        if (coreDatasource == null) {
            LogUtil.error("Can not find CoreDatasource: " + datasourceId);
            return;
        }
        Long startTime = System.currentTimeMillis();
        CoreDatasourceTaskLog datasetTableTaskLog = datasourceTaskServer.initTaskLog(datasourceId, null, startTime, coreDatasource.getName());
        DatasourceServer.UpdateType updateType = DatasourceServer.UpdateType.valueOf(type);

        boolean msg = false;

        Long execTime = System.currentTimeMillis();
        try {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            List<DatasetTableDTO> tables = ExcelUtils.getTables(datasourceRequest);
            int success = 0;
            for (DatasetTableDTO api : tables) {
                datasourceRequest.setTable(api.getTableName());
                try {
                    datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Begin to sync datatable: " + datasourceRequest.getTable());
                    datasourceTaskServer.saveLog(datasetTableTaskLog);
                    List<TableField> tableFields = ExcelUtils.getTableFields(datasourceRequest);
                    createEngineTable(datasourceRequest.getTable(), tableFields);
                    if (updateType.equals(DatasourceServer.UpdateType.all_scope)) {
                        createEngineTable(TableUtils.tmpName(datasourceRequest.getTable()), tableFields);
                    }
                    extractExcelData(datasourceRequest, updateType);
                    if (updateType.equals(DatasourceServer.UpdateType.all_scope)) {
                        replaceTable(datasourceRequest.getTable());
                    }
                    datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n End to sync datatable: " + datasourceRequest.getTable());
                    datasourceTaskServer.saveLog(datasetTableTaskLog);
                    success++;
                } catch (Exception e) {
                    datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Failed to sync datatable: " + datasourceRequest.getTable() + ", " + e.getMessage());
                    datasourceTaskServer.saveLog(datasetTableTaskLog);
                }
            }
            datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Complete to sync datasourc.");
            if (success == 0) {
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Failed to sync datasourc.");
                datasetTableTaskLog.setStatus(TaskStatus.Error.name());
            }
            if (success == tables.size()) {
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Complete to sync datasourc.");
                datasetTableTaskLog.setStatus(TaskStatus.Completed.name());
            }
            if (0 < success && success < tables.size()) {
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Complete to sync datasourc.");
                datasetTableTaskLog.setStatus(TaskStatus.Warning.name());
            }
            datasetTableTaskLog.setEndTime(System.currentTimeMillis());
            datasourceTaskServer.saveLog(datasetTableTaskLog);
        } catch (Exception e) {
            datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Failed to sync datasourc.");
            datasetTableTaskLog.setEndTime(System.currentTimeMillis());
            datasetTableTaskLog.setStatus(TaskStatus.Error.name());
            datasourceTaskServer.saveLog(datasetTableTaskLog);
        } finally {
            //DELETE
        }
    }


    public void extractData(Long datasourceId, Long taskId, String type, JobExecutionContext context) {
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        if (coreDatasource == null) {
            LogUtil.error("Can not find datasource: " + datasourceId);
            return;
        }
        CoreDatasourceTask coreDatasourceTask = datasourceTaskServer.selectById(taskId);
        if (coreDatasourceTask == null) {
            return;
        }
        datasourceTaskServer.checkTaskIsStopped(coreDatasourceTask);
        if (StringUtils.isNotEmpty(coreDatasourceTask.getStatus()) && (coreDatasourceTask.getStatus().equalsIgnoreCase(TaskStatus.Stopped.name()) || coreDatasourceTask.getStatus().equalsIgnoreCase(TaskStatus.Suspend.name()))) {
            LogUtil.info("Skip synchronization task: {} ,due to task status is {}", coreDatasourceTask.getId(), coreDatasourceTask.getStatus());
            return;
        }

        Long startTime = System.currentTimeMillis();
        if (datasourceTaskServer.existUnderExecutionTask(datasourceId, coreDatasourceTask.getId(), startTime)) {
            LogUtil.info("Skip synchronization task for datasource due to exist others, datasource ID : " + datasourceId);
            return;
        }
        CoreDatasourceTaskLog datasetTableTaskLog = datasourceTaskServer.initTaskLog(datasourceId, taskId, startTime, coreDatasource.getName());
        DatasourceServer.UpdateType updateType = DatasourceServer.UpdateType.valueOf(type);
        if (context != null) {
            UpdateWrapper<CoreDatasource> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", datasourceId);
            CoreDatasource record = new CoreDatasource();
            record.setQrtzInstance(context.getFireInstanceId());
            datasourceMapper.update(record, updateWrapper);
        }

        boolean msg = false;
        TaskStatus lastExecStatus = TaskStatus.Completed;
        Long execTime = System.currentTimeMillis();
        try {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            List<DatasetTableDTO> tables = ApiUtils.getTables(datasourceRequest);
            int success = 0;

            for (DatasetTableDTO api : tables) {
                datasourceRequest.setTable(api.getTableName());
                try {
                    datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Begin to sync datatable: " + datasourceRequest.getTable());
                    datasourceTaskServer.saveLog(datasetTableTaskLog);
                    List<TableField> tableFields = ApiUtils.getTableFields(datasourceRequest);
                    createEngineTable(datasourceRequest.getTable(), tableFields);
                    if (updateType.equals(DatasourceServer.UpdateType.all_scope)) {
                        createEngineTable(TableUtils.tmpName(datasourceRequest.getTable()), tableFields);
                    }
                    extractApiData(datasourceRequest, updateType);
                    if (updateType.equals(DatasourceServer.UpdateType.all_scope)) {
                        replaceTable(datasourceRequest.getTable());
                    }
                    datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n End to sync datatable: " + datasourceRequest.getTable());
                    datasourceTaskServer.saveLog(datasetTableTaskLog);
                    success++;
                } catch (Exception e) {
                    datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Failed to sync datatable: " + datasourceRequest.getTable() + ", " + e.getMessage());
                    datasourceTaskServer.saveLog(datasetTableTaskLog);
                }
            }
            datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Complete to sync datasourc.");
            if (success == 0) {
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Failed to sync datasourc.");
                datasetTableTaskLog.setStatus(TaskStatus.Error.name());
                lastExecStatus = TaskStatus.Error;
            }
            if (success == tables.size()) {
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Complete to sync datasourc.");
                datasetTableTaskLog.setStatus(TaskStatus.Completed.name());
                lastExecStatus = TaskStatus.Completed;
            }
            if (0 < success && success < tables.size()) {
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Complete to sync datasourc.");
                datasetTableTaskLog.setStatus(TaskStatus.Warning.name());
                lastExecStatus = TaskStatus.Warning;
            }
            datasetTableTaskLog.setEndTime(System.currentTimeMillis());
            datasourceTaskServer.saveLog(datasetTableTaskLog);
        } catch (Exception e) {
            datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Failed to sync datasourc.");
            datasetTableTaskLog.setEndTime(System.currentTimeMillis());
            datasetTableTaskLog.setStatus(TaskStatus.Error.name());
            datasourceTaskServer.saveLog(datasetTableTaskLog);
        } finally {
            try {
                datasourceTaskServer.updateTaskStatus(coreDatasourceTask, lastExecStatus);
                UpdateWrapper<CoreDatasource> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", datasourceId);
                CoreDatasource record = new CoreDatasource();
                record.setTaskStatus(TaskStatus.WaitingForExecution.name());
                datasourceMapper.update(record, updateWrapper);
            } catch (Exception ignore) {
                LogUtil.error(ignore);
            }
        }
    }
    private void extractApiData(DatasourceRequest datasourceRequest, DatasourceServer.UpdateType extractType) throws Exception {
        Map<String, Object> result = ApiUtils.fetchResultField(datasourceRequest);
        List<String[]> dataList = (List<String[]>) result.get("dataList");
        String engineTableName;
        switch (extractType) {
            case all_scope:
                engineTableName = TableUtils.tmpName(TableUtils.tableName(datasourceRequest.getTable()));
                break;
            default:
                engineTableName = TableUtils.tableName(datasourceRequest.getTable());
                break;
        }
        CoreDeEngine engine = engineServer.info();

        EngineRequest engineRequest = new EngineRequest();
        engineRequest.setEngine(engine);
        EngineProvider engineProvider = ProviderUtil.getEngineProvider(engine.getType());
        int pageNumber = 1000; //一次插入 1000条
        int totalPage;
        if (dataList.size() % pageNumber > 0) {
            totalPage = dataList.size() / pageNumber + 1;
        } else {
            totalPage = dataList.size() / pageNumber;
        }

        for (int page = 1; page <= totalPage; page++) {
            engineRequest.setQuery(engineProvider.insertSql(engineTableName, dataList, page, pageNumber));
            engineProvider.exec(engineRequest);
        }
    }

    private void extractExcelData(DatasourceRequest datasourceRequest, DatasourceServer.UpdateType extractType) throws Exception {
        ExcelUtils excelUtils = new ExcelUtils();
        List<String[]> dataList = excelUtils.fetchDataList(datasourceRequest);
        String engineTableName;
        switch (extractType) {
            case all_scope:
                engineTableName = TableUtils.tmpName(TableUtils.tableName(datasourceRequest.getTable()));
                break;
            default:
                engineTableName = TableUtils.tableName(datasourceRequest.getTable());
                break;
        }
        CoreDeEngine engine = engineServer.info();

        EngineRequest engineRequest = new EngineRequest();
        engineRequest.setEngine(engine);
        EngineProvider engineProvider = ProviderUtil.getEngineProvider(engine.getType());
        int pageNumber = 1000; //一次插入 1000条
        int totalPage;
        if (dataList.size() % pageNumber > 0) {
            totalPage = dataList.size() / pageNumber + 1;
        } else {
            totalPage = dataList.size() / pageNumber;
        }
        for (int page = 1; page <= totalPage; page++) {
            engineRequest.setQuery(engineProvider.insertSql(engineTableName, dataList, page, pageNumber));
            engineProvider.exec(engineRequest);
        }
    }

    private void replaceTable(String tableName) throws Exception {
        CoreDeEngine engine = engineServer.info();
        EngineRequest engineRequest = new EngineRequest();
        engineRequest.setEngine(engine);
        EngineProvider engineProvider = ProviderUtil.getEngineProvider(engine.getType());
        String[] replaceTableSql = engineProvider.replaceTable(tableName).split(";");
        for (int i = 0; i < replaceTableSql.length; i++) {
            if (StringUtils.isNotEmpty(replaceTableSql[i])) {
                engineRequest.setQuery(replaceTableSql[i]);
                engineProvider.exec(engineRequest);
            }
        }
    }

    public void createEngineTable(String tableName, List<TableField> tableFields) throws Exception {
        CoreDeEngine engine = engineServer.info();
        EngineRequest engineRequest = new EngineRequest();
        engineRequest.setEngine(engine);
        EngineProvider engineProvider = ProviderUtil.getEngineProvider(engine.getType());
        engineRequest.setQuery(engineProvider.createTableSql(tableName, tableFields, engine));
        engineProvider.exec(engineRequest);
    }

    public void dropEngineTable(String tableName) throws Exception {
        CoreDeEngine engine = engineServer.info();
        EngineRequest engineRequest = new EngineRequest();
        engineRequest.setEngine(engine);
        EngineProvider engineProvider = ProviderUtil.getEngineProvider(engine.getType());
        engineRequest.setQuery(engineProvider.dropTable(tableName));
        engineProvider.exec(engineRequest);
    }

    public void addSchedule(CoreDatasourceTask datasourceTask) throws Exception {
        if (StringUtils.equalsIgnoreCase(datasourceTask.getSyncRate(), DatasourceTaskServer.ScheduleType.RIGHTNOW.toString())) {
            scheduleManager.addOrUpdateSingleJob(new JobKey(datasourceTask.getId().toString(), datasourceTask.getDsId().toString()),
                    new TriggerKey(datasourceTask.getId().toString(), datasourceTask.getDsId().toString()),
                    ExtractDataJob.class,
                    new Date(datasourceTask.getStartTime()),
                    scheduleManager.getDefaultJobDataMap(datasourceTask.getDsId().toString(), datasourceTask.getCron(), datasourceTask.getId().toString(), datasourceTask.getUpdateType()));
        } else {
            Date endTime;
            if (StringUtils.equalsIgnoreCase(datasourceTask.getEndLimit().toString(), "1")) {
                if (datasourceTask.getEndTime() == null || datasourceTask.getEndTime() == 0) {
                    endTime = null;
                } else {
                    endTime = new Date(datasourceTask.getEndTime());
                    if (endTime.before(new Date())) {
                        deleteSchedule(datasourceTask);
                        return;
                    }
                }
            } else {
                endTime = null;
            }

            scheduleManager.addOrUpdateCronJob(new JobKey(datasourceTask.getId().toString(), datasourceTask.getDsId().toString()),
                    new TriggerKey(datasourceTask.getId().toString(), datasourceTask.getDsId().toString()),
                    ExtractDataJob.class,
                    datasourceTask.getCron(), new Date(datasourceTask.getStartTime()), endTime,
                    scheduleManager.getDefaultJobDataMap(datasourceTask.getDsId().toString(), datasourceTask.getCron(), datasourceTask.getId().toString(), datasourceTask.getUpdateType()));
        }
    }

    public void deleteSchedule(CoreDatasourceTask datasourceTask) {
        scheduleManager.removeJob(new JobKey(datasourceTask.getId().toString(), datasourceTask.getDsId().toString()), new TriggerKey(datasourceTask.getId().toString(), datasourceTask.getDsId().toString()));
    }

    public void fireNow(CoreDatasourceTask datasourceTask) throws Exception {
        scheduleManager.fireNow(datasourceTask.getId().toString(), datasourceTask.getDsId().toString());
    }
}
