package io.dataease.service.datasource;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.*;
import io.dataease.base.mapper.ext.ExtDataSourceMapper;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.model.AuthURD;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.CommonThreadPool;
import io.dataease.commons.utils.LogUtil;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.request.DatasourceUnionRequest;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.base.ConditionEntity;
import io.dataease.commons.constants.DatasourceTypes;
import io.dataease.provider.datasource.DatasourceProvider;
import io.dataease.provider.ProviderFactory;
import io.dataease.controller.request.datasource.DatasourceRequest;
import io.dataease.dto.DatasourceDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.dto.datasource.*;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import io.dataease.service.dataset.DataSetGroupService;
import io.dataease.service.message.DeMsgutil;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

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
    @Resource
    private CommonThreadPool commonThreadPool;

    public Datasource addDatasource(Datasource datasource) throws Exception{
        checkName(datasource);
        long currentTimeMillis = System.currentTimeMillis();
        datasource.setId(UUID.randomUUID().toString());
        datasource.setUpdateTime(currentTimeMillis);
        datasource.setCreateTime(currentTimeMillis);
        datasource.setCreateBy(String.valueOf(AuthUtils.getUser().getUsername()));
        checkAndUpdateDatasourceStatus(datasource);
        datasourceMapper.insertSelective(datasource);
        handleConnectionPool(datasource, "add");
        return datasource;
    }

    private void handleConnectionPool(Datasource datasource, String type) {
        commonThreadPool.addTask(() -> {
            try {
                DatasourceProvider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(datasource);
                datasourceProvider.handleDatasource(datasourceRequest, type);
                LogUtil.info("Succsss to {} datasource connection pool: {}", type, datasource.getName());
            } catch (Exception e) {
                LogUtil.error("Failed to handle datasource connection pool: " + datasource.getName(), e);
            }
        });
    }

    public List<DatasourceDTO> getDatasourceList(DatasourceUnionRequest request) throws Exception {
        request.setSort("update_time desc");
        List<DatasourceDTO> datasourceDTOS = extDataSourceMapper.queryUnion(request);
        datasourceDTOS.forEach(datasourceDTO -> {
            DatasourceTypes datasourceType = DatasourceTypes.valueOf(datasourceDTO.getType());
            try{
                switch (datasourceType) {
                    case mysql:
                    case mariadb:
                    case de_doris:
                    case ds_doris:
                        datasourceDTO.setConfiguration(JSONObject.toJSONString(new Gson().fromJson(datasourceDTO.getConfiguration(), MysqlConfiguration.class)) );
                        break;
                    case sqlServer:
                        datasourceDTO.setConfiguration(JSONObject.toJSONString(new Gson().fromJson(datasourceDTO.getConfiguration(), SqlServerConfiguration.class)) );
                        break;
                    case oracle:
                        datasourceDTO.setConfiguration(JSONObject.toJSONString(new Gson().fromJson(datasourceDTO.getConfiguration(), OracleConfiguration.class)) );
                        break;
                    case pg:
                        datasourceDTO.setConfiguration(JSONObject.toJSONString(new Gson().fromJson(datasourceDTO.getConfiguration(), PgConfiguration.class)) );
                        break;
                    case ck:
                        datasourceDTO.setConfiguration(JSONObject.toJSONString(new Gson().fromJson(datasourceDTO.getConfiguration(), CHConfiguration.class)) );
                        break;
                    default:
                        break;
                }
            }catch (Exception ignore){}

        });
        return datasourceDTOS;
    }

    public List<DatasourceDTO> gridQuery(BaseGridRequest request) {
        //如果没有查询条件增加一个默认的条件
        if (CollectionUtils.isEmpty(request.getConditions())) {
            ConditionEntity conditionEntity = new ConditionEntity();
            conditionEntity.setField("1");
            conditionEntity.setOperator("eq");
            conditionEntity.setValue("1");
            request.setConditions(Collections.singletonList(conditionEntity));
        }
        GridExample gridExample = request.convertExample();
        gridExample.setExtendCondition(String.valueOf(AuthUtils.getUser().getUserId()));
        return extDataSourceMapper.query(gridExample);
    }

    public void deleteDatasource(String datasourceId) throws Exception {
        DatasetTableExample example = new DatasetTableExample();
        example.createCriteria().andDataSourceIdEqualTo(datasourceId);
        List<DatasetTable> datasetTables = datasetTableMapper.selectByExample(example);
        if(CollectionUtils.isNotEmpty(datasetTables)){
            DataEaseException.throwException(datasetTables.size() +  Translator.get("i18n_datasource_not_allow_delete_msg"));
        }
        Datasource datasource = datasourceMapper.selectByPrimaryKey(datasourceId);
        datasourceMapper.deleteByPrimaryKey(datasourceId);
        handleConnectionPool(datasource, "delete");
    }

    public void updateDatasource(Datasource datasource) {
        checkName(datasource);
        datasource.setCreateTime(null);
        datasource.setUpdateTime(System.currentTimeMillis());
        checkAndUpdateDatasourceStatus(datasource);
        datasourceMapper.updateByPrimaryKeySelective(datasource);
        handleConnectionPool(datasource, "edit");
    }

    public ResultHolder validate(Datasource datasource) throws Exception {
        try {
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasource);
            datasourceProvider.checkStatus(datasourceRequest);
            return ResultHolder.success("Success");
        }catch (Exception e){
            return ResultHolder.error("Datasource is invalid: " + e.getMessage());
        }

    }

    public ResultHolder validate(String datasourceId) {
        Datasource datasource = datasourceMapper.selectByPrimaryKey(datasourceId);
        if(datasource == null){
            return ResultHolder.error("Can not find datasource: "+ datasourceId);
        }
        try {
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasource);
            datasourceProvider.checkStatus(datasourceRequest);
            datasource.setStatus("Success");
            return ResultHolder.success("Success");
        }catch (Exception e){
            datasource.setStatus("Error");
            return ResultHolder.error("Datasource is invalid: " + e.getMessage());
        }finally {
            datasourceMapper.updateByPrimaryKey(datasource);
        }
    }

    public List<String> getSchema(Datasource datasource) throws Exception {
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(datasource);
        return datasourceProvider.getSchema(datasourceRequest);
    }

    public List<DBTableDTO> getTables(Datasource datasource) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasource.getId());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceProvider.checkStatus(datasourceRequest);
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
                    parents.forEach(ele -> {
                        if (ObjectUtils.isNotEmpty(ele)) {
                            stringBuilder.append(ele.getName()).append("/");
                        }
                    });
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

    public void initAllDataSourceConnectionPool() {
        List<Datasource> datasources = datasourceMapper.selectByExampleWithBLOBs(new DatasourceExample());
        datasources.forEach(datasource -> {
            try {
                handleConnectionPool(datasource, "add");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void checkName(Datasource datasource) {
        DatasourceExample example = new DatasourceExample();
        DatasourceExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(datasource.getName());
        if (StringUtils.isNotEmpty(datasource.getId())) {
            criteria.andIdNotEqualTo(datasource.getId());
        }
        if (CollectionUtils.isNotEmpty(datasourceMapper.selectByExample(example))) {
            DEException.throwException(Translator.get("i18n_ds_name_exists"));
        }
    }

    public void updateDatasourceStatus(){
        List<Datasource> datasources = datasourceMapper.selectByExampleWithBLOBs(new DatasourceExample());
        datasources.forEach(datasource -> checkAndUpdateDatasourceStatus(datasource, true));
    }

    private void checkAndUpdateDatasourceStatus(Datasource datasource){
        try {
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasource);
            datasourceProvider.checkStatus(datasourceRequest);
            datasource.setStatus("Success");
        } catch (Exception e) {
            datasource.setStatus("Error");
        }
    }

    private void checkAndUpdateDatasourceStatus(Datasource datasource, Boolean withMsg){
        try {
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasource);
            datasourceProvider.checkStatus(datasourceRequest);
            datasource.setStatus("Success");
            datasourceMapper.updateByPrimaryKeySelective(datasource);
        } catch (Exception e) {
            Datasource temp = datasourceMapper.selectByPrimaryKey(datasource.getId());
            datasource.setStatus("Error");
            if (!StringUtils.equals(temp.getStatus(), "Error")) {
                sendWebMsg(datasource);
                datasourceMapper.updateByPrimaryKeySelective(datasource);
            }            
            
        }
    }

    private void sendWebMsg(Datasource datasource) {
        String id = datasource.getId();
        AuthURD authURD = AuthUtils.authURDR(id);
        Set<Long> userIds = AuthUtils.userIdsByURD(authURD);
        Long typeId = 8L;// 代表数据源失效
        Gson gson = new Gson();
        userIds.forEach(userId -> {
            Map<String, Object> param = new HashMap<>();
            param.put("id", id);
            param.put("name", datasource.getName());
            String content = "数据源【" + datasource.getName() + "】无效";
            DeMsgutil.sendMsg(userId, typeId,  content, gson.toJson(param));
        });
    }
}
