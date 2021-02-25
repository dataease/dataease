package io.dataease.service.dataset;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.util.UuidUtils;
import com.google.gson.Gson;
import io.dataease.base.domain.DatasetTable;
import io.dataease.base.domain.DatasetTableExample;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.Datasource;
import io.dataease.base.mapper.DatasetTableMapper;
import io.dataease.base.mapper.DatasourceMapper;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.SessionUtils;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.datasource.constants.DatasourceTypes;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.datasource.provider.DatasourceProvider;
import io.dataease.datasource.provider.ProviderFactory;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.dto.dataset.DataTableInfoDTO;
import jnr.ffi.Struct;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.python.apache.xerces.xs.StringList;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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

    public void batchInsert(List<DatasetTable> datasetTable) throws Exception {
        for (DatasetTable table : datasetTable) {
            save(table);
        }
    }

    public DatasetTable save(DatasetTable datasetTable) throws Exception {
        if (StringUtils.isEmpty(datasetTable.getId())) {
            datasetTable.setId(UuidUtils.generateUuid());
            datasetTable.setCreateTime(System.currentTimeMillis());
            DataTableInfoDTO dataTableInfoDTO = new DataTableInfoDTO();
            if (StringUtils.equalsIgnoreCase("db", datasetTable.getType())) {
                dataTableInfoDTO.setTable(datasetTable.getName());
            }
            datasetTable.setInfo(new Gson().toJson(dataTableInfoDTO));
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
    }

    public List<DatasetTable> list(DataSetTableRequest dataSetTableRequest) {
        DatasetTableExample datasetTableExample = new DatasetTableExample();
        datasetTableExample.createCriteria().andSceneIdEqualTo(dataSetTableRequest.getSceneId());
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

    public List<String[]> getData(DataSetTableRequest dataSetTableRequest) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        String table = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class).getTable();

        DatasetTableField datasetTableField = new DatasetTableField();
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
        String table = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class).getTable();
//        datasourceRequest.setTable(table);

        DatasetTableField datasetTableField = new DatasetTableField();
        datasetTableField.setTableId(dataSetTableRequest.getId());
        datasetTableField.setChecked(Boolean.TRUE);
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);

        String[] fieldArray = fields.stream().map(DatasetTableField::getOriginName).toArray(String[]::new);
//        datasourceRequest.setQuery("SELECT " + StringUtils.join(fieldArray, ",") + " FROM " + table + " LIMIT 0,10;");
        datasourceRequest.setQuery(createQuerySQL(ds.getType(), table, fieldArray) + " LIMIT 0,10");

        List<String[]> data = new ArrayList<>();
        try {
            data.addAll(datasourceProvider.getData(datasourceRequest));
        } catch (Exception e) {
        }

        JSONArray jsonArray = new JSONArray();
        if (CollectionUtils.isNotEmpty(data)) {
            data.forEach(ele -> {
                JSONObject jsonObject = new JSONObject();
                for (int i = 0; i < ele.length; i++) {
                    jsonObject.put(fieldArray[i], ele[i]);
                }
                jsonArray.add(jsonObject);
            });
        }

        Map<String, Object> map = new HashMap<>();
        map.put("fields", fields);
        map.put("data", jsonArray);

        return map;
    }

    public void saveTableField(DatasetTable datasetTable) throws Exception {
        DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
        BeanUtils.copyBean(dataSetTableRequest, datasetTable);
        List<TableFiled> fields = getFields(dataSetTableRequest);
        long syncTime = System.currentTimeMillis();
        if (CollectionUtils.isNotEmpty(fields)) {
            for (int i = 0; i < fields.size(); i++) {
                TableFiled filed = fields.get(i);
                DatasetTableField datasetTableField = new DatasetTableField();
                datasetTableField.setTableId(datasetTable.getId());
                datasetTableField.setOriginName(filed.getFieldName());
                datasetTableField.setName(filed.getRemarks());
                datasetTableField.setType(filed.getFieldType());
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
}
