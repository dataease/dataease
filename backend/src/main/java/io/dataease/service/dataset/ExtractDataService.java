package io.dataease.service.dataset;

import com.google.gson.Gson;
import io.dataease.base.domain.DatasetTable;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.DatasetTableTaskLog;
import io.dataease.commons.constants.JobStatus;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.dto.dataset.DataTableInfoDTO;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    private Long pageSize = 10000l;
    private static ExecutorService pool = Executors.newScheduledThreadPool(50);	//设置连接池
    private Connection connection;

    public void extractData(String datasetTableId, String taskId) {
        DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
        try {
            datasetTableTaskLog.setTableId(datasetTableId);
            datasetTableTaskLog.setTaskId(taskId);
            datasetTableTaskLog.setStatus(JobStatus.Underway.name());
            datasetTableTaskLog.setStartTime(System.currentTimeMillis());
            dataSetTableTaskLogService.save(datasetTableTaskLog);
            Admin admin = getConnection().getAdmin();
            DatasetTable datasetTable = dataSetTableService.get(datasetTableId);
            List<DatasetTableField> datasetTableFields = dataSetTableFieldsService.list(DatasetTableField.builder().tableId(datasetTable.getId()).build());
            String table = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getTable();
            TableName tableName = TableName.valueOf(table + "-" + datasetTable.getDataSourceId());
            if(!admin.tableExists(tableName)){
                TableDescriptorBuilder descBuilder = TableDescriptorBuilder.newBuilder(tableName);
                ColumnFamilyDescriptor hcd = ColumnFamilyDescriptorBuilder.of("cf");
                descBuilder.setColumnFamily(hcd);
                TableDescriptor desc = descBuilder.build();
                admin.createTable(desc);
            }
            admin.disableTable(tableName);
            admin.truncateTable(tableName, true);

            Table tab = getConnection().getTable(tableName);
            Long total = dataSetTableService.getDataSetTotalData(datasetTable.getDataSourceId(), table);
            Long pageCount = total % pageSize == 0 ? total / pageSize : (total / pageSize) + 1;

            for (Long pageIndex = 1l; pageIndex <= pageCount; pageIndex++) {
                List<String[]> data = dataSetTableService.getDataSetPageData(datasetTable.getDataSourceId(), table, datasetTableFields, pageIndex, pageSize);
                for (String[] d : data) {
                    for(int i=0;i<datasetTableFields.size();i++){
                        Put put = new Put(UUID.randomUUID().toString().getBytes());
                        String value = d[i];
                        if(value == null){
                            value = "null";
                        }
                        put.addColumn("cf".getBytes(), datasetTableFields.get(i).getOriginName().getBytes(), value.getBytes());
                        tab.put(put);
                    }
                }
            }
            datasetTableTaskLog.setStatus(JobStatus.Completed.name());
            datasetTableTaskLog.setEndTime(System.currentTimeMillis());
            dataSetTableTaskLogService.save(datasetTableTaskLog);
        }catch (Exception e){
            datasetTableTaskLog.setStatus(JobStatus.Error.name());
            datasetTableTaskLog.setEndTime(System.currentTimeMillis());
            dataSetTableTaskLogService.save(datasetTableTaskLog);
        }
    }


    private synchronized Connection getConnection() throws Exception{
        if(connection == null || connection.isClosed()){
            Configuration cfg = CommonBeanFactory.getBean(Configuration.class);
            connection = ConnectionFactory.createConnection(cfg, pool);
        }
        return connection;
    }
}
