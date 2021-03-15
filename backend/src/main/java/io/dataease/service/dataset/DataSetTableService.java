package io.dataease.service.dataset;


import com.google.gson.Gson;
import io.dataease.base.domain.DatasetTable;
import io.dataease.base.domain.DatasetTableExample;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.Datasource;
import io.dataease.base.mapper.DatasetTableMapper;
import io.dataease.base.mapper.DatasourceMapper;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.datasource.constants.DatasourceTypes;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.datasource.provider.DatasourceProvider;
import io.dataease.datasource.provider.ProviderFactory;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.dto.dataset.DataTableInfoDTO;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/2/23 2:54 下午
 */
@Service
public class DataSetTableService {
    @Resource
    private DatasetTableMapper datasetTableMapper;
    @Resource
    private DatasourceMapper datasourceMapper;
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;
    @Resource
    private DataSetTableTaskService dataSetTableTaskService;

    public void batchInsert(List<DatasetTable> datasetTable) throws Exception {
        for (DatasetTable table : datasetTable) {
            save(table);
        }
    }

    public DatasetTable save(DatasetTable datasetTable) throws Exception {
        if (StringUtils.isEmpty(datasetTable.getId())) {
            datasetTable.setId(UUID.randomUUID().toString());
            datasetTable.setCreateTime(System.currentTimeMillis());
            DataTableInfoDTO dataTableInfoDTO = new DataTableInfoDTO();
            if (StringUtils.equalsIgnoreCase("db", datasetTable.getType())) {
                dataTableInfoDTO.setTable(datasetTable.getName());
                datasetTable.setInfo(new Gson().toJson(dataTableInfoDTO));
            }
            int insert = datasetTableMapper.insert(datasetTable);
            // 添加表成功后，获取当前表字段和类型，抽象到dataease数据库
            if (insert == 1) {
                saveTableField(datasetTable);
            }
        } else {
            datasetTableMapper.updateByPrimaryKeyWithBLOBs(datasetTable);
        }
        return datasetTable;
    }

    public void delete(String id) {
        datasetTableMapper.deleteByPrimaryKey(id);
        dataSetTableFieldsService.deleteByTableId(id);
        // 删除同步任务
        dataSetTableTaskService.deleteByTableId(id);
    }

    public List<DatasetTable> list(DataSetTableRequest dataSetTableRequest) {
        DatasetTableExample datasetTableExample = new DatasetTableExample();
        if (StringUtils.isNotEmpty(dataSetTableRequest.getSceneId())) {
            datasetTableExample.createCriteria().andSceneIdEqualTo(dataSetTableRequest.getSceneId());
        }
        if (StringUtils.isNotEmpty(dataSetTableRequest.getSort())) {
            datasetTableExample.setOrderByClause(dataSetTableRequest.getSort());
        }
        return datasetTableMapper.selectByExampleWithBLOBs(datasetTableExample);
    }

    public DatasetTable get(String id) {
        return datasetTableMapper.selectByPrimaryKey(id);
    }

