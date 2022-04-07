package io.dataease.service.datasource;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import io.dataease.auth.annotation.DeCleaner;
import io.dataease.base.domain.*;
import io.dataease.base.mapper.DatasetTableMapper;
import io.dataease.base.mapper.DatasourceMapper;
import io.dataease.base.mapper.ext.ExtDataSourceMapper;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.SysAuthConstants;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.model.AuthURD;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.CommonThreadPool;
import io.dataease.commons.utils.LogUtil;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.datasource.request.UpdataDsRequest;
import io.dataease.controller.request.DatasourceUnionRequest;
import io.dataease.controller.request.datasource.ApiDefinition;
import io.dataease.controller.request.datasource.DatasourceRequest;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.base.ConditionEntity;
import io.dataease.dto.DatasourceDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.dto.datasource.*;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.constants.DatasourceTypes;
import io.dataease.provider.ProviderFactory;
import io.dataease.provider.datasource.ApiProvider;
import io.dataease.provider.datasource.DatasourceProvider;
import io.dataease.service.dataset.DataSetGroupService;
import io.dataease.service.message.DeMsgutil;
import io.dataease.service.sys.SysAuthService;
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
    @Resource
    private SysAuthService sysAuthService;
    private static List<String> dsTypes = Arrays.asList("TiDB", "StarRocks", "excel", "mysql", "hive", "impala", "mariadb", "ds_doris", "pg", "sqlServer", "oracle", "mongo", "ck", "db2", "es", "redshift", "api");

    @DeCleaner(DePermissionType.DATASOURCE)
    public Datasource addDatasource(Datasource datasource) throws Exception{
        if(!dsTypes.contains(datasource.getType())){
            throw new Exception("Datasource type not supported.");
        }
        checkName(datasource.getName(),datasource.getType(), datasource.getId());
        long currentTimeMillis = System.currentTimeMillis();
        datasource.setId(UUID.randomUUID().toString());
        datasource.setUpdateTime(currentTimeMillis);
        datasource.setCreateTime(currentTimeMillis);
        datasource.setCreateBy(String.valueOf(AuthUtils.getUser().getUsername()));
        checkAndUpdateDatasourceStatus(datasource);
        datasourceMapper.insertSelective(datasource);
        handleConnectionPool(datasource, "add");
        sysAuthService.copyAuth(datasource.getId(), SysAuthConstants.AUTH_SOURCE_TYPE_DATASOURCE);
        return datasource;
    }

    public void handleConnectionPool(Datasource datasource, String type) {
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
                    case engine_doris:
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
                    case api:
                        JSONArray apiDefinitionList = JSONObject.parseArray(datasourceDTO.getConfiguration());
                        JSONArray apiDefinitionListWithStatus = new JSONArray();
                        int success = 0;
                        if(StringUtils.isNotEmpty(datasourceDTO.getStatus())){
                            JSONObject apiItemStatuses = JSONObject.parseObject(datasourceDTO.getStatus());
                            for (Object apiDefinition : apiDefinitionList) {
                                String status = apiItemStatuses.getString(JSONObject.parseObject(apiDefinition.toString()).getString("name") );
                                JSONObject object = JSONObject.parseObject(apiDefinition.toString());
                                object.put("status", status);
                                apiDefinitionListWithStatus.add(object);
                                if(StringUtils.isNotEmpty(status) && status.equalsIgnoreCase("Success")){
                                    success ++;
                                }
                            }
                        }
                        datasourceDTO.setApiConfiguration(apiDefinitionListWithStatus);
                       if(success == apiDefinitionList.size()){
                           datasourceDTO.setStatus("Success");
                           break;
                       }
                        if(success > 0 && success < apiDefinitionList.size() ){
                            datasourceDTO.setStatus("Warning");
                            break;
                        }
                        datasourceDTO.setStatus("Error");
                        break;
                    default:
                        break;
                }
            }catch (Exception ignore){
                ignore.printStackTrace();
            }

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

    @DeCleaner(DePermissionType.DATASOURCE)
    public ResultHolder deleteDatasource(String datasourceId) throws Exception {
        DatasetTableExample example = new DatasetTableExample();
        example.createCriteria().andDataSourceIdEqualTo(datasourceId);
        List<DatasetTable> datasetTables = datasetTableMapper.selectByExample(example);
        if(CollectionUtils.isNotEmpty(datasetTables)){
            return ResultHolder.error(datasetTables.size() +  Translator.get("i18n_datasource_not_allow_delete_msg"));
        }
        Datasource datasource = datasourceMapper.selectByPrimaryKey(datasourceId);
        datasourceMapper.deleteByPrimaryKey(datasourceId);
        handleConnectionPool(datasource, "delete");
        return ResultHolder.success("success");
    }

    public void updateDatasource(UpdataDsRequest updataDsRequest)throws Exception{
        if(!dsTypes.contains(updataDsRequest.getType())){
            throw new Exception("Datasource type not supported.");
        }
        checkName(updataDsRequest.getName(),updataDsRequest.getType(),updataDsRequest.getId());
        Datasource datasource = new Datasource();
        datasource.setName(updataDsRequest.getName());
        datasource.setDesc(updataDsRequest.getDesc());
        datasource.setConfiguration(updataDsRequest.getConfiguration());
        datasource.setCreateTime(null);
        datasource.setType(updataDsRequest.getType());
        datasource.setUpdateTime(System.currentTimeMillis());
        checkAndUpdateDatasourceStatus(datasource);
        DatasourceExample example = new DatasourceExample();
        example.createCriteria().andIdEqualTo(updataDsRequest.getId());
        datasourceMapper.updateByExampleSelective(datasource, example);
        handleConnectionPool(datasource, "edit");
    }

    public ResultHolder validate(DatasourceDTO datasource) throws Exception {
        try {
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasource);
            String datasourceStatus = datasourceProvider.checkStatus(datasourceRequest);
            if(datasource.getType().equalsIgnoreCase("api")){
                int success = 0;
                JSONArray apiDefinitionList = JSONObject.parseArray(datasource.getConfiguration());
                JSONArray apiDefinitionListWithStatus = new JSONArray();
                if(StringUtils.isNotEmpty(datasourceStatus)){
                    JSONObject apiItemStatuses = JSONObject.parseObject(datasourceStatus);
                    for (Object apiDefinition : apiDefinitionList) {
                        String status = apiItemStatuses.getString(JSONObject.parseObject(apiDefinition.toString()).getString("name") );
                        JSONObject object = JSONObject.parseObject(apiDefinition.toString());
                        object.put("status", status);
                        apiDefinitionListWithStatus.add(object);
                        if(StringUtils.isNotEmpty(status) && status.equalsIgnoreCase("Success")){
                            success ++;
                        }
                    }
                }

                datasource.setApiConfiguration(apiDefinitionListWithStatus);
                if(success == apiDefinitionList.size()){
                    return ResultHolder.success(datasource);
                }
                if(success > 0 && success < apiDefinitionList.size() ){
                    return ResultHolder.error("Datasource has invalid tables", datasource);
                }
                return ResultHolder.error("Datasource is invalid.", datasource);
            }
            return ResultHolder.success(datasource);
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
            String datasourceStatus = datasourceProvider.checkStatus(datasourceRequest);
            datasource.setStatus(datasourceStatus);

            if(datasource.getType().equalsIgnoreCase("api")){
                List<ApiDefinition> apiDefinitionList = JSONObject.parseArray(datasource.getConfiguration(), ApiDefinition.class);
                JSONObject apiItemStatuses = JSONObject.parseObject(datasourceStatus);
                int success = 0;
                for (ApiDefinition apiDefinition : apiDefinitionList) {
                    String status = apiItemStatuses.getString(apiDefinition.getName());
                    apiDefinition.setStatus(status);
                    if(status.equalsIgnoreCase("Success")){
                        success ++;
                    }
                }
                if(success == apiDefinitionList.size()){
                    return ResultHolder.success(datasource);
                }
                if(success > 0 && success < apiDefinitionList.size() ){
                    return ResultHolder.error("Datasource has invalid tables", datasource);
                }
                return ResultHolder.error("Datasource is invalid.", datasource);
            }

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

    public List<DBTableDTO> getTables(String id) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(id);
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        if(!ds.getType().equalsIgnoreCase("api")){
            datasourceProvider.checkStatus(datasourceRequest);
        }

        List<TableDesc> tables = datasourceProvider.getTables(datasourceRequest);

        // 获取当前数据源下的db、api类型数据集
        DatasetTableExample datasetTableExample = new DatasetTableExample();
        datasetTableExample.createCriteria().andTypeIn(Arrays.asList("db","api")).andDataSourceIdEqualTo(ds.getId());
        List<DatasetTable> datasetTables = datasetTableMapper.selectByExampleWithBLOBs(datasetTableExample);
        List<DBTableDTO> list = new ArrayList<>();
        for (TableDesc tableDesc : tables) {
            DBTableDTO dbTableDTO = new DBTableDTO();
            dbTableDTO.setDatasourceId(ds.getId());
            dbTableDTO.setName(tableDesc.getName());
            dbTableDTO.setRemark(tableDesc.getRemark());
            dbTableDTO.setEnableCheck(true);
            dbTableDTO.setDatasetPath(null);
            for (DatasetTable datasetTable : datasetTables) {
                DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
                if (StringUtils.equals(tableDesc.getName(), dataTableInfoDTO.getTable())) {
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
            commonThreadPool.addTask(()->{
                try {
                    handleConnectionPool(datasource, "add");
                } catch (Exception e) {
                    LogUtil.error("Failed to init datasource: " + datasource.getName(), e);
                }
            });
        });
    }

    private void checkName(String datasourceName, String type, String id) {
        DatasourceExample example = new DatasourceExample();
        DatasourceExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(datasourceName);
        criteria.andTypeEqualTo(type);
        if (StringUtils.isNotEmpty(id)) {
            criteria.andIdNotEqualTo(id);
        }
        if (CollectionUtils.isNotEmpty(datasourceMapper.selectByExample(example))) {
            DEException.throwException(Translator.get("i18n_ds_name_exists"));
        }
    }

    public void updateDatasourceStatus(){
        List<Datasource> datasources = datasourceMapper.selectByExampleWithBLOBs(new DatasourceExample());
        datasources.forEach(datasource -> checkAndUpdateDatasourceStatus(datasource, true));
    }

    public ApiDefinition checkApiDatasource(ApiDefinition apiDefinition) throws Exception {
        String response = ApiProvider.execHttpRequest(apiDefinition);
        return ApiProvider.checkApiDefinition(apiDefinition, response);
    }

    private void checkAndUpdateDatasourceStatus(Datasource datasource){
        try {
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasource);
            String status = datasourceProvider.checkStatus(datasourceRequest);
            datasource.setStatus(status);
        } catch (Exception e) {
            datasource.setStatus("Error");
        }
    }

    private void checkAndUpdateDatasourceStatus(Datasource datasource, Boolean withMsg){
        try {
            DatasourceProvider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasource);
            String status = datasourceProvider.checkStatus(datasourceRequest);
            datasource.setStatus(status);
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
