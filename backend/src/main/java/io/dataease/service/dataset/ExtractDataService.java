package io.dataease.service.dataset;

import com.google.gson.Gson;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.DatasourceMapper;
import io.dataease.commons.constants.JobStatus;
import io.dataease.commons.constants.ScheduleType;
import io.dataease.commons.constants.UpdateType;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.DorisTableUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.datasource.constants.DatasourceTypes;
import io.dataease.datasource.dto.DorisConfigration;
import io.dataease.datasource.dto.MysqlConfigration;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.datasource.provider.DatasourceProvider;
import io.dataease.datasource.provider.JdbcProvider;
import io.dataease.datasource.provider.ProviderFactory;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.dto.dataset.DataSetTaskLogDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.shell.JobEntryShell;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entries.success.JobEntrySuccess;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.excelinput.ExcelInputField;
import org.pentaho.di.trans.steps.excelinput.ExcelInputMeta;
import org.pentaho.di.trans.steps.excelinput.SpreadSheetType;
import org.pentaho.di.trans.steps.sql.ExecSQLMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassDef;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassMeta;
import org.pentaho.di.www.SlaveServerJobStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class ExtractDataService {

    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;
    @Resource
    private DataSetTableTaskLogService dataSetTableTaskLogService;
    @Resource
    private DataSetTableTaskService dataSetTableTaskService;
    @Resource
    private DatasourceMapper datasourceMapper;
    private static ExecutorService pool = Executors.newScheduledThreadPool(50);    //设置连接池

    private static String lastUpdateTime = "${__last_update_time__}";
    private static String currentUpdateTime = "${__current_update_time__}";
    private static String separator = "|";
    private static String extention = "txt";
    private static String root_path = "/opt/dataease/data/kettle/";

    @Value("${carte.host:127.0.0.1}")
    private String carte;
    @Value("${carte.port:8080}")
    private String port;
    @Value("${carte.user:cluster}")
    private String user;
    @Value("${carte.passwd:cluster}")
    private String passwd;

    private static String creatTableSql = "CREATE TABLE IF NOT EXISTS `TABLE_NAME`" +
            "Column_Fields" +
            "UNIQUE KEY(dataease_uuid)\n" +
            "DISTRIBUTED BY HASH(dataease_uuid) BUCKETS 10\n" +
            "PROPERTIES(\"replication_num\" = \"1\");";

    private static String shellScript = "curl --location-trusted -u %s:%s -H \"label:%s\" -H \"column_separator:%s\" -H \"columns:%s\" -H \"merge_type: %s\" -T %s -XPUT http://%s:%s/api/%s/%s/_stream_load\n" +
            "rm -rf %s\n";

    private String createDorisTablColumnSql( List<DatasetTableField> datasetTableFields){
        String Column_Fields = "dataease_uuid  varchar(50), `";
        for (DatasetTableField datasetTableField : datasetTableFields) {
            Column_Fields = Column_Fields + datasetTableField.getDataeaseName() + "` ";
            switch (datasetTableField.getDeType()){
                case 0:
                    Column_Fields = Column_Fields + "varchar(lenth)".replace("lenth", String.valueOf(datasetTableField.getSize())) + ",`";
                    break;
                case 1:
                    Column_Fields = Column_Fields + "varchar(lenth)".replace("lenth", String.valueOf(datasetTableField.getSize())) + ",`";
                    break;
                case 2:
                    Column_Fields = Column_Fields + "bigint(lenth)".replace("lenth", String.valueOf(datasetTableField.getSize())) + ",`";
                    break;
                case 3:
                    Column_Fields = Column_Fields + "DOUBLE" + ",`";
                    break;
                default:
                    Column_Fields = Column_Fields + "varchar(lenth)".replace("lenth", String.valueOf(datasetTableField.getSize())) + ",";
                    break;
            }
        }
        Column_Fields = Column_Fields.substring(0, Column_Fields.length() -2);
        Column_Fields =  "(" + Column_Fields + ")\n";
        return Column_Fields;
    }

    private void createDorisTable(String dorisTableName, String dorisTablColumnSql) throws Exception{
        Datasource dorisDatasource =  (Datasource)CommonBeanFactory.getBean("DorisDatasource");
        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);;
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dorisDatasource);
        datasourceRequest.setQuery(creatTableSql.replace("TABLE_NAME", dorisTableName).replace("Column_Fields", dorisTablColumnSql));
        jdbcProvider.exec(datasourceRequest);
    }

    private void replaceTable (String dorisTableName) throws Exception{
        Datasource dorisDatasource =  (Datasource)CommonBeanFactory.getBean("DorisDatasource");
        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);;
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dorisDatasource);
        datasourceRequest.setQuery("ALTER TABLE DORIS_TABLE  REPLACE WITH TABLE DORIS_TMP_TABLE PROPERTIES('swap' = 'false');".replace("DORIS_TABLE", dorisTableName).replace("DORIS_TMP_TABLE", DorisTableUtils.dorisTmpName(dorisTableName)));
        jdbcProvider.exec(datasourceRequest);
    }

    public void extractData(String datasetTableId, String taskId, String type) {
        DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
        UpdateType updateType = UpdateType.valueOf(type);
        DatasetTable datasetTable = null;
        Datasource datasource = new Datasource();
        try {
            datasetTable = dataSetTableService.get(datasetTableId);
            if(StringUtils.isNotEmpty(datasetTable.getDataSourceId())){
                datasource = datasourceMapper.selectByPrimaryKey(datasetTable.getDataSourceId());
            }else {
                datasource.setType(datasetTable.getType());
            }

            List<DatasetTableField> datasetTableFields = dataSetTableFieldsService.list(DatasetTableField.builder().tableId(datasetTable.getId()).build());
            datasetTableFields.sort((o1, o2) -> {
                if (o1.getColumnIndex() == null) {
                    return -1;
                }
                if (o2.getColumnIndex() == null) {
                    return 1;
                }
                return o1.getColumnIndex().compareTo(o2.getColumnIndex());
            });
            for (DatasetTableField datasetTableField : datasetTableFields) {
                System.out.println(new Gson().toJson(datasetTableField));
            }

            String dorisTablColumnSql = createDorisTablColumnSql(datasetTableFields);
            switch (updateType) {
                // 全量更新
                case all_scope:
                    writeDatasetTableTaskLog(datasetTableTaskLog, datasetTableId, taskId);
                    // TODO  before: check doris table column type
                    createDorisTable(DorisTableUtils.dorisName(datasetTableId), dorisTablColumnSql);
                    createDorisTable(DorisTableUtils.dorisTmpName(DorisTableUtils.dorisName(datasetTableId)), dorisTablColumnSql);
                    generateTransFile("all_scope", datasetTable, datasource, datasetTableFields, null);
                    generateJobFile("all_scope", datasetTable, String.join(",", datasetTableFields.stream().map(DatasetTableField::getDataeaseName).collect(Collectors.toList())));
                    extractData(datasetTable, "all_scope");
                    replaceTable(DorisTableUtils.dorisName(datasetTableId));
                    datasetTableTaskLog.setStatus(JobStatus.Completed.name());
                    datasetTableTaskLog.setEndTime(System.currentTimeMillis());
                    dataSetTableTaskLogService.save(datasetTableTaskLog);
                    break;

                // 增量更新
                case add_scope:
                    DatasetTableIncrementalConfig datasetTableIncrementalConfig = dataSetTableService.incrementalConfig(datasetTableId);
                    if (datasetTableIncrementalConfig == null || StringUtils.isEmpty(datasetTableIncrementalConfig.getTableId())) {
                        return;
                    }
                    DatasetTableTaskLog request = new DatasetTableTaskLog();
                    request.setTableId(datasetTableId);
                    request.setStatus(JobStatus.Completed.name());
                    List<DataSetTaskLogDTO> dataSetTaskLogDTOS = dataSetTableTaskLogService.list(request);
                    if (CollectionUtils.isEmpty(dataSetTaskLogDTOS)) {
                        return;
                    }
                    writeDatasetTableTaskLog(datasetTableTaskLog, datasetTableId, taskId);

                    // 增量添加
                    if (StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalAdd().replace(" ", ""))) {
                        String sql = datasetTableIncrementalConfig.getIncrementalAdd().replace(lastUpdateTime, dataSetTaskLogDTOS.get(0).getStartTime().toString()
                                .replace(currentUpdateTime, Long.valueOf(System.currentTimeMillis()).toString()));
                        generateTransFile("incremental_add", datasetTable, datasource, datasetTableFields, sql);
                        generateJobFile("incremental_add", datasetTable, fetchSqlField(sql, datasource));
                        extractData(datasetTable, "incremental_add");
                    }

                    // 增量删除
                    if (StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalDelete())) {
                        String sql = datasetTableIncrementalConfig.getIncrementalDelete().replace(lastUpdateTime, dataSetTaskLogDTOS.get(0).getStartTime().toString()
                                .replace(currentUpdateTime, Long.valueOf(System.currentTimeMillis()).toString()));
                        generateTransFile("incremental_delete", datasetTable, datasource, datasetTableFields, sql);
                        generateJobFile("incremental_delete", datasetTable, fetchSqlField(sql, datasource));
                        extractData(datasetTable, "incremental_delete");
                    }
                    datasetTableTaskLog.setStatus(JobStatus.Completed.name());
                    datasetTableTaskLog.setEndTime(System.currentTimeMillis());
                    dataSetTableTaskLogService.save(datasetTableTaskLog);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.error("ExtractData error, dataaset: " + datasetTableId);
            LogUtil.error(e.getMessage(), e);
            datasetTableTaskLog.setStatus(JobStatus.Error.name());
            datasetTableTaskLog.setEndTime(System.currentTimeMillis());
            dataSetTableTaskLogService.save(datasetTableTaskLog);
        } finally {
            DatasetTableTask datasetTableTask = dataSetTableTaskService.get(taskId);
            if (datasetTableTask != null && datasetTableTask.getRate().equalsIgnoreCase(ScheduleType.SIMPLE.toString())) {
                datasetTableTask.setRate(ScheduleType.SIMPLE_COMPLETE.toString());
                dataSetTableTaskService.update(datasetTableTask);
            }
        }
    }

    private void writeDatasetTableTaskLog(DatasetTableTaskLog datasetTableTaskLog, String datasetTableId, String taskId) {
        datasetTableTaskLog.setTableId(datasetTableId);
        datasetTableTaskLog.setTaskId(taskId);
        datasetTableTaskLog.setStatus(JobStatus.Underway.name());
        datasetTableTaskLog.setStartTime(System.currentTimeMillis());
        dataSetTableTaskLogService.save(datasetTableTaskLog);
    }

    private void extractData(DatasetTable datasetTable, String extractType) throws Exception {
        KettleFileRepository repository = CommonBeanFactory.getBean(KettleFileRepository.class);
        RepositoryDirectoryInterface repositoryDirectoryInterface = repository.loadRepositoryDirectoryTree();
        JobMeta jobMeta = null;
        switch (extractType) {
            case "all_scope":
                jobMeta = repository.loadJob("job_" + datasetTable.getId(), repositoryDirectoryInterface, null, null);
                break;
            case "incremental_add":
                jobMeta = repository.loadJob("job_add_" + datasetTable.getId(), repositoryDirectoryInterface, null, null);
                break;
            case "incremental_delete":
                jobMeta = repository.loadJob("job_delete_" + datasetTable.getId(), repositoryDirectoryInterface, null, null);
                break;
            default:
                break;
        }

        SlaveServer remoteSlaveServer = getSlaveServer();
        JobExecutionConfiguration jobExecutionConfiguration = new JobExecutionConfiguration();
        jobExecutionConfiguration.setRemoteServer(remoteSlaveServer);
        jobExecutionConfiguration.setRepository(repository);
        String lastCarteObjectId = Job.sendToSlaveServer(jobMeta, jobExecutionConfiguration, repository, null);
        SlaveServerJobStatus jobStatus = null;
        boolean executing = true;
        while(executing) {
            jobStatus = remoteSlaveServer.getJobStatus(jobMeta.getName(), lastCarteObjectId, 0);
            executing = jobStatus.isRunning() || jobStatus.isWaiting();
            if(!executing)
                break;
            Thread.sleep(1000);
        }
        if (jobStatus.getStatusDescription().equals("Finished")) {
            return;
        } else {
            throw new Exception(jobStatus.getLoggingString());
        }
    }

    private boolean isExitFile(String fileName) {
        File file = new File(root_path + fileName);
        return file.exists();
    }

    private SlaveServer getSlaveServer() {
        SlaveServer remoteSlaveServer = new SlaveServer();
        remoteSlaveServer.setHostname(carte);// 设置远程IP
        remoteSlaveServer.setPort(port);// 端口
        remoteSlaveServer.setUsername(user);
        remoteSlaveServer.setPassword(passwd);
        return remoteSlaveServer;
    }

    private void generateJobFile(String extractType, DatasetTable datasetTable, String columnFeilds) throws Exception {
        String dorisOutputTable = null;
        String jobName = null;
        String script = null;
        Datasource dorisDatasource =  (Datasource)CommonBeanFactory.getBean("DorisDatasource");
        DorisConfigration dorisConfigration = new Gson().fromJson(dorisDatasource.getConfiguration(), DorisConfigration.class);
        String columns = columnFeilds + ",dataease_uuid";
        String transName = null;
        switch (extractType) {
            case "all_scope":
                transName = "trans_" + datasetTable.getId();
                dorisOutputTable = DorisTableUtils.dorisTmpName(DorisTableUtils.dorisName(datasetTable.getId()));
                jobName = "job_" + datasetTable.getId();
                script = String.format(shellScript, dorisConfigration.getUsername(), dorisConfigration.getPassword(), String.valueOf(System.currentTimeMillis()), separator, columns, "APPEND",root_path + dorisOutputTable + "." + extention, dorisConfigration.getHost(),dorisConfigration.getHttpPort(), dorisConfigration.getDataBase(), dorisOutputTable, root_path + dorisOutputTable + "." + extention);
                break;
            case "incremental_add":
                transName = "trans_add_" + datasetTable.getId();
                dorisOutputTable = DorisTableUtils.dorisName(datasetTable.getId());
                jobName = "job_add_" + datasetTable.getId();
                script = String.format(shellScript, dorisConfigration.getUsername(), dorisConfigration.getPassword(), String.valueOf(System.currentTimeMillis()), separator, columns, "APPEND", root_path + dorisOutputTable + "." + extention, dorisConfigration.getHost(),dorisConfigration.getHttpPort(), dorisConfigration.getDataBase(), dorisOutputTable, root_path + dorisOutputTable + "." + extention);
                break;
            case "incremental_delete":
                transName = "trans_delete_" + datasetTable.getId();
                dorisOutputTable = DorisTableUtils.dorisDeleteName(DorisTableUtils.dorisName(datasetTable.getId()));
                script = String.format(shellScript, dorisConfigration.getUsername(), dorisConfigration.getPassword(), String.valueOf(System.currentTimeMillis()), separator, columns, "DELETE", root_path + dorisOutputTable + "." + extention, dorisConfigration.getHost(),dorisConfigration.getHttpPort(), dorisConfigration.getDataBase(), DorisTableUtils.dorisName(datasetTable.getId()), root_path + dorisOutputTable + "." + extention);
                jobName = "job_delete_" + datasetTable.getId();
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

        //trans
        JobEntryTrans transrans = new JobEntryTrans();
        transrans.setTransname(transName);
        transrans.setName("Transformation");
        JobEntryCopy transEntry = new JobEntryCopy(transrans);
        transEntry.setDrawn(true);
        transEntry.setLocation(300, 100);
        jobMeta.addJobEntry(transEntry);

        jobMeta.addJobHop(new JobHopMeta(startEntry, transEntry));

        //exec shell
        JobEntryShell shell = new JobEntryShell();
        shell.setScript(script);
        shell.insertScript = true;
        shell.setName("shell");
        JobEntryCopy shellEntry = new JobEntryCopy(shell);
        shellEntry.setDrawn(true);
        shellEntry.setLocation(500, 100);
        jobMeta.addJobEntry(shellEntry);

        JobHopMeta transHop = new JobHopMeta(transEntry, shellEntry);
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

    private String fetchSqlField(String sql, Datasource ds)throws Exception{
        String tmpSql = sql;
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        if(tmpSql.trim().endsWith(";")){
            tmpSql = tmpSql.substring(0, tmpSql.length() -1 ) + " limit 0";
        }else {
            tmpSql = tmpSql + " limit 0";
        }
        datasourceRequest.setQuery(tmpSql);
        return String.join(",", datasourceProvider.fetchResultField(datasourceRequest).stream().map(TableFiled::getFieldName).collect(Collectors.toList()));
    }

    private void generateTransFile(String extractType, DatasetTable datasetTable, Datasource datasource, List<DatasetTableField> datasetTableFields, String selectSQL) throws Exception {
        TransMeta transMeta = new TransMeta();
        String dorisOutputTable = null;
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasource.getType());
        DatabaseMeta dataMeta = null;
        StepMeta inputStep = null;
        StepMeta outputStep = null;
        StepMeta udjcStep = null;
        TransHopMeta hi1 = null;
        TransHopMeta hi2 = null;
        String transName = null;

        switch (datasourceType) {
            case mysql:
                MysqlConfigration mysqlConfigration = new Gson().fromJson(datasource.getConfiguration(), MysqlConfigration.class);
                dataMeta = new DatabaseMeta("db", "MYSQL", "Native", mysqlConfigration.getHost(), mysqlConfigration.getDataBase(), mysqlConfigration.getPort().toString(), mysqlConfigration.getUsername(), mysqlConfigration.getPassword());
                transMeta.addDatabase(dataMeta);
                if(extractType.equalsIgnoreCase("all_scope")){
                    String tableName = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getTable();
                    selectSQL = dataSetTableService.createQuerySQL(datasource.getType(), tableName, datasetTableFields.stream().map(DatasetTableField::getDataeaseName).toArray(String[]::new));
                }
                inputStep = inputStep(transMeta, selectSQL);
                break;
            case excel:
                String filePath = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getData();
                inputStep = excelInputStep(filePath, datasetTableFields);
            default:
                break;
        }

        switch (extractType) {
            case "all_scope":
                transName = "trans_" + datasetTable.getId();
                dorisOutputTable = DorisTableUtils.dorisTmpName(DorisTableUtils.dorisName(datasetTable.getId()));
                transMeta.setName(transName);
                break;
            case "incremental_add":
                transName = "trans_add_" + datasetTable.getId();
                dorisOutputTable = DorisTableUtils.dorisName(datasetTable.getId());
                transMeta.setName(transName);
                break;
            case "incremental_delete":
                dorisOutputTable = DorisTableUtils.dorisDeleteName(DorisTableUtils.dorisName(datasetTable.getId()));
                transName = "trans_delete_" + datasetTable.getId();
                transMeta.setName(transName);
                break;
            default:
                break;
        }

        udjcStep = udjc(datasetTableFields);
        outputStep = outputStep(dorisOutputTable);
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

    private StepMeta inputStep(TransMeta transMeta, String selectSQL){
        TableInputMeta tableInput = new TableInputMeta();
        DatabaseMeta database = transMeta.findDatabase("db");
        tableInput.setDatabaseMeta(database);
        tableInput.setSQL(selectSQL);
        StepMeta fromStep = new StepMeta("TableInput", "Data Input", tableInput);
        fromStep.setDraw(true);
        fromStep.setLocation(100, 100);
        return fromStep;
    }

    private StepMeta excelInputStep(String filePath, List<DatasetTableField> datasetTableFields){
        ExcelInputMeta excelInputMeta = new ExcelInputMeta();
        excelInputMeta.setSpreadSheetType(SpreadSheetType.SAX_POI);
        excelInputMeta.setPassword("Encrypted");
        excelInputMeta.setFileName(new String[]{filePath});
        excelInputMeta.setStartsWithHeader(true);
        ExcelInputField[] fields = new ExcelInputField[datasetTableFields.size()];
        for (int i=0; i<datasetTableFields.size();i++) {
            ExcelInputField field = new ExcelInputField();
            field.setName(datasetTableFields.get(i).getOriginName());
            field.setType("String");
            fields[i] = field;
        }

        excelInputMeta.setField(fields);
        StepMeta fromStep = new StepMeta("ExcelInput", "Data Input", excelInputMeta);
        fromStep.setDraw(true);
        fromStep.setLocation(100, 100);
        return fromStep;
    }

    private StepMeta outputStep(String dorisOutputTable){
        TextFileOutputMeta textFileOutputMeta = new TextFileOutputMeta();
        textFileOutputMeta.setEncoding("UTF-8");
        textFileOutputMeta.setHeaderEnabled(false);
        textFileOutputMeta.setFilename(root_path + dorisOutputTable);
        textFileOutputMeta.setSeparator(separator);
        textFileOutputMeta.setExtension(extention);
        textFileOutputMeta.setOutputFields(new TextFileField[0]);
        StepMeta outputStep = new StepMeta("TextFileOutput", "TextFileOutput", textFileOutputMeta);
        outputStep.setLocation(600, 100);
        outputStep.setDraw(true);
        return outputStep;
    }

    private StepMeta udjc(List<DatasetTableField> datasetTableFields){
        UserDefinedJavaClassMeta userDefinedJavaClassMeta = new UserDefinedJavaClassMeta();
        List<UserDefinedJavaClassMeta.FieldInfo> fields = new ArrayList<>();
        UserDefinedJavaClassMeta.FieldInfo fieldInfo = new UserDefinedJavaClassMeta.FieldInfo("dataease_uuid", ValueMetaInterface.TYPE_STRING, -1, -1);
        fields.add(fieldInfo);
        userDefinedJavaClassMeta.setFieldInfo(fields);
        List<UserDefinedJavaClassDef> definitions = new ArrayList<UserDefinedJavaClassDef>();
        UserDefinedJavaClassDef userDefinedJavaClassDef = new UserDefinedJavaClassDef(UserDefinedJavaClassDef.ClassType.TRANSFORM_CLASS, "Processor",
                code.replace("Column_Fields", String.join(",", datasetTableFields.stream().map(DatasetTableField::getOriginName).collect(Collectors.toList()))));
        userDefinedJavaClassDef.setActive(true);
        definitions.add(userDefinedJavaClassDef);
        userDefinedJavaClassMeta.replaceDefinitions(definitions);

        StepMeta userDefinedJavaClassStep = new StepMeta("UserDefinedJavaClass", "UserDefinedJavaClass", userDefinedJavaClassMeta);
        userDefinedJavaClassStep.setLocation(300, 100);
        userDefinedJavaClassStep.setDraw(true);
        return  userDefinedJavaClassStep;
    }

    private static String code = "import org.pentaho.di.core.row.ValueMetaInterface;\n" +
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

