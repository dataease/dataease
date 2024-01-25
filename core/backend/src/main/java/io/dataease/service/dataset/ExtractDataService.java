package io.dataease.service.dataset;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.commons.constants.*;
import io.dataease.commons.model.AuthURD;
import io.dataease.commons.utils.*;
import io.dataease.controller.request.datasource.ApiDefinition;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.dto.dataset.ExcelSheetData;
import io.dataease.dto.datasource.*;
import io.dataease.ext.ExtChartViewMapper;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.constants.DatasetType;
import io.dataease.plugins.common.constants.DatasourceTypes;
import io.dataease.plugins.common.constants.DeTypeConstants;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import io.dataease.plugins.datasource.provider.Provider;
import io.dataease.plugins.datasource.query.QueryProvider;
import io.dataease.provider.DDLProvider;
import io.dataease.provider.ProviderFactory;
import io.dataease.provider.datasource.JdbcProvider;
import io.dataease.service.datasource.DatasourceService;
import io.dataease.service.engine.EngineService;
import io.dataease.service.kettle.KettleService;
import io.dataease.service.message.DeMsgutil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.shell.JobEntryShell;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entries.success.JobEntrySuccess;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.excelinput.ExcelInputField;
import org.pentaho.di.trans.steps.excelinput.ExcelInputMeta;
import org.pentaho.di.trans.steps.excelinput.SpreadSheetType;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassDef;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassMeta;
import org.pentaho.di.www.SlaveServerJobStatus;
import org.pentaho.di.www.SlaveServerTransStatus;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ExtractDataService {

    @Resource
    @Lazy
    private DataSetTableService dataSetTableService;
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;
    @Resource
    @Lazy
    private DataSetTableTaskLogService dataSetTableTaskLogService;
    @Resource
    @Lazy
    private DataSetTableTaskService dataSetTableTaskService;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private ExtChartViewMapper extChartViewMapper;
    @Resource
    private EngineService engineService;
    @Resource
    private KettleService kettleService;


    private static final String lastUpdateTime = "${__last_update_time__}";
    private static final String currentUpdateTime = "${__current_update_time__}";
    private static final String separator = "|DE|";
    private static final String extension = "txt";
    private static final String root_path = "/opt/dataease/data/kettle/";

    @Value("${kettle.files.keep:false}")
    private boolean kettleFilesKeep;
    @Value("${extract.page.size:50000}")
    private Long extractPageSize;


    private static final String shellScript = "result=`curl --location-trusted -u %s:%s -H \"label:%s\" -H \"Expect:100-continue\" -H \"column_separator:%s\" -H \"columns:%s\" -H \"merge_type: %s\" -T %s -XPUT http://%s:%s/api/%s/%s/_stream_load`\n" +
            "if [ $? -eq 0 ] ; then\n" +
            "  failstatus=$(echo $result | grep '\"status\":\"FAILED\"')\n" +
            "  if [ \"x${failstatus}\" != \"x\" ];then" +
            "     echo $result\n" +
            "     exit 1\n" +
            "  fi\n" +
            "else\n" +
            "  echo $result\n" +
            "  exit 1\n" +
            "fi\n";

    private static final String shellScriptForDeleteFile = "result=`curl --location-trusted -u %s:%s -H \"label:%s\" -H \"Expect:100-continue\" -H \"column_separator:%s\" -H \"columns:%s\" -H \"merge_type: %s\" -T %s -XPUT http://%s:%s/api/%s/%s/_stream_load`\n" +
            "rm -rf  %s \n" +
            "if [ $? -eq 0 ] ; then\n" +
            "  failstatus=$(echo $result | grep '\"status\":\"FAILED\"')\n" +
            "  if [ \"x${failstatus}\" != \"x\" ];then" +
            "     echo $result\n" +
            "     exit 1\n" +
            "  fi\n" +
            "else\n" +
            "  echo $result\n" +
            "  exit 1\n" +
            "fi\n";

    public synchronized boolean existSyncTask(String datasetTableId, String datasetTableTaskId, Long startTime) {
        DatasetTable datasetTableRecord = new DatasetTable();
        datasetTableRecord.setSyncStatus(JobStatus.Underway.name());
        DatasetTableExample example = new DatasetTableExample();
        example.createCriteria().andIdEqualTo(datasetTableId).andSyncStatusNotEqualTo(JobStatus.Underway.name());
        example.or(example.createCriteria().andIdEqualTo(datasetTableId).andSyncStatusIsNull());
        boolean existSyncTask = dataSetTableService.updateByExampleSelective(datasetTableRecord, example) == 0;
        if (existSyncTask) {
            DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
            datasetTableTaskLog.setTaskId(datasetTableTaskId);
            datasetTableTaskLog.setTableId(datasetTableId);
            datasetTableTaskLog.setStatus(JobStatus.Underway.name());
            List<DatasetTableTaskLog> datasetTableTaskLogs = dataSetTableTaskLogService.select(datasetTableTaskLog);
            return CollectionUtils.isEmpty(datasetTableTaskLogs) || !datasetTableTaskLogs.get(0).getTriggerType().equalsIgnoreCase(TriggerType.Custom.name());
        } else {
            DatasetTableTask record = new DatasetTableTask();
            record.setLastExecTime(startTime);
            record.setLastExecStatus(JobStatus.Underway.name());
            record.setStatus(TaskStatus.Exec.name());
            DatasetTableTaskExample datasetTableTaskExample = new DatasetTableTaskExample();
            datasetTableTaskExample.createCriteria().andIdEqualTo(datasetTableTaskId);
            dataSetTableTaskService.updateByExampleSelective(record, datasetTableTaskExample);
            return false;
        }
    }

    public void extractExcelData(String datasetTableId, String type, String ops, List<DatasetTableField> datasetTableFields, List<String> datasetTableIds) {
        Datasource datasource = new Datasource();
        datasource.setType("excel");
        DatasetTable datasetTable = getDatasetTable(datasetTableId);
        if (datasetTable == null) {
            LogUtil.error("Can not find DatasetTable: " + datasetTableId);
            return;
        }
        UpdateType updateType = UpdateType.valueOf(type);

        if (datasetTableFields == null) {
            datasetTableFields = dataSetTableFieldsService.list(DatasetTableField.builder().tableId(datasetTable.getId()).build());
        }
        datasetTableFields = sortDatasetTableFields(datasetTable, datasetTableFields);

        DatasetTableTaskLog datasetTableTaskLog = writeExcelLog(datasetTableId, ops);
        switch (updateType) {
            case all_scope:  // 全量更新
                try {
                    createEngineTable(datasetTable.getInfo(), TableUtils.tableName(datasetTableId), datasetTableFields);
                    createEngineTable(datasetTable.getInfo(), TableUtils.tmpName(TableUtils.tableName(datasetTableId)), datasetTableFields);
                    Long execTime = System.currentTimeMillis();
                    if (!engineService.isSimpleMode()) {
                        generateTransFile("all_scope", datasetTable, datasource, datasetTableFields, null);
                        generateJobFile("all_scope", datasetTable, datasetTableFields.stream().map(DatasetTableField::getDataeaseName).collect(Collectors.joining(",")));
                        extractData(datasetTable, "all_scope");
                    } else {
                        extractExcelDataForSimpleMode(datasetTable, "all_scope", datasetTableFields);
                    }
                    replaceTable(TableUtils.tableName(datasetTableId));
                    saveSuccessLog(datasetTableTaskLog, false);
                    updateTableStatus(datasetTableId, JobStatus.Completed, execTime);
                    if (ops.equalsIgnoreCase("替换")) {
                        List<DatasetTableField> oldFields = getDatasetTableFields(datasetTable);
                        List<DatasetTableField> toAdd = new ArrayList<>();
                        List<DatasetTableField> toDelete = new ArrayList<>();
                        for (DatasetTableField oldField : oldFields) {
                            boolean delete = true;
                            for (DatasetTableField datasetTableField : datasetTableFields) {
                                if (!oldField.getDataeaseName().equalsIgnoreCase("dataease_uuid") && oldField.getDataeaseName().equalsIgnoreCase(datasetTableField.getDataeaseName()) && oldField.getType().equalsIgnoreCase(datasetTableField.getType())) {
                                    delete = false;
                                }
                            }
                            if (delete) {
                                toDelete.add(oldField);
                            }
                        }

                        for (DatasetTableField datasetTableField : datasetTableFields) {
                            boolean add = true;
                            for (DatasetTableField oldField : oldFields) {
                                if (oldField.getDataeaseName().equalsIgnoreCase(datasetTableField.getDataeaseName()) && oldField.getType().equalsIgnoreCase(datasetTableField.getType())) {
                                    add = false;
                                }
                            }
                            if (add) {
                                toAdd.add(datasetTableField);
                            }
                        }
                        toAdd.forEach(datasetTableField -> dataSetTableFieldsService.save(datasetTableField));
                        toDelete.forEach(datasetTableField -> dataSetTableFieldsService.delete(datasetTableField.getId()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    saveErrorLog(datasetTableTaskLog, e, false);
                    updateTableStatus(datasetTableId, JobStatus.Error, null);
                    dropDorisTable(TableUtils.tmpName(TableUtils.tableName(datasetTableId)));
                } finally {
                    deleteFile("all_scope", datasetTableId);
                    deleteExcelFile(datasetTable, datasetTableIds);
                }
                break;

            case add_scope: // 增量更新
                try {
                    Long execTime = System.currentTimeMillis();
                    if (!engineService.isSimpleMode()) {
                        generateTransFile("incremental_add", datasetTable, datasource, datasetTableFields, null);
                        generateJobFile("incremental_add", datasetTable, datasetTableFields.stream().map(DatasetTableField::getDataeaseName).collect(Collectors.joining(",")));
                        extractData(datasetTable, "incremental_add");
                    } else {
                        extractExcelDataForSimpleMode(datasetTable, "incremental_add", datasetTableFields);
                    }
                    saveSuccessLog(datasetTableTaskLog, false);
                    updateTableStatus(datasetTableId, JobStatus.Completed, execTime);
                } catch (Exception e) {
                    saveErrorLog(datasetTableTaskLog, e, false);
                    updateTableStatus(datasetTableId, JobStatus.Error, null);
                } finally {
                    deleteFile("incremental_add", datasetTableId);
                    deleteFile("incremental_delete", datasetTableId);
                    for (ExcelSheetData excelSheetData : new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getExcelSheetDataList()) {
                        deleteFile(excelSheetData.getPath());
                    }
                }
                break;
        }
        //侵入式清除下属视图缓存
        List<String> viewIds = extChartViewMapper.allViewIds(datasetTableId);
        if (CollectionUtils.isNotEmpty(viewIds)) {
            viewIds.forEach(viewId -> CacheUtils.remove(JdbcConstants.VIEW_CACHE_KEY, viewId));
        }
    }

    public void extractData(String datasetTableId, String taskId, String type, JobExecutionContext context) {
        DatasetTable datasetTable = getDatasetTable(datasetTableId);
        if (datasetTable == null) {
            LogUtil.error("Can not find DatasetTable: " + datasetTableId);
            return;
        }
        DatasetTableTask datasetTableTask = dataSetTableTaskService.selectByPrimaryKey(taskId);
        if (datasetTableTask == null) {
            return;
        }
        if (datasetTableTask.getStatus().equalsIgnoreCase(TaskStatus.Stopped.name()) || datasetTableTask.getStatus().equalsIgnoreCase(TaskStatus.Pending.name())) {
            LogUtil.info("Skip synchronization task: {} ,due to task status is {}", datasetTableTask.getId(), datasetTableTask.getStatus());
            dataSetTableTaskService.checkTaskIsStopped(datasetTableTask);
            return;
        }

        Long startTime = System.currentTimeMillis();
        if (existSyncTask(datasetTable.getId(), datasetTableTask.getId(), startTime)) {
            LogUtil.info("Skip synchronization task for dataset due to exist others, dataset ID : " + datasetTableId);
            return;
        }
        DatasetTableTaskLog datasetTableTaskLog = getDatasetTableTaskLog(datasetTableId, taskId, startTime);
        UpdateType updateType = UpdateType.valueOf(type);
        if (context != null) {
            DatasetTable datasetTableRecord = new DatasetTable();
            datasetTableRecord.setQrtzInstance(context.getFireInstanceId());
            DatasetTableExample example = new DatasetTableExample();
            example.createCriteria().andIdEqualTo(datasetTableId);
            dataSetTableService.updateByExampleSelective(datasetTableRecord, example);
        }
        Datasource datasource = new Datasource();
        if (StringUtils.isNotEmpty(datasetTable.getDataSourceId())) {
            datasource = datasourceService.get(datasetTable.getDataSourceId());
        } else {
            datasource.setType(datasetTable.getType());
        }
        List<DatasetTableField> datasetTableFields = getDatasetTableFields(datasetTable);
        boolean msg = false;
        JobStatus lastExecStatus = JobStatus.Completed;
        Long execTime = null;
        switch (updateType) {
            case all_scope:  // 全量更新
                try {
                    createEngineTable(datasetTable.getInfo(), TableUtils.tableName(datasetTableId), datasetTableFields);
                    createEngineTable(datasetTable.getInfo(), TableUtils.tmpName(TableUtils.tableName(datasetTableId)), datasetTableFields);
                    execTime = System.currentTimeMillis();
                    extractData(datasetTable, datasource, datasetTableFields, "all_scope", null);
                    replaceTable(TableUtils.tableName(datasetTableId));
                    saveSuccessLog(datasetTableTaskLog, true);
                    msg = true;
                    lastExecStatus = JobStatus.Completed;
                } catch (Exception e) {
                    saveErrorLog(datasetTableTaskLog, e, true);
                    msg = false;
                    lastExecStatus = JobStatus.Error;
                    execTime = null;
                } finally {
                    try {
                        deleteFile("all_scope", datasetTableId);
                    } catch (Exception ignore) {
                        LogUtil.error(ignore);
                    }
                    try {
                        sendWebMsg(datasetTable, datasetTableTask, datasetTableTaskLog, msg);
                    } catch (Exception ignore) {
                        LogUtil.error(ignore);
                    }
                    try {
                        updateTableStatus(datasetTableId, lastExecStatus, execTime);
                    } catch (Exception ignore) {
                        LogUtil.error(ignore);
                    }
                    try {
                        dataSetTableTaskService.updateTaskStatus(datasetTableTask, lastExecStatus);
                    } catch (Exception ignore) {
                        LogUtil.error(ignore);
                    }
                    try {
                        dropDorisTable(TableUtils.tmpName(TableUtils.tableName(datasetTableId)));
                    } catch (Exception ignore) {
                        LogUtil.error(ignore);
                    }
                }
                break;

            case add_scope: // 增量更新
                try {
                    if (datasetTable.getLastUpdateTime() == null || datasetTable.getLastUpdateTime() == 0) {
                        throw new Exception("未进行全量同步");
                    }
                    if (datasource.getType().equalsIgnoreCase(DatasourceTypes.api.name())) {
                        extractData(datasetTable, datasource, datasetTableFields, "incremental_add", null);
                    } else {
                        DatasetTableIncrementalConfig datasetTableIncrementalConfig = dataSetTableService.incrementalConfig(datasetTableId);


                        execTime = System.currentTimeMillis();
                        if (datasetTableIncrementalConfig != null && StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalAdd()) && StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalAdd().replace(" ", ""))) {// 增量添加
                            String sql = datasetTableIncrementalConfig.getIncrementalAdd().replace(lastUpdateTime, datasetTable.getLastUpdateTime().toString())
                                    .replace(currentUpdateTime, Long.valueOf(System.currentTimeMillis()).toString());
                            extractData(datasetTable, datasource, datasetTableFields, "incremental_add", sql);
                        }

                        if (StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalDelete()) && StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalDelete().replace(" ", ""))) {// 增量删除
                            String sql = datasetTableIncrementalConfig.getIncrementalDelete().replace(lastUpdateTime, datasetTable.getLastUpdateTime().toString())
                                    .replace(currentUpdateTime, Long.valueOf(System.currentTimeMillis()).toString());
                            extractData(datasetTable, datasource, datasetTableFields, "incremental_delete", sql);
                        }
                    }
                    saveSuccessLog(datasetTableTaskLog, true);
                    msg = true;
                    lastExecStatus = JobStatus.Completed;
                } catch (Exception e) {
                    saveErrorLog(datasetTableTaskLog, e, true);
                    msg = false;
                    lastExecStatus = JobStatus.Error;
                    execTime = null;
                } finally {
                    try {
                        deleteFile("incremental_add", datasetTableId);
                        deleteFile("incremental_delete", datasetTableId);
                    } catch (Exception ignore) {
                    }
                    try {
                        sendWebMsg(datasetTable, datasetTableTask, datasetTableTaskLog, msg);
                    } catch (Exception ignore) {
                    }
                    try {
                        updateTableStatus(datasetTableId, lastExecStatus, execTime);
                    } catch (Exception ignore) {
                    }
                    try {
                        dataSetTableTaskService.updateTaskStatus(datasetTableTask, lastExecStatus);
                    } catch (Exception ignore) {
                    }
                }
                break;
        }
        //侵入式清除下属视图缓存
        List<String> viewIds = extChartViewMapper.allViewIds(datasetTableId);
        if (CollectionUtils.isNotEmpty(viewIds)) {
            viewIds.forEach(viewId -> CacheUtils.remove(JdbcConstants.VIEW_CACHE_KEY, viewId));
        }

    }

    public List<DatasetTableField> getDatasetTableFields(DatasetTable datasetTable) {
        List<DatasetTableField> datasetTableFields = dataSetTableFieldsService.list(DatasetTableField.builder().tableId(datasetTable.getId()).build());
        return sortDatasetTableFields(datasetTable, datasetTableFields);
    }

    private List<DatasetTableField> sortDatasetTableFields(DatasetTable datasetTable, List<DatasetTableField> datasetTableFields) {
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
        datasetTableFields = datasetTableFields.stream().filter(datasetTableField -> datasetTableField.getExtField() == 0).collect(Collectors.toList());
        datasetTableFields.sort((o1, o2) -> {
            if (o1.getColumnIndex() == null) {
                return -1;
            }
            if (o2.getColumnIndex() == null) {
                return 1;
            }
            return o1.getColumnIndex().compareTo(o2.getColumnIndex());
        });
        if (dataTableInfoDTO.isSetKey() && CollectionUtils.isNotEmpty(dataTableInfoDTO.getKeys()) && !engineService.isSimpleMode()) {
            List<DatasetTableField> orderKeyDatasetTableFields = new ArrayList<>();
            for (int i = 0; i < dataTableInfoDTO.getKeys().size(); i++) {
                for (DatasetTableField datasetTableField : datasetTableFields) {
                    if (datasetTableField.getOriginName().equalsIgnoreCase(dataTableInfoDTO.getKeys().get(i))) {
                        orderKeyDatasetTableFields.add(datasetTableField);
                    }
                }
            }
            for (DatasetTableField datasetTableField : datasetTableFields) {
                if (!dataTableInfoDTO.getKeys().contains(datasetTableField.getOriginName())) {
                    orderKeyDatasetTableFields.add(datasetTableField);
                }
            }
            return orderKeyDatasetTableFields;
        } else {
            DatasetTableField datasetTableField = new DatasetTableField();
            datasetTableField.setDeExtractType(0);
            datasetTableField.setDataeaseName("dataease_uuid");
            datasetTableField.setOriginName("dataease_uuid");
            datasetTableField.setSize(0);
            datasetTableFields.add(0, datasetTableField);
            return datasetTableFields;
        }
    }

    private void extractData(DatasetTable datasetTable, Datasource datasource, List<DatasetTableField> datasetTableFields, String extractType, String selectSQL) throws Exception {
        if (datasource.getType().equalsIgnoreCase(DatasourceTypes.api.name())) {
            extractApiData(datasetTable, datasource, datasetTableFields, extractType);
            return;
        }
        Map<String, String> sql = getSelectSQL(extractType, datasetTable, datasource, datasetTableFields, selectSQL);
        if (StringUtils.isNotEmpty(sql.get("totalSql"))) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasource);
            Provider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
            datasourceRequest.setQuery(sql.get("totalSql"));
            List<String[]> tmpData = datasourceProvider.getData(datasourceRequest);
            Long totalItems = CollectionUtils.isEmpty(tmpData) ? 0 : Long.valueOf(tmpData.get(0)[0]);
            Long totalPage = (totalItems / extractPageSize) + (totalItems % extractPageSize > 0 ? 1 : 0);
            for (Long i = 0L; i < totalPage; i++) {
                Long offset = i * extractPageSize;
                Long all = offset + extractPageSize;
                extractDataByKettle(datasetTable, datasource, datasetTableFields, extractType, sql.get("selectSQL").replace("DE_OFFSET", offset.toString()).replace("DE_PAGE_SIZE", extractPageSize.toString()).replace("DE_ALL", all.toString()));
            }
        } else {
            extractDataByKettle(datasetTable, datasource, datasetTableFields, extractType, sql.get("selectSQL"));
        }
    }

    private void extractApiData(DatasetTable datasetTable, Datasource datasource, List<DatasetTableField> datasetTableFields, String extractType) throws Exception {
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
        List<ApiDefinition> lists = new Gson().fromJson(datasource.getConfiguration(), new TypeToken<ArrayList<ApiDefinition>>() {
        }.getType());
        lists = lists.stream().filter(item -> item.getName().equalsIgnoreCase(dataTableInfoDTO.getTable())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(lists)) {
            throw new Exception("未找到API数据表");
        }
        if (lists.size() > 1) {
            throw new Exception("存在重名的API数据表");
        }
        Provider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(datasource);
        datasourceRequest.setTable(new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getTable());
        Map<String, List> result = datasourceProvider.fetchResultAndField(datasourceRequest);
        List<String[]> dataList = result.get("dataList");
        if (engineService.isSimpleMode()) {
            extractDataForSimpleMode(extractType, datasetTable.getId(), dataList);
            return;
        }

        Datasource engine = engineService.getDeEngine();
        DorisConfiguration dorisConfiguration = new Gson().fromJson(engine.getConfiguration(), DorisConfiguration.class);
        String columns = datasetTableFields.stream().map(DatasetTableField::getDataeaseName).collect(Collectors.joining(","));

        String dataFile = null;
        String script = null;
        String streamLoadScript = "";
        if (kettleFilesKeep) {
            streamLoadScript = shellScript;
        } else {
            streamLoadScript = shellScriptForDeleteFile;
        }
        switch (extractType) {
            case "all_scope":
                dataFile = root_path + TableUtils.tmpName(TableUtils.tableName(datasetTable.getId())) + "." + extension;
                script = String.format(streamLoadScript, dorisConfiguration.getUsername(), dorisConfiguration.getPassword(), System.currentTimeMillis(), separator, columns, "APPEND", dataFile, dorisConfiguration.getHost(), dorisConfiguration.getHttpPort(), dorisConfiguration.getDataBase(), TableUtils.tmpName(TableUtils.tableName(datasetTable.getId())), dataFile);
                break;
            default:
                dataFile = root_path + TableUtils.addName(TableUtils.tableName(datasetTable.getId())) + "." + extension;
                script = String.format(streamLoadScript, dorisConfiguration.getUsername(), dorisConfiguration.getPassword(), System.currentTimeMillis(), separator, columns, "APPEND", dataFile, dorisConfiguration.getHost(), dorisConfiguration.getHttpPort(), dorisConfiguration.getDataBase(), TableUtils.tableName(datasetTable.getId()), dataFile);
                break;
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(dataFile));
        for (String[] strings : dataList) {
            String content = "";
            for (int i = 0; i < strings.length; i++) {
                content = i != strings.length - 1 ? content + strings[i] + separator : content + strings[i];
            }
            boolean isSetKey = dataTableInfoDTO.isSetKey() && CollectionUtils.isNotEmpty(dataTableInfoDTO.getKeys());
            if (!isSetKey) {
                content = Md5Utils.md5(content) + separator + content;
            }
            bw.write(content);
            bw.newLine();
        }
        bw.close();

        File scriptFile = new File(root_path + datasetTable.getId() + ".sh");
        scriptFile.createNewFile();
        scriptFile.setExecutable(true);

        BufferedWriter scriptFileBw = new BufferedWriter(new FileWriter(root_path + datasetTable.getId() + ".sh"));
        scriptFileBw.write("#!/bin/sh");
        scriptFileBw.newLine();
        scriptFileBw.write(script);
        scriptFileBw.newLine();
        scriptFileBw.close();

        try {
            Process process = Runtime.getRuntime().exec(root_path + datasetTable.getId() + ".sh");
            process.waitFor();
            if (process.waitFor() != 0) {
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                String errMsg = "";
                String line = "";
                while ((line = input.readLine()) != null) {
                    errMsg = errMsg + line + System.getProperty("line.separator");
                }
                input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while ((line = input.readLine()) != null) {
                    errMsg = errMsg + line + System.getProperty("line.separator");
                }
                throw new Exception(errMsg);
            }
        } catch (Exception e) {
            throw e;
        } finally {
//            File deleteFile = new File(root_path + datasetTable.getId() + ".sh");
//            FileUtils.forceDelete(deleteFile);
        }

    }

    private void extractDataForSimpleMode(String extractType, String datasetId, List<String[]> dataList) throws Exception {
        String tableName;
        switch (extractType) {
            case "all_scope":
                tableName = TableUtils.tmpName(TableUtils.tableName(datasetId));
                break;
            default:
                tableName = TableUtils.tableName(datasetId);
                break;
        }
        Datasource engine = engineService.getDeEngine();
        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(engine);
        DDLProvider ddlProvider = ProviderFactory.getDDLProvider(engine.getType());
        int pageNumber = 100; //一次插入 100条
        int totalPage;
        if (dataList.size() % pageNumber > 0) {
            totalPage = dataList.size() / pageNumber + 1;
        } else {
            totalPage = dataList.size() / pageNumber;
        }

        for (int page = 1; page <= totalPage; page++) {
            datasourceRequest.setQuery(ddlProvider.insertSql(tableName, dataList, page, pageNumber));
            jdbcProvider.exec(datasourceRequest);
        }
    }

    private void extractDataByKettle(DatasetTable datasetTable, Datasource datasource, List<DatasetTableField> datasetTableFields, String extractType, String selectSQL) throws Exception {
        generateTransFile(extractType, datasetTable, datasource, datasetTableFields, selectSQL);
        generateJobFile(extractType, datasetTable, datasetTableFields.stream().map(DatasetTableField::getDataeaseName).collect(Collectors.joining(",")));
        extractData(datasetTable, extractType);
    }

    private void sendWebMsg(DatasetTable datasetTable, DatasetTableTask datasetTableTask, DatasetTableTaskLog datasetTableTaskLog, Boolean status) {
        String taskId = datasetTableTask.getId();
        String msg = status ? "成功" : "失败";
        Long typeId = status ? 5L : 6L;
        String id = datasetTable.getId();
        AuthURD authURD = AuthUtils.authURDR(id);
        Set<Long> userIds = AuthUtils.userIdsByURD(authURD);

        Gson gson = new Gson();
        userIds.forEach(userId -> {
            Map<String, Object> param = new HashMap<>();
            param.put("tableId", id);
            if (StringUtils.isNotEmpty(taskId)) {
                param.put("taskId", taskId);
            }

            if (ObjectUtils.isNotEmpty(datasetTableTaskLog) && StringUtils.isNotEmpty(datasetTableTaskLog.getId())) {
                param.put("logId", datasetTableTaskLog.getId());
            }
            String content = "数据集【" + datasetTable.getName() + "】同步" + msg;
            if (ObjectUtils.isNotEmpty(datasetTableTask) && ObjectUtils.isNotEmpty(datasetTableTask.getName())) {
                content += " 任务名称【" + datasetTableTask.getName() + "】";
            }
            DeMsgutil.sendMsg(userId, typeId, content, gson.toJson(param));
        });
    }

    private void updateTableStatus(String datasetTableId, JobStatus jobStatus, Long execTime) {
        DatasetTable datasetTableRecord = new DatasetTable();
        datasetTableRecord.setId(datasetTableId);
        datasetTableRecord.setSyncStatus(jobStatus.name());
        if (execTime != null) {
            datasetTableRecord.setLastUpdateTime(execTime);
        }
        DatasetTableExample example = new DatasetTableExample();
        example.createCriteria().andIdEqualTo(datasetTableId);
        dataSetTableService.updateByExampleSelective(datasetTableRecord, example);
    }

    private void saveSuccessLog(DatasetTableTaskLog datasetTableTaskLog, Boolean hasTask) {
        datasetTableTaskLog.setStatus(JobStatus.Completed.name());
        datasetTableTaskLog.setEndTime(System.currentTimeMillis());
        dataSetTableTaskLogService.save(datasetTableTaskLog, hasTask);
    }

    private void saveErrorLog(DatasetTableTaskLog datasetTableTaskLog, Exception e, Boolean hasTask) {
        LogUtil.error("Extract data error: " + datasetTableTaskLog.getTaskId(), e);
        datasetTableTaskLog.setStatus(JobStatus.Error.name());
        datasetTableTaskLog.setInfo(e.getMessage());
        datasetTableTaskLog.setEndTime(System.currentTimeMillis());
        dataSetTableTaskLogService.save(datasetTableTaskLog, hasTask);
    }

    public void createEngineTable(String datasetTableInfo, String tableName, List<DatasetTableField> datasetTableFields) throws Exception {
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(datasetTableInfo, DataTableInfoDTO.class);
        Datasource engine = engineService.getDeEngine();
        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(engine);
        datasourceRequest.setQuery("SELECT VERSION()");
        String version = jdbcProvider.getData(datasourceRequest).get(0)[0];
        DDLProvider ddlProvider = ProviderFactory.getDDLProvider(engine.getType());
        datasourceRequest.setQuery(ddlProvider.createTableSql(dataTableInfoDTO, tableName, datasetTableFields, engine, version));
        jdbcProvider.exec(datasourceRequest);
    }

    private void dropDorisTable(String tableName) {
        try {
            Datasource engine = engineService.getDeEngine();
            JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(engine);
            DDLProvider ddlProvider = ProviderFactory.getDDLProvider(engine.getType());
            datasourceRequest.setQuery(ddlProvider.dropTable(tableName));
            jdbcProvider.exec(datasourceRequest);
        } catch (Exception ignore) {
        }
    }

    private void replaceTable(String dorisTableName) throws Exception {
        Datasource engine = engineService.getDeEngine();
        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(engine);
        DDLProvider ddlProvider = ProviderFactory.getDDLProvider(engine.getType());
        String[] replaceTableSql = ddlProvider.replaceTable(dorisTableName).split(";");
        for (int i = 0; i < replaceTableSql.length; i++) {
            if (StringUtils.isNotEmpty(replaceTableSql[i])) {
                datasourceRequest.setQuery(replaceTableSql[i]);
                jdbcProvider.exec(datasourceRequest);
            }
        }
    }

    private DatasetTable getDatasetTable(String datasetTableId) {
        for (int i = 0; i < 5; i++) {
            DatasetTable datasetTable = dataSetTableService.get(datasetTableId);
            if (datasetTable == null) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ignore) {
                }
            } else {
                return datasetTable;
            }
        }
        return null;
    }

    private DatasetTableTaskLog writeExcelLog(String datasetTableId, String taskId) {
        DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
        datasetTableTaskLog.setTableId(datasetTableId);
        datasetTableTaskLog.setTaskId(taskId);
        datasetTableTaskLog.setStatus(JobStatus.Underway.name());
        datasetTableTaskLog.setTriggerType(TriggerType.Cron.name());
        datasetTableTaskLog.setStartTime(System.currentTimeMillis());
        dataSetTableTaskLogService.save(datasetTableTaskLog, false);
        return datasetTableTaskLog;
    }

    private DatasetTableTaskLog getDatasetTableTaskLog(String datasetTableId, String taskId, Long startTime) {
        DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
        datasetTableTaskLog.setTableId(datasetTableId);
        datasetTableTaskLog.setTaskId(taskId);
        datasetTableTaskLog.setStatus(JobStatus.Underway.name());
        datasetTableTaskLog.setTriggerType(TriggerType.Custom.name());
        for (int i = 0; i < 5; i++) {
            List<DatasetTableTaskLog> datasetTableTaskLogs = dataSetTableTaskLogService.select(datasetTableTaskLog);
            if (CollectionUtils.isNotEmpty(datasetTableTaskLogs)) {
                return datasetTableTaskLogs.get(0);
            }
            try {
                Thread.sleep(1000);
            } catch (Exception ignore) {
            }
        }
        datasetTableTaskLog.setTriggerType(TriggerType.Cron.name());
        datasetTableTaskLog.setStartTime(startTime);
        dataSetTableTaskLogService.save(datasetTableTaskLog, true);
        return datasetTableTaskLog;
    }

    private void extractExcelDataForSimpleMode(DatasetTable datasetTable, String extractType, List<DatasetTableField> datasetTableFields) throws Exception {
        List<String[]> data = new ArrayList<>();
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
        List<ExcelSheetData> excelSheetDataList = dataTableInfoDTO.getExcelSheetDataList();
        for (ExcelSheetData excelSheetData : excelSheetDataList) {
            String suffix = excelSheetData.getPath().substring(excelSheetData.getPath().lastIndexOf(".") + 1);
            List<ExcelSheetData> totalSheets = new ArrayList<>();
            if (StringUtils.equalsIgnoreCase(suffix, "xls")) {
                ExcelXlsReader excelXlsReader = new ExcelXlsReader();
                excelXlsReader.process(new FileInputStream(excelSheetData.getPath()));
                totalSheets = excelXlsReader.totalSheets;
            }
            if (StringUtils.equalsIgnoreCase(suffix, "xlsx")) {
                totalSheets = dataSetTableService.excelSheetDataList(new FileInputStream(excelSheetData.getPath()), false);
            }

            if (StringUtils.equalsIgnoreCase(suffix, "csv")) {
                List<TableField> fields = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(excelSheetData.getPath()), StandardCharsets.UTF_8));
                String s = reader.readLine();// first line
                String[] split = s.split(",");
                for (String s1 : split) {
                    TableField tableFiled = new TableField();
                    tableFiled.setFieldName(s1);
                    tableFiled.setRemarks(s1);
                    tableFiled.setFieldType("TEXT");
                    fields.add(tableFiled);
                }
                List<List<String>> csvData = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {

                    String str;
                    line += ",";
                    Pattern pCells = Pattern.compile("(\"[^\"]*(\"{2})*[^\"]*\")*[^,]*,");
                    Matcher mCells = pCells.matcher(line);
                    List<String> cells = new ArrayList();//每行记录一个list
                    //读取每个单元格
                    while (mCells.find()) {
                        str = mCells.group();
                        str = str.replaceAll("(?sm)\"?([^\"]*(\"{2})*[^\"]*)\"?.*,", "$1");
                        str = str.replaceAll("(?sm)(\"(\"))", "$2");
                        cells.add(str);
                    }
                    csvData.add(cells);
                }
                ExcelSheetData csvSheetData = new ExcelSheetData();
                String[] fieldArray = fields.stream().map(TableField::getFieldName).toArray(String[]::new);
                csvSheetData.setFields(fields);
                csvSheetData.setData(csvData);
                csvSheetData.setExcelLabel(excelSheetData.getExcelLabel());
                csvSheetData.setFieldsMd5(Md5Utils.md5(StringUtils.join(fieldArray, ",")));
                totalSheets = Arrays.asList(csvSheetData);
            }


            for (ExcelSheetData sheet : totalSheets) {
                if (sheet.getExcelLabel().equalsIgnoreCase(excelSheetData.getExcelLabel())) {
                    for (List<String> dataItem : sheet.getData()) {
                        if (dataItem.size() > 0) {
                            data.add(dataItem.toArray(new String[dataItem.size()]));
                        }
                    }
                }
            }
        }
        extractDataForSimpleMode(extractType, datasetTable.getId(), data);
    }

    private void extractData(DatasetTable datasetTable, String extractType) throws Exception {
        if (StringUtils.isNotEmpty(datasetTable.getDataSourceId())) {
            datasourceService.validate(datasetTable.getDataSourceId());
        }
        KettleFileRepository repository = CommonBeanFactory.getBean(KettleFileRepository.class);
        RepositoryDirectoryInterface repositoryDirectoryInterface = repository.loadRepositoryDirectoryTree();
        TransMeta transMeta = null;
        JobMeta jobMeta = null;
        switch (extractType) {
            case "all_scope":
                jobMeta = repository.loadJob("job_" + TableUtils.tableName(datasetTable.getId()), repositoryDirectoryInterface, null, null);
                transMeta = repository.loadTransformation("trans_" + TableUtils.tableName(datasetTable.getId()), repositoryDirectoryInterface, null, true, "");
                break;
            case "incremental_add":
                jobMeta = repository.loadJob("job_add_" + TableUtils.tableName(datasetTable.getId()), repositoryDirectoryInterface, null, null);
                transMeta = repository.loadTransformation("trans_add_" + TableUtils.tableName(datasetTable.getId()), repositoryDirectoryInterface, null, true, "");
                break;
            case "incremental_delete":
                jobMeta = repository.loadJob("job_delete_" + TableUtils.tableName(datasetTable.getId()), repositoryDirectoryInterface, null, null);
                transMeta = repository.loadTransformation("trans_delete_" + TableUtils.tableName(datasetTable.getId()), repositoryDirectoryInterface, null, true, "");
                break;
            default:
                break;
        }

        SlaveServer remoteSlaveServer = kettleService.getSlaveServer();
        JobExecutionConfiguration jobExecutionConfiguration = new JobExecutionConfiguration();
        jobExecutionConfiguration.setRemoteServer(remoteSlaveServer);
        jobExecutionConfiguration.setRepository(repository);

        TransExecutionConfiguration transExecutionConfiguration = new TransExecutionConfiguration();
        transExecutionConfiguration.setRepository(repository);
        transExecutionConfiguration.setRemoteServer(remoteSlaveServer);
        String lastTranceId = Trans.sendToSlaveServer(transMeta, transExecutionConfiguration, repository, null);
        SlaveServerTransStatus transStatus = null;
        boolean executing = true;
        while (executing) {
            transStatus = remoteSlaveServer.getTransStatus(transMeta.getName(), lastTranceId, 0);
            executing = transStatus.isRunning() || transStatus.isWaiting();
            if (!executing)
                break;
            Thread.sleep(1000);
        }
        if (!transStatus.getStatusDescription().equals("Finished")) {
            DataEaseException.throwException(transStatus.getLoggingString());
            return;
        }
        executing = true;
        String lastCarteObjectId = Job.sendToSlaveServer(jobMeta, jobExecutionConfiguration, repository, null);
        SlaveServerJobStatus jobStatus = null;

        while (executing) {
            jobStatus = remoteSlaveServer.getJobStatus(jobMeta.getName(), lastCarteObjectId, 0);
            executing = jobStatus.isRunning() || jobStatus.isWaiting();
            if (!executing)
                break;
            Thread.sleep(1000);
        }
        if (jobStatus.getStatusDescription().equals("Finished")) {
            LogUtil.info(datasetTable.getId() + ": " + jobStatus.getLoggingString());
            return;
        } else {
            DataEaseException.throwException(jobStatus.getLoggingString());
        }
    }

    private void generateJobFile(String extractType, DatasetTable datasetTable, String columnFields) throws Exception {
        if (engineService.isSimpleMode()) {
            return;
        }
        String outFile;
        String jobName = null;
        String script = null;
        Datasource dorisDatasource = engineService.getDeEngine();
        DorisConfiguration dorisConfiguration = new Gson().fromJson(dorisDatasource.getConfiguration(), DorisConfiguration.class);
        String columns = columnFields;
        String streamLoadScript = "";
        if (kettleFilesKeep) {
            streamLoadScript = shellScript;
        } else {
            streamLoadScript = shellScriptForDeleteFile;
        }
        switch (extractType) {
            case "all_scope":
                outFile = TableUtils.tmpName(TableUtils.tableName(datasetTable.getId()));
                jobName = "job_" + TableUtils.tableName(datasetTable.getId());
                script = String.format(streamLoadScript, dorisConfiguration.getUsername(), dorisConfiguration.getPassword(), datasetTable.getId() + System.currentTimeMillis(), separator, columns, "APPEND", root_path + outFile + "." + extension, dorisConfiguration.getHost(), dorisConfiguration.getHttpPort(), dorisConfiguration.getDataBase(), TableUtils.tmpName(TableUtils.tableName(datasetTable.getId())), root_path + outFile + "." + extension);
                break;
            case "incremental_add":
                outFile = TableUtils.addName(datasetTable.getId());
                jobName = "job_add_" + TableUtils.tableName(datasetTable.getId());
                script = String.format(streamLoadScript, dorisConfiguration.getUsername(), dorisConfiguration.getPassword(), datasetTable.getId() + System.currentTimeMillis(), separator, columns, "APPEND", root_path + outFile + "." + extension, dorisConfiguration.getHost(), dorisConfiguration.getHttpPort(), dorisConfiguration.getDataBase(), TableUtils.tableName(datasetTable.getId()), root_path + outFile + "." + extension);
                break;
            case "incremental_delete":
                outFile = TableUtils.deleteName(TableUtils.tableName(datasetTable.getId()));
                script = String.format(streamLoadScript, dorisConfiguration.getUsername(), dorisConfiguration.getPassword(), datasetTable.getId() + System.currentTimeMillis(), separator, columns, "DELETE", root_path + outFile + "." + extension, dorisConfiguration.getHost(), dorisConfiguration.getHttpPort(), dorisConfiguration.getDataBase(), TableUtils.tableName(datasetTable.getId()), root_path + outFile + "." + extension);
                jobName = "job_delete_" + TableUtils.tableName(datasetTable.getId());
                break;
            default:
                break;
        }

        JobMeta jobMeta = new JobMeta();
        jobMeta.setName(jobName);

        //start
        JobEntrySpecial start = new JobEntrySpecial();
        start.setName("START");
        start.setStart(true);
        JobEntryCopy startEntry = new JobEntryCopy(start);
        startEntry.setDrawn(true);
        startEntry.setLocation(100, 100);
        jobMeta.addJobEntry(startEntry);

        //exec shell
        JobEntryShell shell = new JobEntryShell();
        shell.setScript(script);
        shell.insertScript = true;
        shell.setName("shell");
        JobEntryCopy shellEntry = new JobEntryCopy(shell);
        shellEntry.setDrawn(true);
        shellEntry.setLocation(500, 100);
        jobMeta.addJobEntry(shellEntry);

        JobHopMeta transHop = new JobHopMeta(startEntry, shellEntry);
        transHop.setEvaluation(true);
        jobMeta.addJobHop(transHop);

        //success
        JobEntrySuccess success = new JobEntrySuccess();
        success.setName("Success");
        JobEntryCopy successEntry = new JobEntryCopy(success);
        successEntry.setDrawn(true);
        successEntry.setLocation(700, 100);
        jobMeta.addJobEntry(successEntry);


        JobHopMeta greenHop = new JobHopMeta(shellEntry, successEntry);
        greenHop.setEvaluation(true);
        jobMeta.addJobHop(greenHop);

        String jobXml = jobMeta.getXML();
        File file = new File(root_path + jobName + ".kjb");
        FileUtils.writeStringToFile(file, jobXml, "UTF-8");
    }

    private String fetchSqlField(String sql, Datasource ds) throws Exception {
        QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
        Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceRequest.setQuery(qp.wrapSql(sql));
        List<String> dorisFields = new ArrayList<>();
        datasourceProvider.fetchResultField(datasourceRequest).stream().map(TableField::getFieldName).forEach(field -> {
            dorisFields.add(TableUtils.columnName(field));
        });
        return String.join(",", dorisFields);
    }

    private void generateTransFile(String extractType, DatasetTable datasetTable, Datasource datasource, List<DatasetTableField> datasetTableFields, String selectSQL) throws Exception {
        if (engineService.isSimpleMode()) {
            return;
        }
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
        boolean isSetKey = dataTableInfoDTO.isSetKey() && CollectionUtils.isNotEmpty(dataTableInfoDTO.getKeys());
        TransMeta transMeta = new TransMeta();
        String outFile = null;
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasource.getType());
        DatabaseMeta dataMeta;
        List<StepMeta> inputSteps = new ArrayList<>();
        StepMeta outputStep;
        StepMeta udjcStep = null;

        TransHopMeta hi2;
        String transName = null;

        switch (datasourceType) {
            case ds_doris:
            case mariadb:
            case mysql:
            case TiDB:
            case StarRocks:
                MysqlConfiguration mysqlConfiguration = new Gson().fromJson(datasource.getConfiguration(), MysqlConfiguration.class);
                dataMeta = new DatabaseMeta("db", "MYSQL", "Native", mysqlConfiguration.getHost().trim(), mysqlConfiguration.getDataBase().trim(), mysqlConfiguration.getPort().toString(), mysqlConfiguration.getUsername(), mysqlConfiguration.getPassword());
                if (StringUtils.isNotEmpty(mysqlConfiguration.getExtraParams()) && mysqlConfiguration.getExtraParams().split("&").length > 0) {
                    String[] params = mysqlConfiguration.getExtraParams().split("&");
                    for (int i = 0; i < params.length; i++) {
                        dataMeta.addExtraOption("MYSQL", params[i].split("=")[0], params[i].split("=")[1]);
                    }
                }
                transMeta.addDatabase(dataMeta);
                inputSteps = inputStep(transMeta, selectSQL, mysqlConfiguration);
                udjcStep = udjc(datasetTableFields, DatasourceTypes.mysql, mysqlConfiguration, isSetKey);
                break;
            case sqlServer:
                SqlServerConfiguration sqlServerConfiguration = new Gson().fromJson(datasource.getConfiguration(), SqlServerConfiguration.class);
                dataMeta = new DatabaseMeta("db", "MSSQLNATIVE", "Native", sqlServerConfiguration.getHost().trim(), sqlServerConfiguration.getDataBase(), sqlServerConfiguration.getPort().toString(), sqlServerConfiguration.getUsername(), sqlServerConfiguration.getPassword());
                transMeta.addDatabase(dataMeta);
                inputSteps = inputStep(transMeta, selectSQL, sqlServerConfiguration);
                udjcStep = udjc(datasetTableFields, DatasourceTypes.sqlServer, sqlServerConfiguration, isSetKey);
                break;
            case pg:
                PgConfiguration pgConfiguration = new Gson().fromJson(datasource.getConfiguration(), PgConfiguration.class);
                dataMeta = new DatabaseMeta("db", "POSTGRESQL", "Native", pgConfiguration.getHost().trim(), pgConfiguration.getDataBase(), pgConfiguration.getPort().toString(), pgConfiguration.getUsername(), pgConfiguration.getPassword());
                transMeta.addDatabase(dataMeta);
                inputSteps = inputStep(transMeta, selectSQL, pgConfiguration);
                udjcStep = udjc(datasetTableFields, DatasourceTypes.pg, pgConfiguration, isSetKey);
                break;
            case oracle:
                OracleConfiguration oracleConfiguration = new Gson().fromJson(datasource.getConfiguration(), OracleConfiguration.class);
                if (oracleConfiguration.getConnectionType().equalsIgnoreCase("serviceName")) {
                    String database = "(DESCRIPTION =(ADDRESS = (PROTOCOL = TCP)(HOST = ORACLE_HOSTNAME)(PORT = ORACLE_PORT))(CONNECT_DATA = (SERVER = DEDICATED)(SERVICE_NAME = ORACLE_SERVICE_NAME )))".replace("ORACLE_HOSTNAME", oracleConfiguration.getHost()).replace("ORACLE_PORT", oracleConfiguration.getPort().toString()).replace("ORACLE_SERVICE_NAME", oracleConfiguration.getDataBase());
                    dataMeta = new DatabaseMeta("db", "ORACLE", "Native", "", database, "-1", oracleConfiguration.getUsername(), oracleConfiguration.getPassword());
                } else {
                    dataMeta = new DatabaseMeta("db", "ORACLE", "Native", oracleConfiguration.getHost().trim(), oracleConfiguration.getDataBase(), oracleConfiguration.getPort().toString(), oracleConfiguration.getUsername(), oracleConfiguration.getPassword());
                }
                transMeta.addDatabase(dataMeta);
                inputSteps = inputStep(transMeta, selectSQL, oracleConfiguration);
                udjcStep = udjc(datasetTableFields, DatasourceTypes.oracle, oracleConfiguration, isSetKey);
                break;
            case ck:
                CHConfiguration chConfiguration = new Gson().fromJson(datasource.getConfiguration(), CHConfiguration.class);
                dataMeta = new DatabaseMeta("db", "ORACLE", "Native", chConfiguration.getHost().trim(), chConfiguration.getDataBase().trim(), chConfiguration.getPort().toString(), chConfiguration.getUsername(), chConfiguration.getPassword());
                dataMeta.setDatabaseType("Clickhouse");
                transMeta.addDatabase(dataMeta);
                inputSteps = inputStep(transMeta, selectSQL, chConfiguration);
                udjcStep = udjc(datasetTableFields, DatasourceTypes.ck, chConfiguration, isSetKey);
                break;
            case db2:
                Db2Configuration db2Configuration = new Gson().fromJson(datasource.getConfiguration(), Db2Configuration.class);
                dataMeta = new DatabaseMeta("db", "DB2", "Native", db2Configuration.getHost().trim(), db2Configuration.getDataBase().trim(), db2Configuration.getPort().toString(), db2Configuration.getUsername(), db2Configuration.getPassword());
                dataMeta.setDatabaseType("DB2");
                transMeta.addDatabase(dataMeta);
                inputSteps = inputStep(transMeta, selectSQL, db2Configuration);
                udjcStep = udjc(datasetTableFields, DatasourceTypes.db2, db2Configuration, isSetKey);
                break;
            case excel:
                inputSteps = excelInputStep(datasetTable.getInfo(), datasetTableFields);
                udjcStep = udjc(datasetTableFields, DatasourceTypes.excel, null, isSetKey);
            default:
                break;
        }

        switch (extractType) {
            case "all_scope":
                transName = "trans_" + TableUtils.tableName(datasetTable.getId());
                outFile = TableUtils.tmpName(TableUtils.tableName(datasetTable.getId()));
                transMeta.setName(transName);
                break;
            case "incremental_add":
                transName = "trans_add_" + TableUtils.tableName(datasetTable.getId());
                outFile = TableUtils.addName(datasetTable.getId());
                transMeta.setName(transName);
                break;
            case "incremental_delete":
                transName = "trans_delete_" + TableUtils.tableName(datasetTable.getId());
                outFile = TableUtils.deleteName(TableUtils.tableName(datasetTable.getId()));
                transMeta.setName(transName);
                break;
            default:
                break;
        }

        outputStep = outputStep(outFile, datasetTableFields, datasource);

        for (StepMeta inputStep : inputSteps) {
            TransHopMeta hi1 = new TransHopMeta(inputStep, udjcStep);
            transMeta.addTransHop(hi1);
            transMeta.addStep(inputStep);
        }

        hi2 = new TransHopMeta(udjcStep, outputStep);
        transMeta.addTransHop(hi2);
        transMeta.addStep(udjcStep);
        transMeta.addStep(outputStep);

        String transXml = transMeta.getXML();
        File file = new File(root_path + transName + ".ktr");
        FileUtils.writeStringToFile(file, transXml, "UTF-8");
    }

    private Map<String, String> getSelectSQL(String extractType, DatasetTable datasetTable, Datasource datasource, List<DatasetTableField> datasetTableFields, String selectSQL) {
        List<DatasetTableField> fields = datasetTableFields.stream().filter(datasetTableField -> !datasetTableField.getDataeaseName().equalsIgnoreCase("dataease_uuid")).collect(Collectors.toList());
        Map<String, String> sql = new HashMap<>();
        if (extractType.equalsIgnoreCase("all_scope") && datasetTable.getType().equalsIgnoreCase(DatasetType.DB.name())) {
            String tableName = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getTable();
            QueryProvider qp = ProviderFactory.getQueryProvider(datasource.getType());
            sql.put("selectSQL", qp.createRawQuerySQL(tableName, fields, datasource));
            sql.put("totalSql", qp.getTotalCount(true, tableName, datasource));
        }

        if (extractType.equalsIgnoreCase("all_scope") && datasetTable.getType().equalsIgnoreCase(DatasetType.SQL.name())) {
            DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
            selectSQL = dataTableInfoDTO.getSql();
            if (dataTableInfoDTO.isBase64Encryption()) {
                selectSQL = new String(java.util.Base64.getDecoder().decode(selectSQL));
            }
            QueryProvider qp = ProviderFactory.getQueryProvider(datasource.getType());
            sql.put("totalSql", qp.getTotalCount(false, selectSQL, datasource));
            sql.put("selectSQL", qp.createRawQuerySQLAsTmp(selectSQL, fields));
        }

        if (!extractType.equalsIgnoreCase("all_scope")) {
            QueryProvider qp = ProviderFactory.getQueryProvider(datasource.getType());
            sql.put("totalSql", qp.getTotalCount(false, selectSQL, datasource));
            sql.put("selectSQL", qp.createRawQuerySQLAsTmp(selectSQL, fields));
        }

        return sql;
    }

    private List<StepMeta> inputStep(TransMeta transMeta, String selectSQL, JdbcConfiguration jdbcConfiguration) {
        TableInputMeta tableInput = new TableInputMeta();
        DatabaseMeta database = transMeta.findDatabase(DatasetType.DB.name());
        tableInput.setDatabaseMeta(database);
        tableInput.setSQL(selectSQL);
        StepMeta fromStep = new StepMeta("TableInput", "Data Input", tableInput);
        fromStep.setDraw(true);
        fromStep.setLocation(100, 100);
        List<StepMeta> inputSteps = new ArrayList<>();
        inputSteps.add(fromStep);
        return inputSteps;
    }

    private List<StepMeta> excelInputStep(String Info, List<DatasetTableField> datasetTableFields) {
        List<StepMeta> inputSteps = new ArrayList<>();
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(Info, DataTableInfoDTO.class);
        List<ExcelSheetData> excelSheetDataList = dataTableInfoDTO.getExcelSheetDataList();

        List<String> sheetNames = new ArrayList<>();

        int size = 1;
        for (ExcelSheetData excelSheetData : excelSheetDataList) {
            StepMeta fromStep = null;
            String suffix = excelSheetData.getPath().substring(excelSheetDataList.get(0).getPath().lastIndexOf(".") + 1);

            if (StringUtils.equalsIgnoreCase(suffix, "csv")) {
                CsvInputMeta csvInputMeta = new CsvInputMeta();
                csvInputMeta.setFilename(excelSheetData.getPath());
                csvInputMeta.setHeaderPresent(true);
                csvInputMeta.setBufferSize("10000");
                csvInputMeta.setDelimiter(",");
                TextFileInputField[] fields = new TextFileInputField[datasetTableFields.size()];
                for (int i = 0; i < datasetTableFields.size(); i++) {
                    TextFileInputField field = new TextFileInputField();
                    field.setName(datasetTableFields.get(i).getDataeaseName());
                    if (datasetTableFields.get(i).getDeExtractType() == 1) {
                        field.setType(ValueMeta.getType("String"));
                        field.setFormat("yyyy-MM-dd HH:mm:ss");
                    } else {
                        field.setType(ValueMeta.getType("String"));
                    }
                    fields[i] = field;
                }
                csvInputMeta.setInputFields(fields);
                fromStep = new StepMeta("CsvInput", "Data Input " + size, csvInputMeta);
                fromStep.setDraw(true);
                fromStep.setLocation(100, 100 * size);
                inputSteps.add(fromStep);
            } else {
                List<String> files = new ArrayList<>();
                files.add(excelSheetData.getPath());

                List<String> filesRequired = new ArrayList<>();
                filesRequired.add("Y");

                ExcelInputMeta excelInputMeta = new ExcelInputMeta();
                sheetNames.add(excelSheetData.getExcelLabel());
                if (StringUtils.equalsIgnoreCase(suffix, "xlsx")) {
                    excelInputMeta.setSpreadSheetType(SpreadSheetType.SAX_POI);
                    excelInputMeta.setSheetName(sheetNames.toArray(new String[sheetNames.size()]));
                }
                if (StringUtils.equalsIgnoreCase(suffix, "xls")) {
                    excelInputMeta.setSpreadSheetType(SpreadSheetType.JXL);
                    excelInputMeta.setSheetName(sheetNames.toArray(new String[sheetNames.size()]));
                }
                excelInputMeta.setPassword("Encrypted");
                excelInputMeta.setFileName(files.toArray(new String[files.size()]));
                excelInputMeta.setFileRequired(filesRequired.toArray(new String[filesRequired.size()]));
                excelInputMeta.setStartsWithHeader(true);
                excelInputMeta.setIgnoreEmptyRows(true);
                ExcelInputField[] fields = new ExcelInputField[datasetTableFields.size()];
                for (int i = 0; i < datasetTableFields.size(); i++) {
                    ExcelInputField field = new ExcelInputField();
                    field.setName(datasetTableFields.get(i).getDataeaseName());
                    if (datasetTableFields.get(i).getDeExtractType() == 1) {
                        field.setType("String");
                        field.setFormat("yyyy-MM-dd HH:mm:ss");
                    } else {
                        field.setType("String");
                    }
                    fields[i] = field;
                }
                excelInputMeta.setField(fields);
                fromStep = new StepMeta("ExcelInput", "Data Input " + size, excelInputMeta);
                fromStep.setDraw(true);
                fromStep.setLocation(100, 100 * size);
                inputSteps.add(fromStep);
            }
            size++;
        }
        return inputSteps;
    }

    private StepMeta outputStep(String dorisOutputTable, List<DatasetTableField> datasetTableFields, Datasource datasource) {
        TextFileOutputMeta textFileOutputMeta = new TextFileOutputMeta();
        textFileOutputMeta.setEncoding("UTF-8");
        textFileOutputMeta.setHeaderEnabled(false);
        textFileOutputMeta.setFilename(root_path + dorisOutputTable);
        textFileOutputMeta.setSeparator(separator);
        textFileOutputMeta.setExtension(extension);

        if (datasource.getType().equalsIgnoreCase(DatasourceTypes.oracle.name())) {
            TextFileField[] outputFields = new TextFileField[datasetTableFields.size()];
            for (int i = 0; i < datasetTableFields.size(); i++) {
                TextFileField textFileField = new TextFileField();
                textFileField.setName(datasetTableFields.get(i).getOriginName());
                textFileField.setType("String");
                outputFields[i] = textFileField;
            }
            textFileOutputMeta.setOutputFields(outputFields);
        } else if (datasource.getType().equalsIgnoreCase(DatasourceTypes.sqlServer.name()) || datasource.getType().equalsIgnoreCase(DatasourceTypes.pg.name()) || datasource.getType().equalsIgnoreCase(DatasourceTypes.mysql.name())) {
            TextFileField[] outputFields = new TextFileField[datasetTableFields.size()];
            for (int i = 0; i < datasetTableFields.size(); i++) {
                TextFileField textFileField = new TextFileField();
                textFileField.setName(datasetTableFields.get(i).getDataeaseName());
                if (datasetTableFields.get(i).getDeExtractType().equals(DeTypeConstants.DE_TIME)) {
                    textFileField.setType("String");
                    textFileField.setFormat("yyyy-MM-dd HH:mm:ss");
                } else {
                    textFileField.setType("String");
                }

                outputFields[i] = textFileField;
            }
            textFileOutputMeta.setOutputFields(outputFields);
        } else if (datasource.getType().equalsIgnoreCase(DatasourceTypes.excel.name())) {
            TextFileField[] outputFields = new TextFileField[datasetTableFields.size()];
            for (int i = 0; i < datasetTableFields.size(); i++) {
                TextFileField textFileField = new TextFileField();
                textFileField.setName(datasetTableFields.get(i).getDataeaseName());
                if (datasetTableFields.get(i).getDeExtractType().equals(DeTypeConstants.DE_INT)) {
                    textFileField.setType("Integer");
                    textFileField.setFormat("0");
                } else {
                    textFileField.setType("String");
                }

                outputFields[i] = textFileField;
            }

            textFileOutputMeta.setOutputFields(outputFields);
        } else {
            textFileOutputMeta.setOutputFields(new TextFileField[0]);
        }

        StepMeta outputStep = new StepMeta("TextFileOutput", "TextFileOutput", textFileOutputMeta);
        outputStep.setLocation(600, 100);
        outputStep.setDraw(true);
        return outputStep;
    }

    private StepMeta udjc(List<DatasetTableField> datasetTableFields, DatasourceTypes datasourceType, JdbcConfiguration jdbcConfiguration, boolean isSetKey) {
        StringBuilder handleBinaryTypeCode = new StringBuilder();
        String excelCompletion = "";

        for (DatasetTableField datasetTableField : datasetTableFields) {
            if (datasetTableField.getDeExtractType().equals(DeTypeConstants.DE_BINARY) || datasetTableField.getType().equalsIgnoreCase("blob")) {
                handleBinaryTypeCode.append("\n").append(handleBinaryType.replace("FIELD", datasetTableField.getDataeaseName()));
            }
        }

        UserDefinedJavaClassMeta userDefinedJavaClassMeta = new UserDefinedJavaClassMeta();
        List<UserDefinedJavaClassMeta.FieldInfo> fields = new ArrayList<>();
        if (!isSetKey) {
            UserDefinedJavaClassMeta.FieldInfo fieldInfo = new UserDefinedJavaClassMeta.FieldInfo("dataease_uuid", ValueMetaInterface.TYPE_STRING, -1, -1);
            fields.add(fieldInfo);
        }
        userDefinedJavaClassMeta.setFieldInfo(fields);
        List<UserDefinedJavaClassDef> definitions = new ArrayList<>();
        String tmp_code = code.replace("handleWraps", handleWraps).replace("handleBinaryType", handleBinaryTypeCode.toString());
        if (!isSetKey) {
            tmp_code = tmp_code.replace("handleDataease_uuid", "");
        } else {
            tmp_code = tmp_code.replace("handleDataease_uuid", handleDataease_uuid);
        }
        String Column_Fields;

        if (datasourceType.equals(DatasourceTypes.oracle) || datasourceType.equals(DatasourceTypes.db2)) {
            Column_Fields = datasetTableFields.stream().map(DatasetTableField::getOriginName).collect(Collectors.joining(","));
            String charset = null;
            String targetCharset = "UTF-8";
            if (StringUtils.isNotEmpty(jdbcConfiguration.getCharset()) && !jdbcConfiguration.getCharset().equalsIgnoreCase("Default")) {
                charset = jdbcConfiguration.getCharset();
            }
            if (StringUtils.isNotEmpty(jdbcConfiguration.getTargetCharset()) && !jdbcConfiguration.getTargetCharset().equalsIgnoreCase("Default")) {
                targetCharset = jdbcConfiguration.getTargetCharset();
            }
            if (StringUtils.isNotEmpty(charset)) {
                String varcharFields = datasetTableFields.stream().filter(datasetTableField -> datasetTableField.getDeExtractType() == 0).map(DatasetTableField::getOriginName).collect(Collectors.joining(","));
                tmp_code = tmp_code.replace("handleCharset", handleCharset.replace("Datasource_Charset", charset).replace("Target_Charset", targetCharset).replace("varcharFields", varcharFields));
            } else {
                tmp_code = tmp_code.replace("handleCharset", "");
            }
        } else {
            Column_Fields = datasetTableFields.stream().map(DatasetTableField::getDataeaseName).collect(Collectors.joining(","));
            tmp_code = tmp_code.replace("handleCharset", "");
        }

        if (datasourceType.equals(DatasourceTypes.excel)) {
            tmp_code = tmp_code.replace("handleExcelIntColumn", handleExcelIntColumn).replace("Column_Fields", Column_Fields)
                    .replace("ExcelCompletion", excelCompletion);
        } else {
            tmp_code = tmp_code.replace("handleExcelIntColumn", "").replace("Column_Fields", Column_Fields)
                    .replace("ExcelCompletion", "");
        }

        String handleMysqlBIGINTUNSIGNEDStr = "";
        if (datasourceType.equals(DatasourceTypes.mysql)) {
            for (DatasetTableField datasetTableField : datasetTableFields) {
                if (datasetTableField.getType().equalsIgnoreCase("BIGINT UNSIGNED")) {
                    handleMysqlBIGINTUNSIGNEDStr = handleMysqlBIGINTUNSIGNEDStr + handleMysqlBIGINTUNSIGNED.replace("BIGINTUNSIGNEDFIELD", datasetTableField.getDataeaseName()) + "; \n";
                }
            }
        }
        tmp_code = tmp_code.replace("handleMysqlBIGINTUNSIGNED", handleMysqlBIGINTUNSIGNEDStr);


        UserDefinedJavaClassDef userDefinedJavaClassDef = new UserDefinedJavaClassDef(UserDefinedJavaClassDef.ClassType.TRANSFORM_CLASS, "Processor", tmp_code);

        userDefinedJavaClassDef.setActive(true);
        definitions.add(userDefinedJavaClassDef);
        userDefinedJavaClassMeta.replaceDefinitions(definitions);

        StepMeta userDefinedJavaClassStep = new StepMeta("UserDefinedJavaClass", "UserDefinedJavaClass", userDefinedJavaClassMeta);
        userDefinedJavaClassStep.setLocation(300, 100);
        userDefinedJavaClassStep.setDraw(true);
        return userDefinedJavaClassStep;
    }

    public void deleteFile(String type, String dataSetTableId) {
        if (kettleFilesKeep) {
            return;
        }
        String transName = null;
        String jobName = null;
        String fileName = null;

        switch (type) {
            case "all_scope":
                transName = "trans_" + TableUtils.tableName(dataSetTableId);
                jobName = "job_" + TableUtils.tableName(dataSetTableId);
                fileName = TableUtils.tmpName(TableUtils.tableName(dataSetTableId));
                break;
            case "incremental_add":
                transName = "trans_add_" + TableUtils.tableName(dataSetTableId);
                jobName = "job_add_" + TableUtils.tableName(dataSetTableId);
                fileName = TableUtils.addName(TableUtils.tableName(dataSetTableId));
                break;
            case "incremental_delete":
                transName = "trans_delete_" + TableUtils.tableName(dataSetTableId);
                jobName = "job_delete_" + TableUtils.tableName(dataSetTableId);
                fileName = TableUtils.deleteName(TableUtils.tableName(dataSetTableId));
                break;
            default:
                break;
        }
        deleteFile(root_path + fileName + "." + extension);
        deleteFile(root_path + jobName + ".kjb");
        deleteFile(root_path + transName + ".ktr");
    }

    private void deleteExcelFile(DatasetTable datasetTable, List<String> datasetTableIds) {
        List<DatasetTable> datasetTables = dataSetTableService.list(datasetTableIds);
        for (ExcelSheetData excelSheetData : new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getExcelSheetDataList()) {
            Boolean allIsFinished = true;
            for (DatasetTable table : datasetTables) {
                for (ExcelSheetData data : new Gson().fromJson(table.getInfo(), DataTableInfoDTO.class).getExcelSheetDataList()) {
                    if (data.getPath().equalsIgnoreCase(excelSheetData.getPath())) {
                        if (StringUtils.isEmpty(table.getSyncStatus()) || table.getSyncStatus().equalsIgnoreCase(JobStatus.Underway.name())) {
                            allIsFinished = false;
                        }
                    }
                }
            }
            if (allIsFinished) {
                deleteFile(excelSheetData.getPath());
            } else {
                try {
                    Thread.sleep(5000);
                } catch (Exception ignore) {
                }
                deleteExcelFile(datasetTable, datasetTableIds);
            }
        }
    }

    private void deleteFile(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return;
        }
        try {
            File file = new File(filePath);
            FileUtils.forceDelete(file);
        } catch (Exception e) {
        }
    }

    private final static String handleBinaryType = "    \t\tif(\"FIELD\".equalsIgnoreCase(filed)){\n" +
            "           get(Fields.Out, filed).setValue(r, \"\");\n" +
            "           get(Fields.Out, filed).getValueMeta().setType(2);\n" +
            "     \t}";

    private final static String handleExcelIntColumn = " \t\tif(tmp != null && tmp.endsWith(\".0\")){\n" +
            "            try {\n" +
            "                Long.valueOf(tmp.substring(0, tmp.length()-2));\n" +
            "                get(Fields.Out, filed).setValue(r, tmp.substring(0, tmp.length()-2));\n" +
            "                get(Fields.Out, filed).getValueMeta().setType(2);\n" +
            "            }catch (Exception e){}\n" +
            "        }";

    private final static String handleMysqlBIGINTUNSIGNED = "if(filed.equalsIgnoreCase(\"BIGINTUNSIGNEDFIELD\")){\n" +
            "\t\t\tif(tmp != null && tmp.endsWith(\".0\")){\n" +
            "            \ttry {\n" +
            "                Long.valueOf(tmp.substring(0, tmp.length()-2));\n" +
            "                get(Fields.Out, filed).setValue(r, tmp.substring(0, tmp.length()-2));\n" +
            "                get(Fields.Out, filed).getValueMeta().setType(2);\n" +
            "            }catch (Exception e){}\n" +
            "       \t\t}\n" +
            "\t\t}";

    private final static String handleWraps = "        if(tmp != null && ( tmp.contains(\"\\r\") || tmp.contains(\"\\n\"))){\n" +
            "\t\t\ttmp = tmp.trim();\n" +
            "            tmp = tmp.replaceAll(\"\\r\",\" \");\n" +
            "            tmp = tmp.replaceAll(\"\\n\",\" \");\n" +
            "            get(Fields.Out, filed).setValue(r, tmp);\n" +
            "        } \n";

    private final static String handleDataease_uuid = "get(Fields.Out, \"dataease_uuid\").setValue(r, md5)";
    private final static String handleCharset = "\tif(tmp != null && Arrays.asList(\"varcharFields\".split(\",\")).contains(filed)){\n" +
            "  \t\t\ttry {\n" +
            "\t\t\t\tget(Fields.Out, filed).setValue(r, new String(tmp.getBytes(\"Datasource_Charset\"), \"Target_Charset\"));\n" +
            "       \t\t}catch (Exception e){}\n" +
            "\t\t}";


    private final static String code = "import org.pentaho.di.core.row.ValueMetaInterface;\n" +
            "import java.util.List;\n" +
            "import java.io.File;\n" +
            "import java.security.MessageDigest;\n" +
            "import java.util.ArrayList;\n" +
            "import java.util.Arrays;\n" +
            "import java.util.List;\n" +
            "import java.util.concurrent.ExecutorService;\n" +
            "import java.util.concurrent.Executors;\n" +
            "import org.pentaho.di.core.util.StringUtil;\n" +
            "\n" +
            "public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {\n" +
            "  if (first) {\n" +
            "    first = false;\n" +
            "  }\n" +
            "\n" +
            "  Object[] r = getRow();\n" +
            "\n" +
            "  if (r == null) {\n" +
            "    setOutputDone();\n" +
            "    return false;\n" +
            "  }\n" +
            "\n" +
            "  r = createOutputRow(r, data.outputRowMeta.size());\n" +
            "  String str = \"\";\n" +
            "\n" +
            "    List<String> fields = Arrays.asList(\"Column_Fields\".split(\",\"));\n" +
            "    for (String filed : fields) {\n" +
            "        String tmp = get(Fields.In, filed).getString(r);\n" +
            "handleMysqlBIGINTUNSIGNED \n" +
            "handleCharset \n" +
            "handleWraps \n" +
            "ExcelCompletion \n" +
            "handleBinaryType \n" +
            "handleExcelIntColumn \n" +
            "        str = str + tmp;\n" +
            "    }\n" +
            "\n" +
            "  String md5 = md5(str);\n" +
            "  handleDataease_uuid ;\n" +
            "\n" +
            "  putRow(data.outputRowMeta, r);\n" +
            "\n" +
            "  return true;\n" +
            "}\n" +
            "\n" +
            "    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};\n" +
            "    private static final String UTF_8 = \"UTF-8\";\n" +
            "    public static String md5(String src) {\n" +
            "        return md5(src, UTF_8);\n" +
            "    }\n" +
            "    public static String md5(String src, String charset) {\n" +
            "        try {\n" +
            "            byte[] strTemp = charset == null || charset.equals(\"\") ? src.getBytes() : src.getBytes(charset);\n" +
            "            MessageDigest mdTemp = MessageDigest.getInstance(\"MD5\");\n" +
            "            mdTemp.update(strTemp);\n" +
            "\n" +
            "            byte[] md = mdTemp.digest();\n" +
            "            int j = md.length;\n" +
            "            char[] str = new char[j * 2];\n" +
            "            int k = 0;\n" +
            "\n" +
            "            for (byte byte0 : md) {\n" +
            "                str[k++] = HEX_DIGITS[byte0 >>> 4 & 0xf];\n" +
            "                str[k++] = HEX_DIGITS[byte0 & 0xf];\n" +
            "            }\n" +
            "\n" +
            "            return new String(str);\n" +
            "        } catch (Exception e) {\n" +
            "            throw new RuntimeException(\"MD5 encrypt error:\", e);\n" +
            "        }\n" +
            "    }\n" +
            "\n" +
            "    public String generateStr(int size, String[] d ){\n" +
            "        String str = null;\n" +
            "        for(int i=0;i<size;i++){\n" +
            "            str = str + d[i];\n" +
            "        }\n" +
            "        return str;\n" +
            "    }";
}