    public List<TableFiled> getFields(DataSetTableRequest dataSetTableRequest) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceRequest.setTable(new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class).getTable());
        return datasourceProvider.getTableFileds(datasourceRequest);
    }

    public Map<String, List<DatasetTableField>> getFieldsFromDE(DataSetTableRequest dataSetTableRequest) throws Exception {
        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(dataSetTableRequest.getId());
        datasetTableField.setChecked(Boolean.TRUE);
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);

        List<DatasetTableField> dimension = new ArrayList<>();
        List<DatasetTableField> quota = new ArrayList<>();

        fields.forEach(field -> {
            if (field.getDeType() == 2) {
                quota.add(field);
            } else {
                dimension.add(field);
            }
        });
        Map<String, List<DatasetTableField>> map = new HashMap<>();
        map.put("dimension", dimension);
        map.put("quota", quota);

        return map;
    }

    public List<String[]> getData(DataSetTableRequest dataSetTableRequest) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        String table = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class).getTable();

        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(dataSetTableRequest.getId());
        datasetTableField.setChecked(Boolean.TRUE);
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
        String[] fieldArray = fields.stream().map(DatasetTableField::getOriginName).toArray(String[]::new);
        datasourceRequest.setQuery(createQuerySQL(ds.getType(), table, fieldArray));

        return datasourceProvider.getData(datasourceRequest);
    }

    public Map<String, Object> getPreviewData(DataSetTableRequest dataSetTableRequest) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);

        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(dataSetTableRequest.getId());
        datasetTableField.setChecked(Boolean.TRUE);
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
        String[] fieldArray = fields.stream().map(DatasetTableField::getOriginName).toArray(String[]::new);

        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
        DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(dataSetTableRequest.getId());
        if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "db")) {
            String table = dataTableInfoDTO.getTable();
            datasourceRequest.setQuery(createQuerySQL(ds.getType(), table, fieldArray) + " LIMIT 0,10");// todo limit
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "sql")) {
            String sql = dataTableInfoDTO.getSql();
            datasourceRequest.setQuery(createQuerySQL(ds.getType(), " (" + sql + ") AS tmp ", fieldArray));// todo 因为编辑可能取消某些字段展示，这里sql看看怎么处理
        }

        List<String[]> data = new ArrayList<>();
        try {
            data.addAll(datasourceProvider.getData(datasourceRequest));
        } catch (Exception e) {
        }

        List<Map<String, Object>> jsonArray = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(data)) {
            jsonArray = data.stream().map(ele -> {
                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < ele.length; i++) {
                    map.put(fieldArray[i], ele[i]);
                }
                return map;
            }).collect(Collectors.toList());
        }

        Map<String, Object> map = new HashMap<>();
        map.put("fields", fields);
        map.put("data", jsonArray);

        return map;
    }

    public Map<String, Object> getSQLPreview(DataSetTableRequest dataSetTableRequest) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        String sql = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class).getSql();
        datasourceRequest.setQuery(sql);
        ResultSet dataResultSet = datasourceProvider.getDataResultSet(datasourceRequest);
        List<String[]> data = datasourceProvider.fetchResult(dataResultSet);
        List<TableFiled> fields = datasourceProvider.fetchResultField(dataResultSet);
        String[] fieldArray = fields.stream().map(TableFiled::getFieldName).toArray(String[]::new);

        List<Map<String, Object>> jsonArray = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(data)) {
            jsonArray = data.stream().map(ele -> {
                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < ele.length; i++) {
                    map.put(fieldArray[i], ele[i]);
                }
                return map;
            }).collect(Collectors.toList());
        }

        Map<String, Object> map = new HashMap<>();
        map.put("fields", fields);
        map.put("data", jsonArray);

        return map;
    }

    public List<String[]> getDataSetData(String datasourceId, String table, List<DatasetTableField> fields) {
        List<String[]> data = new ArrayList<>();
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasourceId);
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        String[] fieldArray = fields.stream().map(DatasetTableField::getOriginName).toArray(String[]::new);
        datasourceRequest.setQuery(createQuerySQL(ds.getType(), table, fieldArray) + " LIMIT 0, 10");
        try {
            data.addAll(datasourceProvider.getData(datasourceRequest));
        } catch (Exception e) {
        }
        return data;
    }

    public Long getDataSetTotalData(String datasourceId, String table) {
        List<String[]> data = new ArrayList<>();
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasourceId);
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceRequest.setQuery("select count(*) from " + table);
        try {
            return datasourceProvider.count(datasourceRequest);
        } catch (Exception e) {

        }
        return 0l;
    }

    public List<String[]> getDataSetPageData(String datasourceId, String table, List<DatasetTableField> fields, Long startPage, Long pageSize) {
        List<String[]> data = new ArrayList<>();
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasourceId);
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        String[] fieldArray = fields.stream().map(DatasetTableField::getOriginName).toArray(String[]::new);
        datasourceRequest.setPageSize(pageSize);
        datasourceRequest.setStartPage(startPage);
        datasourceRequest.setQuery(createQuerySQL(ds.getType(), table, fieldArray));
        try {
            return datasourceProvider.getData(datasourceRequest);
        } catch (Exception e) {
        }
        return data;
    }

    public void saveTableField(DatasetTable datasetTable) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasetTable.getDataSourceId());
        DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
        BeanUtils.copyBean(dataSetTableRequest, datasetTable);

        List<TableFiled> fields = new ArrayList<>();
        long syncTime = System.currentTimeMillis();
        if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "db")) {
            fields = getFields(dataSetTableRequest);
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "sql")) {
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            datasourceRequest.setQuery(new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class).getSql());
            ResultSet dataResultSet = datasourceProvider.getDataResultSet(datasourceRequest);
            fields = datasourceProvider.fetchResultField(dataResultSet);
        }
        if (CollectionUtils.isNotEmpty(fields)) {
            for (int i = 0; i < fields.size(); i++) {
                TableFiled filed = fields.get(i);
                DatasetTableField datasetTableField = DatasetTableField.builder().build();
                datasetTableField.setTableId(datasetTable.getId());
                datasetTableField.setOriginName(filed.getFieldName());
                datasetTableField.setName(filed.getRemarks());
                datasetTableField.setType(filed.getFieldType());
                datasetTableField.setDeType(transFieldType(ds.getType(), filed.getFieldType()));
                datasetTableField.setChecked(true);
                datasetTableField.setColumnIndex(i);
                datasetTableField.setLastSyncTime(syncTime);
                dataSetTableFieldsService.save(datasetTableField);
            }
        }
    }

    public String createQuerySQL(String type, String table, String[] fields) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(type);
        switch (datasourceType) {
            case mysql:
                return MessageFormat.format("SELECT {0} FROM {1}", StringUtils.join(fields, ","), table);
            case sqlServer:
                return MessageFormat.format("SELECT {0} FROM {1}", StringUtils.join(fields, ","), table);
            default:
                return MessageFormat.format("SELECT {0} FROM {1}", StringUtils.join(fields, ","), table);
        }
    }

    public Integer transFieldType(String type, String field) {
        DatasourceTypes datasourceType = DatasourceTypes.valueOf(type);
        switch (datasourceType) {
            case mysql:
                return transMysqlField(field);
            case sqlServer:
            default:
                return 0;
        }
    }

    public Integer transMysqlField(String field) {
        switch (field) {
            case "CHAR":
            case "VARCHAR":
            case "TEXT":
            case "TINYTEXT":
            case "MEDIUMTEXT":
            case "LONGTEXT":
            case "ENUM":
                return 0;// 文本
            case "DATE":
            case "TIME":
            case "YEAR":
            case "DATETIME":
            case "TIMESTAMP":
                return 1;// 时间
            case "INT":
            case "BIT":
            case "TINYINT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "INTEGER":
            case "BIGINT":
            case "FLOAT":
            case "DOUBLE":
            case "DECIMAL":
                return 2;// 数值
            default:
                return 0;
        }
    }
}
