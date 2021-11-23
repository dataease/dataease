package io.dataease.service.dataset;

import com.google.gson.Gson;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.DatasetTableMapper;
import io.dataease.base.mapper.DatasetTableTaskMapper;
import io.dataease.base.mapper.DatasourceMapper;
import io.dataease.base.mapper.ext.ExtChartViewMapper;
import io.dataease.commons.constants.*;
import io.dataease.commons.model.AuthURD;
import io.dataease.commons.utils.*;
import io.dataease.commons.constants.DatasourceTypes;
import io.dataease.provider.datasource.DatasourceProvider;
import io.dataease.provider.datasource.JdbcProvider;
import io.dataease.provider.ProviderFactory;
import io.dataease.controller.request.datasource.DatasourceRequest;
import io.dataease.dto.datasource.*;
import io.dataease.service.datasource.DatasourceService;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.dto.dataset.ExcelSheetData;
import io.dataease.exception.DataEaseException;
import io.dataease.listener.util.CacheUtils;
import io.dataease.provider.query.QueryProvider;
import io.dataease.service.message.DeMsgutil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.HttpClientManager;
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
import org.pentaho.di.trans.steps.excelinput.ExcelInputField;
import org.pentaho.di.trans.steps.excelinput.ExcelInputMeta;
import org.pentaho.di.trans.steps.excelinput.SpreadSheetType;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
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
import java.io.File;
import java.net.InetAddress;
import java.util.*;
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
    private DatasourceMapper datasourceMapper;
    @Resource
    private DatasetTableMapper datasetTableMapper;
    @Resource
    private DatasetTableTaskMapper datasetTableTaskMapper;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private ExtChartViewMapper extChartViewMapper;

    private static final String lastUpdateTime = "${__last_update_time__}";
    private static final String currentUpdateTime = "${__current_update_time__}";
    private static final String separator = "|DE|";
    private static final String extention = "txt";
    private static final String root_path = "/opt/dataease/data/kettle/";

    @Value("${kettle.files.keep:false}")
    private boolean kettleFilesKeep;
    @Value("${carte.host:127.0.0.1}")
    private String carte;
    @Value("${carte.port:8080}")
    private String port;
    @Value("${carte.user:cluster}")
    private String user;
    @Value("${carte.passwd:cluster}")
    private String passwd;
    private static final String creatTableSql = "CREATE TABLE IF NOT EXISTS `TABLE_NAME`" +
            "Column_Fields" +
            "UNIQUE KEY(dataease_uuid)\n" +
            "DISTRIBUTED BY HASH(dataease_uuid) BUCKETS 10\n" +
            "PROPERTIES(\"replication_num\" = \"1\");";

    private static final String dropTableSql = "DROP TABLE IF EXISTS TABLE_NAME;";
    private static final String shellScript = "result=`curl --location-trusted -u %s:%s -H \"label:%s\" -H \"column_separator:%s\" -H \"columns:%s\" -H \"merge_type: %s\" -T %s -XPUT http://%s:%s/api/%s/%s/_stream_load`\n" +
            "if [ $? == 0 ] ; then\n" +
            "  failstatus=$(echo $result | grep '\"Status\": \"Fail\"')\n" +
            "  if [[ \"$failstatus\" != \"\" ]]; then\n" +
            "     echo $result\n" +
            "     exit 1\n" +
            "  fi\n" +
            "else\n" +
            "  echo $result\n" +
            "  exit 1\n" +
            "fi\n";

    public synchronized boolean existSyncTask(DatasetTable datasetTable, DatasetTableTask datasetTableTask, Long startTime) {
        datasetTable.setSyncStatus(JobStatus.Underway.name());
        DatasetTableExample example = new DatasetTableExample();
        example.createCriteria().andIdEqualTo(datasetTable.getId()).andSyncStatusNotEqualTo(JobStatus.Underway.name());
        example.or(example.createCriteria().andIdEqualTo(datasetTable.getId()).andSyncStatusIsNull());
        boolean existSyncTask = datasetTableMapper.updateByExampleSelective(datasetTable, example) == 0;
        if (existSyncTask) {
            DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
            datasetTableTaskLog.setTaskId(datasetTableTask.getId());
            datasetTableTaskLog.setTableId(datasetTable.getId());
            datasetTableTaskLog.setStatus(JobStatus.Underway.name());
            List<DatasetTableTaskLog> datasetTableTaskLogs = dataSetTableTaskLogService.select(datasetTableTaskLog);
            return !CollectionUtils.isNotEmpty(datasetTableTaskLogs) || !datasetTableTaskLogs.get(0).getTriggerType().equalsIgnoreCase(TriggerType.Custom.name());
        } else {
            datasetTableTask.setLastExecTime(startTime);
            datasetTableTask.setLastExecStatus(JobStatus.Underway.name());
            datasetTableTask.setStatus(TaskStatus.Exec.name());
            dataSetTableTaskService.update(datasetTableTask);
            return false;
        }
    }

    public void extractExcelData(String datasetTableId, String type, String ops, List<DatasetTableField> datasetTableFields) {
        Datasource datasource = new Datasource();
        datasource.setType("excel");
        DatasetTable datasetTable = getDatasetTable(datasetTableId);
        if (datasetTable == null) {
            LogUtil.error("Can not find DatasetTable: " + datasetTableId);
            return;
        }
        UpdateType updateType = UpdateType.valueOf(type);
        DatasetTableTaskLog datasetTableTaskLog;
        if(datasetTableFields == null){
            datasetTableFields = dataSetTableFieldsService.list(DatasetTableField.builder().tableId(datasetTable.getId()).build());
        }
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
        String dorisTableColumnSql = createDorisTableColumnSql(datasetTableFields);
        switch (updateType) {
            case all_scope:  // 全量更新
                try {
                    datasetTableTaskLog = writeDatasetTableTaskLog(datasetTableId, ops);
                    createDorisTable(DorisTableUtils.dorisName(datasetTableId), dorisTableColumnSql);
                    createDorisTable(DorisTableUtils.dorisTmpName(DorisTableUtils.dorisName(datasetTableId)), dorisTableColumnSql);
                    generateTransFile("all_scope", datasetTable, datasource, datasetTableFields, null);
                    generateJobFile("all_scope", datasetTable, datasetTableFields.stream().map(DatasetTableField::getDataeaseName).collect(Collectors.joining(",")));
                    Long execTime = System.currentTimeMillis();
                    extractData(datasetTable, "all_scope");
                    replaceTable(DorisTableUtils.dorisName(datasetTableId));
                    saveSuccessLog(datasetTableTaskLog);
                    updateTableStatus(datasetTableId, datasetTable, JobStatus.Completed, execTime);
                    if(ops.equalsIgnoreCase("替换")){
                        dataSetTableFieldsService.deleteByTableId(datasetTable.getId());
                        datasetTableFields.forEach(datasetTableField -> dataSetTableFieldsService.save(datasetTableField));
                    }
                } catch (Exception e) {
                    saveErrorLog(datasetTableId, null, e);
                    updateTableStatus(datasetTableId, datasetTable, JobStatus.Error, null);
                    dropDorisTable(DorisTableUtils.dorisTmpName(DorisTableUtils.dorisName(datasetTableId)));
                } finally {
                    deleteFile("all_scope", datasetTableId);
                    for (ExcelSheetData excelSheetData : new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getExcelSheetDataList()) {
                        deleteFile(excelSheetData.getPath());
                    }
                }
                break;

            case add_scope: // 增量更新
                try {
                    datasetTableTaskLog = writeDatasetTableTaskLog(datasetTableId, ops);
                    generateTransFile("incremental_add", datasetTable, datasource, datasetTableFields, null);
                    generateJobFile("incremental_add", datasetTable, datasetTableFields.stream().map(DatasetTableField::getDataeaseName).collect(Collectors.joining(",")));
                    Long execTime = System.currentTimeMillis();
                    extractData(datasetTable, "incremental_add");
                    saveSuccessLog(datasetTableTaskLog);
                    updateTableStatus(datasetTableId, datasetTable, JobStatus.Completed, execTime);
                } catch (Exception e) {
                    saveErrorLog(datasetTableId, null, e);
                    updateTableStatus(datasetTableId, datasetTable, JobStatus.Error, null);
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
        DatasetTableTask datasetTableTask = datasetTableTaskMapper.selectByPrimaryKey(taskId);
        if (datasetTableTask == null) {
            return;
        }
        if (datasetTableTask.getStatus().equalsIgnoreCase(TaskStatus.Stopped.name()) || datasetTableTask.getStatus().equalsIgnoreCase(TaskStatus.Pending.name())) {
            LogUtil.info("Skip synchronization task: {} ,due to task status is {}", datasetTableTask.getId(), datasetTableTask.getStatus());
            dataSetTableTaskService.checkTaskIsStopped(datasetTableTask);
            return;
        }

        Long startTime = System.currentTimeMillis();
        if (existSyncTask(datasetTable, datasetTableTask, startTime)) {
            LogUtil.info("Skip synchronization task for dataset due to exist others, dataset ID : " + datasetTableId);
            return;
        }
        DatasetTableTaskLog datasetTableTaskLog = getDatasetTableTaskLog(datasetTableId, taskId, startTime);
        UpdateType updateType = UpdateType.valueOf(type);
        if (context != null) {
            datasetTable.setQrtzInstance(context.getFireInstanceId());
            datasetTableMapper.updateByPrimaryKeySelective(datasetTable);
        }
        Datasource datasource = new Datasource();
        if (StringUtils.isNotEmpty(datasetTable.getDataSourceId())) {
            datasource = datasourceMapper.selectByPrimaryKey(datasetTable.getDataSourceId());
        } else {
            datasource.setType(datasetTable.getType());
        }
        List<DatasetTableField> datasetTableFields = dataSetTableFieldsService.list(DatasetTableField.builder().tableId(datasetTable.getId()).build());
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
        String dorisTableColumnSql = createDorisTableColumnSql(datasetTableFields);

        boolean msg = false;
        JobStatus lastExecStatus = JobStatus.Completed;
        Long execTime = null;
        switch (updateType) {
            case all_scope:  // 全量更新
                try {
                    createDorisTable(DorisTableUtils.dorisName(datasetTableId), dorisTableColumnSql);
                    createDorisTable(DorisTableUtils.dorisTmpName(DorisTableUtils.dorisName(datasetTableId)), dorisTableColumnSql);
                    generateTransFile("all_scope", datasetTable, datasource, datasetTableFields, null);
                    generateJobFile("all_scope", datasetTable, datasetTableFields.stream().map(DatasetTableField::getDataeaseName).collect(Collectors.joining(",")));
                    execTime = System.currentTimeMillis();
                    extractData(datasetTable, "all_scope");
                    replaceTable(DorisTableUtils.dorisName(datasetTableId));
                    saveSuccessLog(datasetTableTaskLog);
                    msg = true;
                    lastExecStatus = JobStatus.Completed;
                } catch (Exception e) {
                    e.printStackTrace();
                    saveErrorLog(datasetTableId, taskId, e);
                    msg = false;
                    lastExecStatus = JobStatus.Error;
                    execTime = null;
                } finally {
                    try { deleteFile("all_scope", datasetTableId); }catch (Exception ignore){ System.out.println(ignore.getMessage());}
                    try { sendWebMsg(datasetTable, datasetTableTask, datasetTableTaskLog, msg); }catch (Exception ignore){ System.out.println(ignore.getMessage());}
                    try { dataSetTableTaskService.updateTaskStatus(datasetTableTask, lastExecStatus); }catch (Exception ignore){
                        System.out.println(ignore.getMessage());
                    }
                    try { updateTableStatus(datasetTableId, datasetTable, lastExecStatus, execTime); }catch (Exception ignore){ System.out.println(ignore.getMessage());}
                    try { dropDorisTable(DorisTableUtils.dorisTmpName(DorisTableUtils.dorisName(datasetTableId))); }catch (Exception ignore){ System.out.println(ignore.getMessage());}
                }
                break;

            case add_scope: // 增量更新
                try {
                    DatasetTableIncrementalConfig datasetTableIncrementalConfig = dataSetTableService.incrementalConfig(datasetTableId);
                    if (datasetTableIncrementalConfig == null || StringUtils.isEmpty(datasetTableIncrementalConfig.getTableId())) {
                        updateTableStatus(datasetTableId, datasetTable, JobStatus.Completed, null);
                        return;
                    }
                    if (datasetTable.getLastUpdateTime() == null || datasetTable.getLastUpdateTime() == 0) {
                        updateTableStatus(datasetTableId, datasetTable, JobStatus.Completed, null);
                        saveErrorLog(datasetTableId, taskId, new Exception("未进行全量同步"));
                        lastExecStatus = JobStatus.Error;
                        return;
                    }

                    execTime = System.currentTimeMillis();
                    if (StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalAdd()) && StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalAdd().replace(" ", ""))) {// 增量添加
                        String sql = datasetTableIncrementalConfig.getIncrementalAdd().replace(lastUpdateTime, datasetTable.getLastUpdateTime().toString())
                                .replace(currentUpdateTime, Long.valueOf(System.currentTimeMillis()).toString());
                        generateTransFile("incremental_add", datasetTable, datasource, datasetTableFields, sql);
                        generateJobFile("incremental_add", datasetTable, fetchSqlField(sql, datasource));
                        extractData(datasetTable, "incremental_add");
                    }

                    if (StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalDelete()) && StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalDelete().replace(" ", ""))) {// 增量删除
                        String sql = datasetTableIncrementalConfig.getIncrementalDelete().replace(lastUpdateTime, datasetTable.getLastUpdateTime().toString())
                                .replace(currentUpdateTime, Long.valueOf(System.currentTimeMillis()).toString());
                        generateTransFile("incremental_delete", datasetTable, datasource, datasetTableFields, sql);
                        generateJobFile("incremental_delete", datasetTable, fetchSqlField(sql, datasource));
                        extractData(datasetTable, "incremental_delete");
                    }
                    saveSuccessLog(datasetTableTaskLog);

                    msg = true;
                    lastExecStatus = JobStatus.Completed;
                } catch (Exception e) {
                    saveErrorLog(datasetTableId, taskId, e);
                    msg = false;
                    lastExecStatus = JobStatus.Error;
                    execTime = null;
                } finally {
                    try { deleteFile("incremental_add", datasetTableId); deleteFile("incremental_delete", datasetTableId); }catch (Exception ignore){}
                    try { sendWebMsg(datasetTable, datasetTableTask, datasetTableTaskLog, msg); }catch (Exception ignore){}
                    try { dataSetTableTaskService.updateTaskStatus(datasetTableTask, lastExecStatus); }catch (Exception ignore){}
                    try { updateTableStatus(datasetTableId, datasetTable, lastExecStatus, execTime); }catch (Exception ignore){}
                }
                break;
        }
        //侵入式清除下属视图缓存
        List<String> viewIds = extChartViewMapper.allViewIds(datasetTableId);
        if (CollectionUtils.isNotEmpty(viewIds)) {
            viewIds.forEach(viewId -> CacheUtils.remove(JdbcConstants.VIEW_CACHE_KEY, viewId));
        }

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

    private void updateTableStatus(String datasetTableId, DatasetTable datasetTable, JobStatus completed, Long execTime) {
        datasetTable.setSyncStatus(completed.name());
        if (execTime != null) {
            datasetTable.setLastUpdateTime(execTime);
        }
        DatasetTableExample example = new DatasetTableExample();
        example.createCriteria().andIdEqualTo(datasetTableId);
        datasetTableMapper.updateByExampleSelective(datasetTable, example);
    }

    private void saveSuccessLog(DatasetTableTaskLog datasetTableTaskLog) {
        datasetTableTaskLog.setStatus(JobStatus.Completed.name());
        datasetTableTaskLog.setEndTime(System.currentTimeMillis());
        dataSetTableTaskLogService.save(datasetTableTaskLog);
    }

    private void saveErrorLog(String datasetTableId, String taskId, Exception e) {
        LogUtil.error("Extract data error: " + datasetTableId, e);
        DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
        datasetTableTaskLog.setTableId(datasetTableId);
        datasetTableTaskLog.setStatus(JobStatus.Underway.name());
        if (StringUtils.isNotEmpty(taskId)) {
            datasetTableTaskLog.setTaskId(taskId);
        }
        List<DatasetTableTaskLog> datasetTableTaskLogs = dataSetTableTaskLogService.select(datasetTableTaskLog);
        if (CollectionUtils.isNotEmpty(datasetTableTaskLogs)) {
            datasetTableTaskLog = datasetTableTaskLogs.get(0);
            datasetTableTaskLog.setStatus(JobStatus.Error.name());
            datasetTableTaskLog.setInfo(e.getMessage());
            datasetTableTaskLog.setEndTime(System.currentTimeMillis());
            dataSetTableTaskLogService.save(datasetTableTaskLog);
        }

    }

    private String createDorisTableColumnSql(final List<DatasetTableField> datasetTableFields) {
        StringBuilder Column_Fields = new StringBuilder("dataease_uuid  varchar(50), `");
        for (DatasetTableField datasetTableField : datasetTableFields) {
            Column_Fields.append(datasetTableField.getDataeaseName()).append("` ");
            Integer size = datasetTableField.getSize() * 3;
            if (datasetTableField.getSize() == 0 || datasetTableField.getSize() > 65533 || datasetTableField.getSize() * 3 > 65533) {
                size = 65533;
            }
            switch (datasetTableField.getDeExtractType()) {
                case 0:
                    Column_Fields.append("varchar(length)".replace("length", String.valueOf(size))).append(",`");
                    break;
                case 1:
                    size  = size < 50? 50 : size;
                    Column_Fields.append("varchar(length)".replace("length", String.valueOf(size))).append(",`");
                    break;
                case 2:
                    Column_Fields.append("bigint").append(",`");
                    break;
                case 3:
                    Column_Fields.append("DOUBLE").append(",`");
                    break;
                case 4:
                    Column_Fields.append("TINYINT(length)".replace("length", String.valueOf(size))).append(",`");
                    break;
                default:
                    Column_Fields.append("varchar(length)".replace("length", String.valueOf(size))).append(",`");
                    break;
            }
        }

        Column_Fields = new StringBuilder(Column_Fields.substring(0, Column_Fields.length() - 2));
        Column_Fields = new StringBuilder("(" + Column_Fields + ")\n");
        return Column_Fields.toString();
    }

    private void createDorisTable(String dorisTableName, String dorisTableColumnSql) throws Exception {
        Datasource dorisDatasource = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dorisDatasource);
        datasourceRequest.setQuery(creatTableSql.replace("TABLE_NAME", dorisTableName).replace("Column_Fields", dorisTableColumnSql));
        jdbcProvider.exec(datasourceRequest);
    }

    private void dropDorisTable(String dorisTableName) {
        try {
            Datasource dorisDatasource = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
            JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(dorisDatasource);
            datasourceRequest.setQuery(dropTableSql.replace("TABLE_NAME", dorisTableName));
            jdbcProvider.exec(datasourceRequest);
        } catch (Exception ignore) {
        }
    }

    private void replaceTable(String dorisTableName) throws Exception {
        Datasource dorisDatasource = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dorisDatasource);
        datasourceRequest.setQuery("ALTER TABLE DORIS_TABLE  REPLACE WITH TABLE DORIS_TMP_TABLE PROPERTIES('swap' = 'false');".replace("DORIS_TABLE", dorisTableName).replace("DORIS_TMP_TABLE", DorisTableUtils.dorisTmpName(dorisTableName)));
        jdbcProvider.exec(datasourceRequest);
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

    private DatasetTableTaskLog writeDatasetTableTaskLog(String datasetTableId, String taskId) {
        DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
        datasetTableTaskLog.setTableId(datasetTableId);
        datasetTableTaskLog.setTaskId(taskId);
        datasetTableTaskLog.setStatus(JobStatus.Underway.name());
        datasetTableTaskLog.setTriggerType(TriggerType.Cron.name());
        List<DatasetTableTaskLog> datasetTableTaskLogs = dataSetTableTaskLogService.select(datasetTableTaskLog);
        if (CollectionUtils.isEmpty(datasetTableTaskLogs)) {
            datasetTableTaskLog.setStartTime(System.currentTimeMillis());
            dataSetTableTaskLogService.save(datasetTableTaskLog);
            return datasetTableTaskLog;
        } else {
            return datasetTableTaskLogs.get(0);
        }
    }

    private DatasetTableTaskLog getDatasetTableTaskLog(String datasetTableId, String taskId, Long startTime) {
        DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
        datasetTableTaskLog.setTableId(datasetTableId);
        datasetTableTaskLog.setTaskId(taskId);
        datasetTableTaskLog.setStatus(JobStatus.Underway.name());
        datasetTableTaskLog.setTriggerType(TriggerType.Custom.name());
        for (int i = 0; i < 10; i++) {
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
        dataSetTableTaskLogService.save(datasetTableTaskLog);
        return datasetTableTaskLog;
    }

    private void extractData(DatasetTable datasetTable, String extractType) throws Exception {
        if(StringUtils.isNotEmpty(datasetTable.getDataSourceId())){
            datasourceService.validate(datasetTable.getDataSourceId());
        }
        KettleFileRepository repository = CommonBeanFactory.getBean(KettleFileRepository.class);
        RepositoryDirectoryInterface repositoryDirectoryInterface = repository.loadRepositoryDirectoryTree();
        TransMeta transMeta = null;
        JobMeta jobMeta = null;
        switch (extractType) {
            case "all_scope":
                jobMeta = repository.loadJob("job_" + DorisTableUtils.dorisName(datasetTable.getId()), repositoryDirectoryInterface, null, null);
                transMeta = repository.loadTransformation("trans_" + DorisTableUtils.dorisName(datasetTable.getId()), repositoryDirectoryInterface, null, true, "");
                break;
            case "incremental_add":
                jobMeta = repository.loadJob("job_add_" + DorisTableUtils.dorisName(datasetTable.getId()), repositoryDirectoryInterface, null, null);
                transMeta = repository.loadTransformation("trans_add_" + DorisTableUtils.dorisName(datasetTable.getId()), repositoryDirectoryInterface, null, true, "");
                break;
            case "incremental_delete":
                jobMeta = repository.loadJob("job_delete_" + DorisTableUtils.dorisName(datasetTable.getId()), repositoryDirectoryInterface, null, null);
                transMeta = repository.loadTransformation("trans_delete_" + DorisTableUtils.dorisName(datasetTable.getId()), repositoryDirectoryInterface, null, true, "");
                break;
            default:
                break;
        }

        SlaveServer remoteSlaveServer = getSlaveServer();
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
            return;
        } else {
            DataEaseException.throwException((jobStatus.getLoggingString()));
        }
    }

    private SlaveServer getSlaveServer() {
        SlaveServer remoteSlaveServer = new SlaveServer();
        remoteSlaveServer.setHostname(carte);// 设置远程IP
        remoteSlaveServer.setPort(port);// 端口
        remoteSlaveServer.setUsername(user);
        remoteSlaveServer.setPassword(passwd);
        return remoteSlaveServer;
    }

    private void generateJobFile(String extractType, DatasetTable datasetTable, String columnFields) throws Exception {
        String outFile;
        String jobName = null;
        String script = null;
        Datasource dorisDatasource = (Datasource) CommonBeanFactory.getBean("DorisDatasource");
        DorisConfiguration dorisConfiguration = new Gson().fromJson(dorisDatasource.getConfiguration(), DorisConfiguration.class);
        String columns = columnFields + ",dataease_uuid";
        switch (extractType) {
            case "all_scope":
                outFile = DorisTableUtils.dorisTmpName(DorisTableUtils.dorisName(datasetTable.getId()));
                jobName = "job_" + DorisTableUtils.dorisName(datasetTable.getId());
                script = String.format(shellScript, dorisConfiguration.getUsername(), dorisConfiguration.getPassword(), System.currentTimeMillis(), separator, columns, "APPEND", root_path + outFile + "." + extention, dorisConfiguration.getHost(), dorisConfiguration.getHttpPort(), dorisConfiguration.getDataBase(), DorisTableUtils.dorisTmpName(DorisTableUtils.dorisName(datasetTable.getId())), root_path + outFile + "." + extention);
                break;
            case "incremental_add":
                outFile = DorisTableUtils.dorisAddName(datasetTable.getId());
                jobName = "job_add_" + DorisTableUtils.dorisName(datasetTable.getId());
                script = String.format(shellScript, dorisConfiguration.getUsername(), dorisConfiguration.getPassword(), System.currentTimeMillis(), separator, columns, "APPEND", root_path + outFile + "." + extention, dorisConfiguration.getHost(), dorisConfiguration.getHttpPort(), dorisConfiguration.getDataBase(), DorisTableUtils.dorisName(datasetTable.getId()), root_path + outFile + "." + extention);
                break;
            case "incremental_delete":
                outFile = DorisTableUtils.dorisDeleteName(DorisTableUtils.dorisName(datasetTable.getId()));
                script = String.format(shellScript, dorisConfiguration.getUsername(), dorisConfiguration.getPassword(), System.currentTimeMillis(), separator, columns, "DELETE", root_path + outFile + "." + extention, dorisConfiguration.getHost(), dorisConfiguration.getHttpPort(), dorisConfiguration.getDataBase(), DorisTableUtils.dorisName(datasetTable.getId()), root_path + outFile + "." + extention);
                jobName = "job_delete_" + DorisTableUtils.dorisName(datasetTable.getId());
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
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceRequest.setQuery(qp.wrapSql(sql));
        List<String> dorisFields = new ArrayList<>();
        datasourceProvider.fetchResultField(datasourceRequest).stream().map(TableFiled::getFieldName).forEach(filed -> {
            dorisFields.add(DorisTableUtils.columnName(filed));
        });
        return String.join(",", dorisFields);
    }

    private void generateTransFile(String extractType, DatasetTable datasetTable, Datasource datasource, List<DatasetTableField> datasetTableFields, String selectSQL) throws Exception {
        TransMeta transMeta = new TransMeta();
        String outFile = null;
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasource.getType());
        DatabaseMeta dataMeta;
        StepMeta inputStep = null;
        StepMeta outputStep;
        StepMeta udjcStep = null;
        TransHopMeta hi1;
        TransHopMeta hi2;
        String transName = null;

        switch (datasourceType) {
            case ds_doris:
            case mariadb:
            case mysql:
                MysqlConfiguration mysqlConfiguration = new Gson().fromJson(datasource.getConfiguration(), MysqlConfiguration.class);
                dataMeta = new DatabaseMeta("db", "MYSQL", "Native", mysqlConfiguration.getHost().trim(), mysqlConfiguration.getDataBase().trim(), mysqlConfiguration.getPort().toString(), mysqlConfiguration.getUsername(), mysqlConfiguration.getPassword());
                dataMeta.addExtraOption("MYSQL", "characterEncoding", "UTF-8");
                transMeta.addDatabase(dataMeta);
                selectSQL = getSelectSQL(extractType, datasetTable, datasource, datasetTableFields, selectSQL);
                inputStep = inputStep(transMeta, selectSQL);
                udjcStep = udjc(datasetTableFields, DatasourceTypes.mysql);
                break;
            case sqlServer:
                SqlServerConfiguration sqlServerConfiguration = new Gson().fromJson(datasource.getConfiguration(), SqlServerConfiguration.class);
                dataMeta = new DatabaseMeta("db", "MSSQLNATIVE", "Native", sqlServerConfiguration.getHost().trim(), sqlServerConfiguration.getDataBase(), sqlServerConfiguration.getPort().toString(), sqlServerConfiguration.getUsername(), sqlServerConfiguration.getPassword());
                transMeta.addDatabase(dataMeta);
                selectSQL = getSelectSQL(extractType, datasetTable, datasource, datasetTableFields, selectSQL);
                inputStep = inputStep(transMeta, selectSQL);
                udjcStep = udjc(datasetTableFields, DatasourceTypes.sqlServer);
                break;
            case pg:
                PgConfiguration pgConfiguration = new Gson().fromJson(datasource.getConfiguration(), PgConfiguration.class);
                dataMeta = new DatabaseMeta("db", "POSTGRESQL", "Native", pgConfiguration.getHost().trim(), pgConfiguration.getDataBase(), pgConfiguration.getPort().toString(), pgConfiguration.getUsername(), pgConfiguration.getPassword());
                transMeta.addDatabase(dataMeta);
                selectSQL = getSelectSQL(extractType, datasetTable, datasource, datasetTableFields, selectSQL);
                inputStep = inputStep(transMeta, selectSQL);
                udjcStep = udjc(datasetTableFields, DatasourceTypes.pg);
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

                selectSQL = getSelectSQL(extractType, datasetTable, datasource, datasetTableFields, selectSQL);
                inputStep = inputStep(transMeta, selectSQL);
                udjcStep = udjc(datasetTableFields, DatasourceTypes.oracle);
                break;
            case ck:
                CHConfiguration chConfiguration = new Gson().fromJson(datasource.getConfiguration(), CHConfiguration.class);
                dataMeta = new DatabaseMeta("db", "ORACLE", "Native", chConfiguration.getHost().trim(), chConfiguration.getDataBase().trim(), chConfiguration.getPort().toString(), chConfiguration.getUsername(), chConfiguration.getPassword());
                dataMeta.setDatabaseType("Clickhouse");
                transMeta.addDatabase(dataMeta);
                selectSQL = getSelectSQL(extractType, datasetTable, datasource, datasetTableFields, selectSQL);
                inputStep = inputStep(transMeta, selectSQL);
                udjcStep = udjc(datasetTableFields, DatasourceTypes.ck);
                break;
            case excel:
                inputStep = excelInputStep(datasetTable.getInfo(), datasetTableFields);
                udjcStep = udjc(datasetTableFields, DatasourceTypes.excel);
            default:
                break;
        }

        switch (extractType) {
            case "all_scope":
                transName = "trans_" + DorisTableUtils.dorisName(datasetTable.getId());
                outFile = DorisTableUtils.dorisTmpName(DorisTableUtils.dorisName(datasetTable.getId()));
                transMeta.setName(transName);
                break;
            case "incremental_add":
                transName = "trans_add_" + DorisTableUtils.dorisName(datasetTable.getId());
                outFile = DorisTableUtils.dorisAddName(datasetTable.getId());
                transMeta.setName(transName);
                break;
            case "incremental_delete":
                transName = "trans_delete_" + DorisTableUtils.dorisName(datasetTable.getId());
                outFile = DorisTableUtils.dorisDeleteName(DorisTableUtils.dorisName(datasetTable.getId()));
                transMeta.setName(transName);
                break;
            default:
                break;
        }

        outputStep = outputStep(outFile, datasetTableFields, datasource);

        hi1 = new TransHopMeta(inputStep, udjcStep);
        hi2 = new TransHopMeta(udjcStep, outputStep);
        transMeta.addTransHop(hi1);
        transMeta.addTransHop(hi2);
        transMeta.addStep(inputStep);
        transMeta.addStep(udjcStep);
        transMeta.addStep(outputStep);

        String transXml = transMeta.getXML();
        File file = new File(root_path + transName + ".ktr");
        FileUtils.writeStringToFile(file, transXml, "UTF-8");
    }

    private String getSelectSQL(String extractType, DatasetTable datasetTable, Datasource datasource, List<DatasetTableField> datasetTableFields, String selectSQL) {
        if (extractType.equalsIgnoreCase("all_scope") && datasetTable.getType().equalsIgnoreCase("db")) {
            String tableName = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getTable();
            QueryProvider qp = ProviderFactory.getQueryProvider(datasource.getType());
            selectSQL = qp.createRawQuerySQL(tableName, datasetTableFields, datasource);
        }

        if (extractType.equalsIgnoreCase("all_scope") && datasetTable.getType().equalsIgnoreCase("sql")) {
            selectSQL = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getSql();
            QueryProvider qp = ProviderFactory.getQueryProvider(datasource.getType());
            selectSQL = qp.createRawQuerySQLAsTmp(selectSQL, datasetTableFields);
        }
        if (!extractType.equalsIgnoreCase("all_scope")) {
            QueryProvider qp = ProviderFactory.getQueryProvider(datasource.getType());
            selectSQL = qp.createRawQuerySQLAsTmp(selectSQL, datasetTableFields);
        }
        return selectSQL;
    }

    private StepMeta inputStep(TransMeta transMeta, String selectSQL) {
        TableInputMeta tableInput = new TableInputMeta();
        DatabaseMeta database = transMeta.findDatabase("db");
        tableInput.setDatabaseMeta(database);
        tableInput.setSQL(selectSQL);
        StepMeta fromStep = new StepMeta("TableInput", "Data Input", tableInput);
        fromStep.setDraw(true);
        fromStep.setLocation(100, 100);
        return fromStep;
    }

    private StepMeta excelInputStep(String Info, List<DatasetTableField> datasetTableFields){
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(Info, DataTableInfoDTO.class);
        List<ExcelSheetData> excelSheetDataList = dataTableInfoDTO.getExcelSheetDataList();
        String suffix = excelSheetDataList.get(0).getPath().substring(excelSheetDataList.get(0).getPath().lastIndexOf(".") + 1);
        ExcelInputMeta excelInputMeta = new ExcelInputMeta();

        List<String> sheetNames = new ArrayList<>();
        List<String> files = new ArrayList<>();
        List<String> filesRequired = new ArrayList<>();
        for (ExcelSheetData excelSheetData : excelSheetDataList) {
            if(!sheetNames.contains(excelSheetData.getExcelLable())){
                sheetNames.add(excelSheetData.getExcelLable());
            }
            if(!files.contains(excelSheetData.getPath())){
                files.add(excelSheetData.getPath());
                filesRequired.add("Y");
            }
        }
        if (StringUtils.equalsIgnoreCase(suffix, "xlsx")) {
            excelInputMeta.setSpreadSheetType(SpreadSheetType.SAX_POI);
            excelInputMeta.setSheetName(sheetNames.toArray(new String[sheetNames.size()]));
        }
        if (StringUtils.equalsIgnoreCase(suffix, "xls")) {
            excelInputMeta.setSpreadSheetType(SpreadSheetType.JXL);
            excelInputMeta.setSheetName(sheetNames.toArray(new String[sheetNames.size()]));
        }
        excelInputMeta.setPassword("Encrypted");
        excelInputMeta.setFileName( files.toArray(new String[files.size()]));
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
        StepMeta fromStep = new StepMeta("ExcelInput", "Data Input", excelInputMeta);
        fromStep.setDraw(true);
        fromStep.setLocation(100, 100);
        return fromStep;
    }

    private StepMeta outputStep(String dorisOutputTable, List<DatasetTableField> datasetTableFields, Datasource datasource) {
        TextFileOutputMeta textFileOutputMeta = new TextFileOutputMeta();
        textFileOutputMeta.setEncoding("UTF-8");
        textFileOutputMeta.setHeaderEnabled(false);
        textFileOutputMeta.setFilename(root_path + dorisOutputTable);
        textFileOutputMeta.setSeparator(separator);
        textFileOutputMeta.setExtension(extention);

        if (datasource.getType().equalsIgnoreCase(DatasourceTypes.oracle.name()) ) {
            TextFileField[] outputFields = new TextFileField[datasetTableFields.size() + 1];
            for(int i=0;i< datasetTableFields.size();i++){
                TextFileField textFileField = new TextFileField();
                textFileField.setName(datasetTableFields.get(i).getOriginName());
                textFileField.setType("String");
                outputFields[i] = textFileField;
            }
            TextFileField textFileField = new TextFileField();
            textFileField.setName("dataease_uuid");
            textFileField.setType("String");
            outputFields[datasetTableFields.size()] = textFileField;

            textFileOutputMeta.setOutputFields(outputFields);
        }else if (datasource.getType().equalsIgnoreCase(DatasourceTypes.sqlServer.name()) || datasource.getType().equalsIgnoreCase(DatasourceTypes.pg.name()) || datasource.getType().equalsIgnoreCase(DatasourceTypes.mysql.name())){
            TextFileField[] outputFields = new TextFileField[datasetTableFields.size() + 1];
            for(int i=0;i< datasetTableFields.size();i++){
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
            TextFileField textFileField = new TextFileField();
            textFileField.setName("dataease_uuid");
            textFileField.setType("String");
            outputFields[datasetTableFields.size()] = textFileField;

            textFileOutputMeta.setOutputFields(outputFields);
        }else if(datasource.getType().equalsIgnoreCase(DatasourceTypes.excel.name())) {
            TextFileField[] outputFields = new TextFileField[datasetTableFields.size() + 1];
            for(int i=0;i< datasetTableFields.size();i++){
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
            TextFileField textFileField = new TextFileField();
            textFileField.setName("dataease_uuid");
            textFileField.setType("String");
            outputFields[datasetTableFields.size()] = textFileField;

            textFileOutputMeta.setOutputFields(outputFields);
        }else {
            textFileOutputMeta.setOutputFields(new TextFileField[0]);
        }

        StepMeta outputStep = new StepMeta("TextFileOutput", "TextFileOutput", textFileOutputMeta);
        outputStep.setLocation(600, 100);
        outputStep.setDraw(true);
        return outputStep;
    }

    private StepMeta udjc(List<DatasetTableField> datasetTableFields, DatasourceTypes datasourceType) {
        StringBuilder handleBinaryTypeCode = new StringBuilder();
        String excelCompletion = "";

        for (DatasetTableField datasetTableField : datasetTableFields) {
            if(datasetTableField.getDeExtractType().equals(DeTypeConstants.DE_BINARY)){
                handleBinaryTypeCode.append("\n").append(handleBinaryType.replace("FIELD", datasetTableField.getDataeaseName()));
            }
        }

        UserDefinedJavaClassMeta userDefinedJavaClassMeta = new UserDefinedJavaClassMeta();
        List<UserDefinedJavaClassMeta.FieldInfo> fields = new ArrayList<>();
        UserDefinedJavaClassMeta.FieldInfo fieldInfo = new UserDefinedJavaClassMeta.FieldInfo("dataease_uuid", ValueMetaInterface.TYPE_STRING, -1, -1);
        fields.add(fieldInfo);
        userDefinedJavaClassMeta.setFieldInfo(fields);
        List<UserDefinedJavaClassDef> definitions = new ArrayList<>();
        String tmp_code = code.replace("handleWraps", handleWraps).replace("handleBinaryType", handleBinaryTypeCode.toString());

        String Column_Fields;
        if (datasourceType.equals(DatasourceTypes.oracle)) {
            Column_Fields = datasetTableFields.stream().map(DatasetTableField::getOriginName).collect(Collectors.joining(","));
        } else {
            Column_Fields = datasetTableFields.stream().map(DatasetTableField::getDataeaseName).collect(Collectors.joining(","));
        }

        if (datasourceType.equals(DatasourceTypes.excel)) {
            tmp_code = tmp_code.replace("handleExcelIntColumn", handleExcelIntColumn).replace("Column_Fields", Column_Fields)
                    .replace("ExcelCompletion", excelCompletion);
        } else {
            tmp_code = tmp_code.replace("handleExcelIntColumn", "").replace("Column_Fields", Column_Fields)
                    .replace("ExcelCompletion", "");
        }

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
        if(kettleFilesKeep){
            return;
        }
        String transName = null;
        String jobName = null;
        String fileName = null;

        switch (type) {
            case "all_scope":
                transName = "trans_" + DorisTableUtils.dorisName(dataSetTableId);
                jobName = "job_" + DorisTableUtils.dorisName(dataSetTableId);
                fileName = DorisTableUtils.dorisTmpName(DorisTableUtils.dorisName(dataSetTableId));
                break;
            case "incremental_add":
                transName = "trans_add_" + DorisTableUtils.dorisName(dataSetTableId);
                jobName = "job_add_" + DorisTableUtils.dorisName(dataSetTableId);
                fileName = DorisTableUtils.dorisAddName(DorisTableUtils.dorisName(dataSetTableId));
                break;
            case "incremental_delete":
                transName = "trans_delete_" + DorisTableUtils.dorisName(dataSetTableId);
                jobName = "job_delete_" + DorisTableUtils.dorisName(dataSetTableId);
                fileName = DorisTableUtils.dorisDeleteName(DorisTableUtils.dorisName(dataSetTableId));
                break;
            default:
                break;
        }
        deleteFile(root_path + fileName + "." + extention);
        deleteFile(root_path + jobName + ".kjb");
        deleteFile(root_path + transName + ".ktr");
    }

    private void deleteFile(String filePath){
        if(StringUtils.isEmpty(filePath)){
            return;
        }
        try {
            File file = new File(filePath);
            FileUtils.forceDelete(file);
        } catch (Exception e) {
        }
    }
    public boolean isKettleRunning() {
        try {
            if (!InetAddress.getByName(carte).isReachable(1000)) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        HttpGet getMethod = new HttpGet("http://" + carte + ":" + port);
        HttpClientManager.HttpClientBuilderFacade clientBuilder = HttpClientManager.getInstance().createBuilder();
        clientBuilder.setConnectionTimeout(1);
        clientBuilder.setCredentials(user, passwd);
        try {
            CloseableHttpClient httpClient = clientBuilder.build();
            HttpResponse httpResponse = httpClient.execute(getMethod);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != -1 && statusCode < 400) {
                httpResponse.getEntity().getContent().close();
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
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

    private final static String handleWraps = "        if(tmp != null && ( tmp.contains(\"\\r\") || tmp.contains(\"\\n\"))){\n" +
            "\t\t\ttmp = tmp.trim();\n" +
            "            tmp = tmp.replaceAll(\"\\r\",\" \");\n" +
            "            tmp = tmp.replaceAll(\"\\n\",\" \");\n" +
            "            get(Fields.Out, filed).setValue(r, tmp);\n" +
            "        } \n";

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
            "    List<String> fileds = Arrays.asList(\"Column_Fields\".split(\",\"));\n" +
            "    for (String filed : fileds) {\n" +
            "        String tmp = get(Fields.In, filed).getString(r);\n" +
            "handleWraps \n" +
            "ExcelCompletion \n" +
            "handleBinaryType \n" +
            "handleExcelIntColumn \n" +
            "        str = str + tmp;\n" +
            "    }\n" +
            "\n" +
            "  String md5 = md5(str);\n" +
            "  get(Fields.Out, \"dataease_uuid\").setValue(r, md5);\n" +
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

