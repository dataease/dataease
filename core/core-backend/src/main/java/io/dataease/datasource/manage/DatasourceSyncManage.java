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
import io.dataease.exception.DEException;
import io.dataease.job.sechedule.ExtractDataJob;
import io.dataease.job.sechedule.ScheduleManager;
import io.dataease.utils.JsonUtil;
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

import static io.dataease.datasource.server.DatasourceTaskServer.ScheduleType.CRON;
import static io.dataease.datasource.server.DatasourceTaskServer.ScheduleType.MANUAL;

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

    public void extractExcelData(CoreDatasource coreDatasource, String type) {
        if (coreDatasource == null) {
            LogUtil.error("Can not find CoreDatasource: " + coreDatasource.getName());
            return;
        }
        DatasourceServer.UpdateType updateType = DatasourceServer.UpdateType.valueOf(type);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(coreDatasource);
        List<DatasetTableDTO> tables = ExcelUtils.getTables(datasourceRequest);
        for (DatasetTableDTO tableDTO : tables) {
            CoreDatasourceTaskLog datasetTableTaskLog = datasourceTaskServer.initTaskLog(coreDatasource.getId(), null, tableDTO.getTableName(), CRON.toString());
            datasourceRequest.setTable(tableDTO.getTableName());
            List<TableField> tableFields = ExcelUtils.getTableFields(datasourceRequest);
            try {
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Begin to sync datatable: " + datasourceRequest.getTable());
                createEngineTable(datasourceRequest.getTable(), tableFields);
                if (updateType.equals(DatasourceServer.UpdateType.all_scope)) {
                    createEngineTable(TableUtils.tmpName(datasourceRequest.getTable()), tableFields);
                }
                extractExcelData(datasourceRequest, updateType);
                if (updateType.equals(DatasourceServer.UpdateType.all_scope)) {
                    replaceTable(datasourceRequest.getTable());
                }
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n End to sync datatable: " + datasourceRequest.getTable());
                datasetTableTaskLog.setTaskStatus(TaskStatus.Completed.toString());
            } catch (Exception e) {
                try {
                    if (updateType.equals(DatasourceServer.UpdateType.all_scope)) {
                        dropEngineTable(TableUtils.tmpName(datasourceRequest.getTable()));
                    }
                }catch (Exception ignore){}
                datasetTableTaskLog.setTaskStatus(TaskStatus.Error.toString());
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Failed to sync datatable: " + datasourceRequest.getTable() + ", " + e.getMessage());

            } finally {
                datasourceTaskServer.saveLog(datasetTableTaskLog);
            }
        }
    }


    public void extractData(Long datasourceId, Long taskId, JobExecutionContext context) {
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
        if (StringUtils.isNotEmpty(coreDatasourceTask.getTaskStatus()) && (coreDatasourceTask.getTaskStatus().equalsIgnoreCase(TaskStatus.Stopped.name()) || coreDatasourceTask.getTaskStatus().equalsIgnoreCase(TaskStatus.Suspend.name()))) {
            LogUtil.info("Skip synchronization task: {} ,due to task status is {}", coreDatasourceTask.getId(), coreDatasourceTask.getTaskStatus());
            return;
        }

        if (datasourceTaskServer.existUnderExecutionTask(datasourceId, coreDatasourceTask.getId())) {
            LogUtil.info("Skip synchronization task for datasource due to exist others, datasource ID : " + datasourceId);
            return;
        }

        DatasourceServer.UpdateType updateType = DatasourceServer.UpdateType.valueOf(coreDatasourceTask.getUpdateType());
        if (context != null) {
            UpdateWrapper<CoreDatasource> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", datasourceId);
            CoreDatasource record = new CoreDatasource();
            record.setQrtzInstance(context.getFireInstanceId());
            datasourceMapper.update(record, updateWrapper);
        }
        extractedData(taskId, coreDatasource, updateType, coreDatasourceTask.getSyncRate());
        try {
            datasourceTaskServer.updateTaskStatus(coreDatasourceTask);
            updateDsTaskStatus(datasourceId);
        } catch (Exception ignore) {
            LogUtil.error(ignore);
        }
    }

    public void extractedData(Long taskId, CoreDatasource coreDatasource, DatasourceServer.UpdateType updateType, String scheduleType ) {
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(coreDatasource);
        List<DatasetTableDTO> tables = ApiUtils.getTables(datasourceRequest);
        for (DatasetTableDTO api : tables) {
            CoreDatasourceTaskLog datasetTableTaskLog = datasourceTaskServer.initTaskLog(coreDatasource.getId(), taskId, api.getTableName(), scheduleType);
            datasourceRequest.setTable(api.getTableName());
            List<TableField> tableFields = ApiUtils.getTableFields(datasourceRequest);
            try {
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Begin to sync datatable: " + datasourceRequest.getTable());
                createEngineTable(datasourceRequest.getTable(), tableFields);
                if (updateType.equals(DatasourceServer.UpdateType.all_scope)) {
                    createEngineTable(TableUtils.tmpName(datasourceRequest.getTable()), tableFields);
                }
                extractApiData(datasourceRequest, updateType);
                if (updateType.equals(DatasourceServer.UpdateType.all_scope)) {
                    replaceTable(datasourceRequest.getTable());
                }
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n End to sync datatable: " + datasourceRequest.getTable());
                datasetTableTaskLog.setTaskStatus(TaskStatus.Completed.toString());
                datasetTableTaskLog.setEndTime(System.currentTimeMillis());
            } catch (Exception e) {
                try {
                    if (updateType.equals(DatasourceServer.UpdateType.all_scope)) {
                        dropEngineTable(TableUtils.tmpName(datasourceRequest.getTable()));
                    }
                } catch (Exception ignore) {
                }
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Failed to sync datatable: " + datasourceRequest.getTable() + ", " + e.getMessage());
                datasetTableTaskLog.setTaskStatus(TaskStatus.Error.toString());
                datasetTableTaskLog.setEndTime(System.currentTimeMillis());
            } finally {
                datasourceTaskServer.saveLog(datasetTableTaskLog);
            }
        }
    }

    private void updateDsTaskStatus(Long datasourceId){
        UpdateWrapper<CoreDatasource> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", datasourceId);
        CoreDatasource record = new CoreDatasource();
        record.setTaskStatus(TaskStatus.WaitingForExecution.name());
        datasourceMapper.update(record, updateWrapper);
    }

    public void extractDataForTable(Long datasourceId, String tableName, String type) {
        DatasourceServer.UpdateType updateType = DatasourceServer.UpdateType.valueOf(type);
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        if (coreDatasource == null) {
            LogUtil.error("Can not find datasource: " + datasourceId);
            return;
        }
        CoreDatasourceTaskLog datasetTableTaskLog = datasourceTaskServer.initTaskLog(datasourceId,  null, tableName, MANUAL.toString());

        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(coreDatasource);
        List<DatasetTableDTO> tables = ApiUtils.getTables(datasourceRequest);
        for (DatasetTableDTO api : tables) {
            if(api.getTableName().equalsIgnoreCase(tableName)){
                datasourceRequest.setTable(api.getTableName());
                List<TableField> tableFields = ApiUtils.getTableFields(datasourceRequest);
                try {
                    datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Begin to sync datatable: " + datasourceRequest.getTable());
                    createEngineTable(datasourceRequest.getTable(), tableFields);
                    if (updateType.equals(DatasourceServer.UpdateType.all_scope)) {
                        createEngineTable(TableUtils.tmpName(datasourceRequest.getTable()), tableFields);
                    }
                    extractApiData(datasourceRequest, updateType);
                    if (updateType.equals(DatasourceServer.UpdateType.all_scope)) {
                        replaceTable(datasourceRequest.getTable());
                    }
                    datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n End to sync datatable: " + datasourceRequest.getTable());
                    datasetTableTaskLog.setTaskStatus(TaskStatus.Completed.name());
                    datasetTableTaskLog.setEndTime(System.currentTimeMillis());
                } catch (Exception e) {
                    try {
                        if (updateType.equals(DatasourceServer.UpdateType.all_scope)) {
                            dropEngineTable(TableUtils.tmpName(datasourceRequest.getTable()));
                        }
                    }catch (Exception ignore){}
                    datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Failed to sync datatable: " + datasourceRequest.getTable() + ", " + e.getMessage());
                    datasetTableTaskLog.setTaskStatus(TaskStatus.Error.name());
                    datasetTableTaskLog.setEndTime(System.currentTimeMillis());
                }finally {
                    datasourceTaskServer.saveLog(datasetTableTaskLog);
                }
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

    public void dropEngineTable(String tableName) throws Exception{
        CoreDeEngine engine = engineServer.info();
        EngineRequest engineRequest = new EngineRequest();
        engineRequest.setEngine(engine);
        EngineProvider engineProvider = ProviderUtil.getEngineProvider(engine.getType());
        engineRequest.setQuery(engineProvider.dropTable(tableName));
        engineProvider.exec(engineRequest);
    }

    public void addSchedule(CoreDatasourceTask datasourceTask) throws DEException {
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
