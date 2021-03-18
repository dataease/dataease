package io.dataease.service.dataset;

import com.google.gson.Gson;
import io.dataease.base.domain.*;
import io.dataease.commons.constants.JobStatus;
import io.dataease.commons.constants.ScheduleType;
import io.dataease.commons.constants.UpdateType;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.LogUtil;
import io.dataease.dto.dataset.DataSetTaskLogDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private Long pageSize = 10000l;
    private static ExecutorService pool = Executors.newScheduledThreadPool(50);	//设置连接池
    private Connection connection;
    private static String lastUpdateTime = "${__last_update_time__}";
    private static String currentUpdateTime = "${__current_update_time__}";
    private static String column_family = "dataease";

    public void extractData(String datasetTableId, String taskId, String type) {
        DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
        UpdateType updateType = UpdateType.valueOf(type);
        try {
            Admin admin = getConnection().getAdmin();
            DatasetTable datasetTable = dataSetTableService.get(datasetTableId);
            List<DatasetTableField> datasetTableFields = dataSetTableFieldsService.list(DatasetTableField.builder().tableId(datasetTable.getId()).build());
            String table = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getTable();
            TableName tableName = TableName.valueOf(table + "-" + datasetTable.getDataSourceId());
            switch (updateType){
                // 全量更新
                case all_scope:
                    writeDatasetTableTaskLog(datasetTableTaskLog,datasetTableId, taskId);
                    if(!admin.tableExists(tableName)){
                        creatHaseTable(tableName, admin);
                    }
                    extractAllData(admin, tableName, table, datasetTable, datasetTableFields);
                    datasetTableTaskLog.setStatus(JobStatus.Completed.name());
                    datasetTableTaskLog.setEndTime(System.currentTimeMillis());
                    dataSetTableTaskLogService.save(datasetTableTaskLog);
                    break;
                case add_scope:
                    // 增量更新
                    if(!admin.tableExists(tableName)){
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
                    if(StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalAdd())){
                        String sql = datasetTableIncrementalConfig.getIncrementalAdd().replace(lastUpdateTime, dataSetTaskLogDTOS.get(0).getStartTime().toString()
                                .replace(currentUpdateTime, Long.valueOf(System.currentTimeMillis()).toString()));
                        extractIncrementalData(tableName,table,datasetTable, datasetTableFields, sql, "add");
                    }

                    // 增量删除
                    if( StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalDelete())){
                        String sql = datasetTableIncrementalConfig.getIncrementalDelete().replace(lastUpdateTime, dataSetTaskLogDTOS.get(0).getStartTime().toString()
                                .replace(currentUpdateTime, Long.valueOf(System.currentTimeMillis()).toString()));
                        extractIncrementalData(tableName,table,datasetTable, datasetTableFields, sql, "delete");
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

    private void creatHaseTable(TableName tableName, Admin admin)throws Exception{
        TableDescriptorBuilder descBuilder = TableDescriptorBuilder.newBuilder(tableName);
        ColumnFamilyDescriptor hcd = ColumnFamilyDescriptorBuilder.of(column_family);
        descBuilder.setColumnFamily(hcd);
        TableDescriptor desc = descBuilder.build();
        admin.createTable(desc);
    }

    private void extractAllData(Admin admin, TableName tableName, String table, DatasetTable datasetTable, List<DatasetTableField> datasetTableFields)throws Exception{
        admin.disableTable(tableName);
        admin.truncateTable(tableName, true);

        Table tab = getConnection().getTable(tableName);
        Long total = dataSetTableService.getDataSetTotalData(datasetTable.getDataSourceId(), table);
        Long pageCount = total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1;

        for (Long pageIndex = 1l; pageIndex <= pageCount; pageIndex++) {
            List<String[]> data = dataSetTableService.getDataSetPageData(datasetTable.getDataSourceId(), table, datasetTableFields, pageIndex, pageSize);
            insertDataToHbaseTable(data,datasetTableFields,tab);
        }
    }

    private void extractIncrementalData(TableName tableName, String table, DatasetTable datasetTable, List<DatasetTableField> datasetTableFields, String sql, String type)throws Exception{
        Table tab = getConnection().getTable(tableName);
        List<String[]> data = dataSetTableService.getDataSetDataBySql(datasetTable.getDataSourceId(), table, sql);
        if (type.equalsIgnoreCase("add")){
            insertDataToHbaseTable(data,datasetTableFields,tab);
        }else {
            deleteDataFromHbaseTable(data,datasetTableFields,tab);
        }
    }

    private void insertDataToHbaseTable(List<String[]> data, List<DatasetTableField> datasetTableFields, Table tab)throws Exception{
        for (String[] d : data) {
            Put put = new Put(md5(generateStr(datasetTableFields.size(), d)).getBytes());
            for(int i=0;i<datasetTableFields.size();i++){
                String value = d[i];
                if(value == null){
                    value = "null";
                }
                put.addColumn(column_family.getBytes(), datasetTableFields.get(i).getOriginName().getBytes(), value.getBytes());
            }
            tab.put(put);
        }
    }

    private void deleteDataFromHbaseTable(List<String[]> data, List<DatasetTableField> datasetTableFields, Table tab)throws Exception{
        for (String[] d : data) {
            Delete delete = new Delete(md5(generateStr(datasetTableFields.size(), d)).getBytes());
            tab.delete(delete);
        }
    }

    private synchronized Connection getConnection() throws Exception{
        if(connection == null || connection.isClosed()){
            Configuration cfg = CommonBeanFactory.getBean(Configuration.class);
            connection = ConnectionFactory.createConnection(cfg, pool);
        }
        return connection;
    }


    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String UTF_8 = "UTF-8";

    public static String md5(String src) {
        return md5(src, UTF_8);
    }

    public static String md5(String src, String charset) {
        try {
            byte[] strTemp = io.micrometer.core.instrument.util.StringUtils.isEmpty(charset) ? src.getBytes() : src.getBytes(charset);
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(strTemp);

            byte[] md = mdTemp.digest();
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;

            for (byte byte0 : md) {
                str[k++] = HEX_DIGITS[byte0 >>> 4 & 0xf];
                str[k++] = HEX_DIGITS[byte0 & 0xf];
            }

            return new String(str);
        } catch (Exception e) {
            throw new RuntimeException("MD5 encrypt error:", e);
        }
    }

    public String generateStr(int size, String[] d ){
        String str = null;
        for(int i=0;i<size;i++){
            str = str + d[i];
        }
        return str;
    }

}
