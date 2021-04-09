package io.dataease.service.dataset;

import com.google.gson.Gson;
import com.sun.org.apache.bcel.internal.generic.SWITCH;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.DatasourceMapper;
import io.dataease.commons.constants.JobStatus;
import io.dataease.commons.constants.ScheduleType;
import io.dataease.commons.constants.UpdateType;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.LogUtil;
import io.dataease.datasource.constants.DatasourceTypes;
import io.dataease.datasource.dto.MysqlConfigrationDTO;
import io.dataease.dto.dataset.DataSetTaskLogDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.pentaho.big.data.api.cluster.NamedCluster;
import org.pentaho.big.data.api.cluster.NamedClusterService;
import org.pentaho.big.data.api.cluster.service.locator.NamedClusterServiceLocator;
import org.pentaho.big.data.api.cluster.service.locator.impl.NamedClusterServiceLocatorImpl;
import org.pentaho.big.data.api.initializer.ClusterInitializer;
import org.pentaho.big.data.api.initializer.ClusterInitializerProvider;
import org.pentaho.big.data.api.initializer.impl.ClusterInitializerImpl;
import org.pentaho.big.data.impl.cluster.NamedClusterImpl;
import org.pentaho.big.data.impl.cluster.NamedClusterManager;
import org.pentaho.big.data.kettle.plugins.hbase.MappingDefinition;
import org.pentaho.big.data.kettle.plugins.hbase.output.HBaseOutputMeta;
import org.pentaho.di.cluster.SlaveServer;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.engine.configuration.impl.pentaho.DefaultRunConfiguration;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobExecutionConfiguration;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entries.success.JobEntrySuccess;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entries.writetolog.JobEntryWriteToLog;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.repository.filerep.KettleFileRepositoryMeta;
import org.pentaho.di.trans.TransConfiguration;
import org.pentaho.di.trans.TransExecutionConfiguration;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.userdefinedjavaclass.InfoStepDefinition;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassDef;
import org.pentaho.di.trans.steps.userdefinedjavaclass.UserDefinedJavaClassMeta;
import org.pentaho.di.www.SlaveServerJobStatus;
import org.pentaho.runtime.test.RuntimeTest;
import org.pentaho.runtime.test.RuntimeTester;
import org.pentaho.runtime.test.action.RuntimeTestActionHandler;
import org.pentaho.runtime.test.action.RuntimeTestActionService;
import org.pentaho.runtime.test.action.impl.RuntimeTestActionServiceImpl;
import org.pentaho.runtime.test.impl.RuntimeTesterImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.pentaho.di.core.row.ValueMetaInterface;
import scala.annotation.meta.field;

