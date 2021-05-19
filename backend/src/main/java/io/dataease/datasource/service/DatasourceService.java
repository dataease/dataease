package io.dataease.datasource.service;

import com.google.gson.Gson;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.*;
import io.dataease.base.mapper.ext.ExtDataSourceMapper;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.request.DatasourceUnionRequest;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.base.ConditionEntity;
import io.dataease.datasource.dto.DBTableDTO;
import io.dataease.datasource.provider.DatasourceProvider;
import io.dataease.datasource.provider.ProviderFactory;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.dto.DatasourceDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.service.dataset.DataSetGroupService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class DatasourceService {

    @Resource
    private DatasourceMapper datasourceMapper;
    @Resource
    private ExtDataSourceMapper extDataSourceMapper;
    @Resource
    private DatasetTableMapper datasetTableMapper;
    @Resource
    private DataSetGroupService dataSetGroupService;

    public Datasource addDatasource(Datasource datasource) {
        DatasourceExample example = new DatasourceExample();
        example.createCriteria().andNameEqualTo(datasource.getName());
        if (CollectionUtils.isNotEmpty(datasourceMapper.selectByExample(example))) {
            DEException.throwException("Exist data connection with the same name ");
        }
        long currentTimeMillis = System.currentTimeMillis();
        datasource.setId(UUID.randomUUID().toString());
        datasource.setUpdateTime(currentTimeMillis);
        datasource.setCreateTime(currentTimeMillis);
        datasource.setCreateBy(String.valueOf(AuthUtils.getUser().getUsername()));
        datasourceMapper.insertSelective(datasource);
        return datasource;
    }

    public List<DatasourceDTO> getDatasourceList(DatasourceUnionRequest request) throws Exception {
        request.setSort("update_time desc");
        return extDataSourceMapper.queryUnion(request);
    }

    public List<DatasourceDTO> gridQuery(BaseGridRequest request) {
        //如果没有查询条件增加一个默认的条件
        if(CollectionUtils.isEmpty(request.getConditions())){
            ConditionEntity conditionEntity = new ConditionEntity();
            conditionEntity.setField("1");
            conditionEntity.setOperator("eq");
            conditionEntity.setValue("1");
            request.setConditions(Arrays.asList(conditionEntity));
        }
        GridExample gridExample = request.convertExample();
        gridExample.setExtendCondition(String.valueOf(AuthUtils.getUser().getUserId()));
        return extDataSourceMapper.query(gridExample);
    }

    public void deleteDatasource(String datasourceId) {
        datasourceMapper.deleteByPrimaryKey(datasourceId);
    }

    public void updateDatasource(Datasource datasource) {
        datasource.setCreateTime(null);
        datasource.setUpdateTime(System.currentTimeMillis());
        datasourceMapper.updateByPrimaryKeySelective(datasource);
    }

    public void validate(Datasource datasource) throws Exception {
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(datasource);
        datasourceProvider.test(datasourceRequest);
    }

    public List<DBTableDTO> getTables(Datasource datasource) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasource.getId());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        List<String> tables = datasourceProvider.getTables(datasourceRequest);

        // 获取当前数据源下的db类型数据集
        DatasetTableExample datasetTableExample = new DatasetTableExample();
        datasetTableExample.createCriteria().andTypeEqualTo("db").andDataSourceIdEqualTo(datasource.getId());
        List<DatasetTable> datasetTables = datasetTableMapper.selectByExampleWithBLOBs(datasetTableExample);
        List<DBTableDTO> list = new ArrayList<>();
        for (String name : tables) {
            DBTableDTO dbTableDTO = new DBTableDTO();
            dbTableDTO.setDatasourceId(datasource.getId());
            dbTableDTO.setName(name);
            dbTableDTO.setEnableCheck(true);
            dbTableDTO.setDatasetPath(null);
            for (DatasetTable datasetTable : datasetTables) {
                DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
                if (StringUtils.equals(name, dataTableInfoDTO.getTable())) {
                    dbTableDTO.setEnableCheck(false);

                    List<DatasetGroup> parents = dataSetGroupService.getParents(datasetTable.getSceneId());
                    StringBuilder stringBuilder = new StringBuilder();
                    parents.forEach(ele -> stringBuilder.append(ele.getName()).append("/"));
                    stringBuilder.append(datasetTable.getName());
                    dbTableDTO.setDatasetPath(stringBuilder.toString());
                    break;
                }
            }
            list.add(dbTableDTO);
        }
        return list;
    }

    public Datasource get(String id) {
        return datasourceMapper.selectByPrimaryKey(id);
    }
}
