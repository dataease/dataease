package io.dataease.service.dataset;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.util.UuidUtils;
import com.google.gson.Gson;
import io.dataease.base.domain.DatasetTable;
import io.dataease.base.domain.DatasetTableExample;
import io.dataease.base.domain.Datasource;
import io.dataease.base.mapper.DatasetTableMapper;
import io.dataease.base.mapper.DatasourceMapper;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.datasource.dto.TableFiled;
import io.dataease.datasource.provider.DatasourceProvider;
import io.dataease.datasource.provider.ProviderFactory;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.dto.dataset.DataTableInfoDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public void batchInsert(List<DatasetTable> datasetTable) {
        for (DatasetTable table : datasetTable) {
            save(table);
        }
    }

    public DatasetTable save(DatasetTable datasetTable) {
        if (StringUtils.isEmpty(datasetTable.getId())) {
            datasetTable.setId(UuidUtils.generateUuid());
            datasetTable.setCreateTime(System.currentTimeMillis());
            DataTableInfoDTO dataTableInfoDTO = new DataTableInfoDTO();
            if (StringUtils.equalsIgnoreCase("db", datasetTable.getType())) {
                dataTableInfoDTO.setTable(datasetTable.getName());
            }
            datasetTable.setInfo(new Gson().toJson(dataTableInfoDTO));
            datasetTableMapper.insert(datasetTable);
        } else {
            datasetTableMapper.updateByPrimaryKeyWithBLOBs(datasetTable);
        }
        return datasetTable;
    }

    public void delete(String id) {
        datasetTableMapper.deleteByPrimaryKey(id);
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
        datasourceRequest.setQuery("SELECT * FROM " + table + ";");
        return datasourceProvider.getData(datasourceRequest);
    }

    public Map<String, Object> getPreviewData(DataSetTableRequest dataSetTableRequest) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        String table = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class).getTable();
        datasourceRequest.setTable(table);
        datasourceRequest.setQuery("SELECT * FROM " + table + " LIMIT 0,10;");

        List<TableFiled> fields = datasourceProvider.getTableFileds(datasourceRequest);
        List<String[]> data = datasourceProvider.getData(datasourceRequest);

        JSONArray jsonArray = new JSONArray();
        data.forEach(ele -> {
            JSONObject jsonObject = new JSONObject();
            for (int i = 0; i < ele.length; i++) {
                jsonObject.put(fields.get(i).getFieldName(), ele[i]);
            }
            jsonArray.add(jsonObject);
        });

        Map<String, Object> map = new HashMap<>();
        map.put("fields",fields);
        map.put("data",jsonArray);

        return map;
    }
}