import javax.annotation.Resource;
import javax.sound.sampled.Line;
import java.io.File;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.mock;

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
    private static ExecutorService pool = Executors.newScheduledThreadPool(50);	//设置连接池
    private Connection connection;

    private static String lastUpdateTime = "${__last_update_time__}";
    private static String currentUpdateTime = "${__current_update_time__}";
    private static String dataease_column_family = "dataease";
    private static String root_path = "/opt/dataease/data/kettle/";
    private static String hbase_conf_file = "/opt/dataease/conf/hbase-site.xml";
    private static String pentaho_mappings = "pentaho_mappings";

    @Value("${carte.host:127.0.0.1}")
    private String carte;
    @Value("${carte.port:8080}")
    private String port;
    @Value("${carte.user:cluster}")
    private String user;
    @Value("${carte.passwd:cluster}")
    private String passwd;
    @Value("${hbase.zookeeper.quorum:zookeeper}")
    private String zkHost;
    @Value("${hbase.zookeeper.property.clientPort:2181}")
    private String zkPort;


    public void extractData(String datasetTableId, String taskId, String type) {
        DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
        UpdateType updateType = UpdateType.valueOf(type);
        try {
            Admin admin = getConnection().getAdmin();
            DatasetTable datasetTable = dataSetTableService.get(datasetTableId);
            Datasource datasource = datasourceMapper.selectByPrimaryKey(datasetTable.getDataSourceId());
            List<DatasetTableField> datasetTableFields = dataSetTableFieldsService.list(DatasetTableField.builder().tableId(datasetTable.getId()).build());
            String table = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getTable();
            TableName hbaseTable = TableName.valueOf(datasetTableId);
            switch (updateType){
                // 全量更新
                case all_scope:
                    writeDatasetTableTaskLog(datasetTableTaskLog, datasetTableId, taskId);

                    //check pentaho_mappings table
                    TableName pentaho_mappings = TableName.valueOf(this.pentaho_mappings);
                    if(!admin.tableExists(pentaho_mappings)){
                        creatHaseTable(pentaho_mappings, admin, Arrays.asList("columns","key"));
                    }

                    //check pentaho files
                    if(!isExitFile("job_" + datasetTableId + ".kjb") || !isExitFile("trans_" + datasetTableId + ".ktr")){
                        generateTransFile("all_scope", datasetTable, datasource, table, datasetTableFields, null);
                        generateJobFile("all_scope", datasetTable);
                    }

                    if(!admin.tableExists(hbaseTable)){
                        creatHaseTable(hbaseTable, admin, Arrays.asList(dataease_column_family));
                    }
                    admin.disableTable(hbaseTable);
                    admin.truncateTable(hbaseTable, true);

                    extractData(datasetTable, "all_scope");
                    datasetTableTaskLog.setStatus(JobStatus.Completed.name());
                    datasetTableTaskLog.setEndTime(System.currentTimeMillis());
                    dataSetTableTaskLogService.save(datasetTableTaskLog);
                    break;
                case add_scope:
                    // 增量更新
                    if(!admin.tableExists(hbaseTable)){
                        LogUtil.error("TableName error, dataaset: " + datasetTableId);
                        return;
                    }
                    DatasetTableIncrementalConfig datasetTableIncrementalConfig = dataSetTableService.incrementalConfig(datasetTableId);
                    if(datasetTableIncrementalConfig == null || StringUtils.isEmpty(datasetTableIncrementalConfig.getTableId())){
                        return;
                    }
                    DatasetTableTaskLog request = new DatasetTableTaskLog();
                    request.setTableId(datasetTableId);
                    request.setStatus(JobStatus.Completed.name());
                    List<DataSetTaskLogDTO> dataSetTaskLogDTOS = dataSetTableTaskLogService.list(request);
                    if(CollectionUtils.isEmpty(dataSetTaskLogDTOS)){
                        return;
                    }
                    writeDatasetTableTaskLog(datasetTableTaskLog,datasetTableId, taskId);

                    // 增量添加
                    if(StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalAdd().replace(" ", ""))){
                        System.out.println("datasetTableIncrementalConfig.getIncrementalAdd(): " + datasetTableIncrementalConfig.getIncrementalAdd());
                        String sql = datasetTableIncrementalConfig.getIncrementalAdd().replace(lastUpdateTime, dataSetTaskLogDTOS.get(0).getStartTime().toString()
                                .replace(currentUpdateTime, Long.valueOf(System.currentTimeMillis()).toString()));

                        if(!isExitFile("job_add_" + datasetTableId + ".kjb") || !isExitFile("trans_add_" + datasetTableId + ".ktr")){
                            generateTransFile("incremental_add", datasetTable, datasource, table, datasetTableFields, sql);
                            generateJobFile("incremental_add", datasetTable);
                        }

                        extractData(datasetTable, "incremental_add");
                    }

                    // 增量删除
                    if( StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalDelete())){
                        String sql = datasetTableIncrementalConfig.getIncrementalDelete().replace(lastUpdateTime, dataSetTaskLogDTOS.get(0).getStartTime().toString()
                                .replace(currentUpdateTime, Long.valueOf(System.currentTimeMillis()).toString()));
                        if(!isExitFile("job_delete_" + datasetTableId + ".kjb") || !isExitFile("trans_delete_" + datasetTableId + ".ktr")){
                            generateTransFile("incremental_delete", datasetTable, datasource, table, datasetTableFields, sql);
                            generateJobFile("incremental_delete", datasetTable);
                        }
                        extractData(datasetTable, "incremental_delete");
                    }

                    datasetTableTaskLog.setStatus(JobStatus.Completed.name());
                    datasetTableTaskLog.setEndTime(System.currentTimeMillis());
                    dataSetTableTaskLogService.save(datasetTableTaskLog);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
            LogUtil.error("ExtractData error, dataaset: " + datasetTableId);
            LogUtil.error(e.getMessage(), e);
            datasetTableTaskLog.setStatus(JobStatus.Error.name());
            datasetTableTaskLog.setEndTime(System.currentTimeMillis());
            dataSetTableTaskLogService.save(datasetTableTaskLog);
        }
        finally {
            DatasetTableTask datasetTableTask = dataSetTableTaskService.get(taskId);
            if (datasetTableTask != null && datasetTableTask.getRate().equalsIgnoreCase(ScheduleType.SIMPLE.toString())){
                datasetTableTask.setRate(ScheduleType.SIMPLE_COMPLETE.toString());
                dataSetTableTaskService.update(datasetTableTask);
            }
        }
    }

    private void writeDatasetTableTaskLog(DatasetTableTaskLog datasetTableTaskLog, String datasetTableId, String taskId){
        datasetTableTaskLog.setTableId(datasetTableId);
        datasetTableTaskLog.setTaskId(taskId);
        datasetTableTaskLog.setStatus(JobStatus.Underway.name());
        datasetTableTaskLog.setStartTime(System.currentTimeMillis());
        dataSetTableTaskLogService.save(datasetTableTaskLog);
    }

    private void creatHaseTable(TableName tableName, Admin admin, List<String> columnFamily)throws Exception{
        TableDescriptorBuilder descBuilder = TableDescriptorBuilder.newBuilder(tableName);
        Collection<ColumnFamilyDescriptor> families = new ArrayList<>();
        for (String s : columnFamily) {
            ColumnFamilyDescriptor hcd = ColumnFamilyDescriptorBuilder.of(s);
            families.add(hcd);
        }
        descBuilder.setColumnFamilies(families);
        TableDescriptor desc = descBuilder.build();
        admin.createTable(desc);
    }

    private void extractData(DatasetTable datasetTable, String extractType)throws Exception{
        KettleFileRepository repository = CommonBeanFactory.getBean(KettleFileRepository.class);
        RepositoryDirectoryInterface repositoryDirectoryInterface = repository.loadRepositoryDirectoryTree();
        JobMeta jobMeta = null;
        switch (extractType){
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
        do {
            jobStatus = remoteSlaveServer.getJobStatus(jobMeta.getName(), lastCarteObjectId, 0);
        } while (jobStatus != null && jobStatus.isRunning());
       if(jobStatus.getStatusDescription().equals("Finished")){
            return;
       }else {
            throw new Exception(jobStatus.getLoggingString());
       }
    }

    private synchronized Connection getConnection() throws Exception{
        if(connection == null || connection.isClosed()){
            Configuration cfg = CommonBeanFactory.getBean(Configuration.class);
            connection = ConnectionFactory.createConnection(cfg, pool);
        }
        return connection;
    }

    private boolean isExitFile(String fileName){
        File file=new File(root_path + fileName);
        return file.exists();
    }

    private SlaveServer getSlaveServer(){
        SlaveServer remoteSlaveServer = new SlaveServer();
        remoteSlaveServer.setHostname(carte);// 设置远程IP
        remoteSlaveServer.setPort(port);// 端口
        remoteSlaveServer.setUsername(user);
        remoteSlaveServer.setPassword(passwd);
        return remoteSlaveServer;
    }

    private void generateJobFile(String extractType, DatasetTable datasetTable) throws Exception{
        String jobName = null;
        switch (extractType) {
            case "all_scope":
                jobName = "job_" + datasetTable.getId();
                break;
            case "incremental_add":
                jobName =  "job_add_" + datasetTable.getId();
                break;
            case "incremental_delete":
                jobName = "job_delete_" + datasetTable.getId();
                break;
            default:
                break;
        }

        String transName = null;
        switch (extractType) {
            case "all_scope":
                transName = "trans_" + datasetTable.getId();
                break;
            case "incremental_add":
                transName =  "trans_add_" + datasetTable.getId();
                break;
            case "incremental_delete":
                transName = "trans_delete_" + datasetTable.getId();
                break;
            default:
                break;
        }

        JobMeta jobMeta = new JobMeta();
        jobMeta.setName(jobName);
        JobEntrySpecial start = new JobEntrySpecial();
        start.setName("START");
        start.setStart(true);
        JobEntryCopy startEntry = new JobEntryCopy(start);
        startEntry.setDrawn(true);
        startEntry.setLocation(100, 100);
        jobMeta.addJobEntry(startEntry);

        JobEntryTrans transrans = new JobEntryTrans();
        transrans.setTransname(transName);
        transrans.setName("Transformation");
        JobEntryCopy transEntry = new JobEntryCopy(transrans);
        transEntry.setDrawn(true);
        transEntry.setLocation(300, 100);
        jobMeta.addJobEntry(transEntry);

        jobMeta.addJobHop(new JobHopMeta(startEntry, transEntry));

        JobEntrySuccess success = new JobEntrySuccess();
        success.setName("Success");
        JobEntryCopy successEntry = new JobEntryCopy(success);
        successEntry.setDrawn(true);
        successEntry.setLocation(500, 100);
        jobMeta.addJobEntry(successEntry);

        JobHopMeta greenHop = new JobHopMeta(transEntry, successEntry);
        greenHop.setEvaluation(true);
        jobMeta.addJobHop(greenHop);

        String jobXml = jobMeta.getXML();
        File file = new File( root_path + jobName + ".kjb");
        FileUtils.writeStringToFile(file, jobXml, "UTF-8");
    }

    private void generateTransFile(String extractType, DatasetTable datasetTable, Datasource datasource, String table, List<DatasetTableField> datasetTableFields, String selectSQL) throws Exception{
        TransMeta transMeta = new TransMeta();
        String transName = null;
        switch (extractType) {
            case "all_scope":
                transName = "trans_" + datasetTable.getId();
                selectSQL = dataSetTableService.createQuerySQL(datasource.getType(), table, datasetTableFields.stream().map(DatasetTableField::getOriginName).toArray(String[]::new));
                break;
            case "incremental_add":
                transName =  "trans_add_" + datasetTable.getId();
                break;
            case "incremental_delete":
                transName = "trans_delete_" + datasetTable.getId();
                break;
            default:
                break;
        }

        transMeta.setName(transName);
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasource.getType());
        DatabaseMeta dataMeta = null;
        switch (datasourceType) {
            case mysql:
                MysqlConfigrationDTO mysqlConfigrationDTO = new Gson().fromJson(datasource.getConfiguration(), MysqlConfigrationDTO.class);
                dataMeta = new DatabaseMeta("db", "MYSQL", "Native", mysqlConfigrationDTO.getHost(), mysqlConfigrationDTO.getDataBase(), mysqlConfigrationDTO.getPort().toString(), mysqlConfigrationDTO.getUsername(), mysqlConfigrationDTO.getPassword());
                transMeta.addDatabase(dataMeta);
                break;
            default:
                break;

        }
        //registry是给每个步骤生成一个标识id
        PluginRegistry registry = PluginRegistry.getInstance();
        //第一个表输入步骤(TableInputMeta)
        TableInputMeta tableInput = new TableInputMeta();

        //给表输入添加一个DatabaseMeta连接数据库
        DatabaseMeta database_bjdt = transMeta.findDatabase("db");
        tableInput.setDatabaseMeta(database_bjdt);
        tableInput.setSQL(selectSQL);
        //添加TableInputMeta到转换中
        String tableInputPluginId = registry.getPluginId(StepPluginType.class, tableInput);
        StepMeta fromStep = new StepMeta(tableInputPluginId, "Data Input", tableInput);
        //给步骤添加在spoon工具中的显示位置
        fromStep.setDraw(true);
        fromStep.setLocation(100, 100);
        transMeta.addStep(fromStep);

        //第二个 (User defined Java class)
        UserDefinedJavaClassMeta userDefinedJavaClassMeta = new UserDefinedJavaClassMeta();
        List<UserDefinedJavaClassMeta.FieldInfo> fields = new ArrayList<>();
        UserDefinedJavaClassMeta.FieldInfo fieldInfo = new UserDefinedJavaClassMeta.FieldInfo("uuid", ValueMetaInterface.TYPE_STRING, -1, -1);
        fields.add(fieldInfo);
        userDefinedJavaClassMeta.setFieldInfo(fields);
        List<UserDefinedJavaClassDef> definitions = new ArrayList<UserDefinedJavaClassDef>();
        UserDefinedJavaClassDef userDefinedJavaClassDef = new UserDefinedJavaClassDef(UserDefinedJavaClassDef.ClassType.TRANSFORM_CLASS, "Processor", code);
        userDefinedJavaClassDef.setActive(true);
        definitions.add(userDefinedJavaClassDef);
        userDefinedJavaClassMeta.replaceDefinitions(definitions);

        StepMeta userDefinedJavaClassStep = new StepMeta("UserDefinedJavaClass", "UserDefinedJavaClass", userDefinedJavaClassMeta);
        userDefinedJavaClassStep.setLocation(300, 100);
        userDefinedJavaClassStep.setDraw(true);
        transMeta.addStep(userDefinedJavaClassStep);

        //第三个 (HBaseOutputMeta)
        NamedClusterService namedClusterService = new NamedClusterManager();
        NamedCluster clusterTemplate = new NamedClusterImpl();
        clusterTemplate.setName("hadoop");
        clusterTemplate.setZooKeeperHost(zkHost);
        clusterTemplate.setZooKeeperPort(zkPort);
        clusterTemplate.setStorageScheme("HDFS");
        namedClusterService.setClusterTemplate(clusterTemplate);

        List<ClusterInitializerProvider> providers = new ArrayList<>();
        ClusterInitializer clusterInitializer = new ClusterInitializerImpl(providers);
        NamedClusterServiceLocator namedClusterServiceLocator = new NamedClusterServiceLocatorImpl(clusterInitializer);

        List<RuntimeTestActionHandler> runtimeTestActionHandlers = new ArrayList<>();
        RuntimeTestActionHandler defaultHandler = null;

        RuntimeTestActionService runtimeTestActionService = new RuntimeTestActionServiceImpl(runtimeTestActionHandlers, defaultHandler);
        RuntimeTester runtimeTester = new RuntimeTesterImpl(new ArrayList<>( Arrays.asList( mock( RuntimeTest.class ) ) ), mock( ExecutorService.class ), "modules");

        Put put = new Put((datasetTable.getId() + "," + "target_mapping").getBytes());
        for (DatasetTableField datasetTableField : datasetTableFields) {
            put.addColumn("columns".getBytes(), (dataease_column_family + "," + datasetTableField.getOriginName() +  "," + datasetTableField.getOriginName()).getBytes(), transToColumnType(datasetTableField.getDeType()).getBytes());
        }
        put.addColumn("key".getBytes(), "uuid".getBytes(), "String".getBytes());
        TableName pentaho_mappings = TableName.valueOf(this.pentaho_mappings);
        Table tab = getConnection().getTable(pentaho_mappings);
        tab.put(put);

        HBaseOutputMeta hBaseOutputMeta = new HBaseOutputMeta(namedClusterService, namedClusterServiceLocator, runtimeTestActionService, runtimeTester);
        hBaseOutputMeta.setTargetTableName(datasetTable.getId());
        hBaseOutputMeta.setTargetMappingName("target_mapping");
        hBaseOutputMeta.setNamedCluster(clusterTemplate);
        hBaseOutputMeta.setCoreConfigURL(hbase_conf_file);
        if(extractType.equalsIgnoreCase("incremental_delete")){
            hBaseOutputMeta.setDeleteRowKey(true);
        }
        StepMeta tostep = new StepMeta("HBaseOutput", "HBaseOutput", hBaseOutputMeta);
        tostep.setLocation(600, 100);

        tostep.setDraw(true);
        transMeta.addStep(tostep);
        TransHopMeta hi1 = new TransHopMeta(fromStep, userDefinedJavaClassStep);
        TransHopMeta hi2 = new TransHopMeta(userDefinedJavaClassStep, tostep);
        transMeta.addTransHop(hi1);
        transMeta.addTransHop(hi2);

        String transXml = transMeta.getXML();
        File file = new File(root_path + transName + ".ktr");
        FileUtils.writeStringToFile(file, transXml, "UTF-8");
    }

    public String transToColumnType(Integer field) {
        switch (field) {
            case 0:
                return "String";
            case 1:
                return "Date";
            case 2:
                return "BigNumber";
            default:
                return "String";
        }
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
            "\n" +
            "public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {\n" +
            "  if (first) {\n" +
            "    first = false;\n" +
            "\n" +
            "    /* TODO: Your code here. (Using info fields)\n" +
            "\n" +
            "    FieldHelper infoField = get(Fields.Info, \"info_field_name\");\n" +
            "\n" +
            "    RowSet infoStream = findInfoRowSet(\"info_stream_tag\");\n" +
            "\n" +
            "    Object[] infoRow = null;\n" +
            "\n" +
            "    int infoRowCount = 0;\n" +
            "\n" +
            "    // Read all rows from info step before calling getRow() method, which returns first row from any\n" +
            "    // input rowset. As rowMeta for info and input steps varies getRow() can lead to errors.\n" +
            "    while((infoRow = getRowFrom(infoStream)) != null){\n" +
            "\n" +
            "      // do something with info data\n" +
            "      infoRowCount++;\n" +
            "    }\n" +
            "    */\n" +
            "  }\n" +
            "\n" +
            "  Object[] r = getRow();\n" +
            "\n" +
            "  if (r == null) {\n" +
            "    setOutputDone();\n" +
            "    return false;\n" +
            "  }\n" +
            "\n" +
            "  // It is always safest to call createOutputRow() to ensure that your output row's Object[] is large\n" +
            "  // enough to handle any new fields you are creating in this step.\n" +
            "  r = createOutputRow(r, data.outputRowMeta.size());\n" +
            "  String str = \"\";\n" +
            "  List<ValueMetaInterface> valueMetaList = data.outputRowMeta.getValueMetaList();\n" +
            "  for (ValueMetaInterface valueMetaInterface : valueMetaList) {\n" +
            "\t if(!valueMetaInterface.getName().equalsIgnoreCase(\"uuid\")){\n" +
            "       str = str + get(Fields.In, valueMetaInterface.getName()).getString(r);\n" +
            "     }\n" +
            "   }\n" +
            "\n" +
            "  String md5 = md5(str);\n" +
            "  get(Fields.Out, \"uuid\").setValue(r, md5);\n" +
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
            "    }\n";
}

