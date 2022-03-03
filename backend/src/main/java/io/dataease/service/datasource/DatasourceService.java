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
import io.dataease.commons.constants.DatasourceTypes;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.model.AuthURD;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.CommonThreadPool;
import io.dataease.commons.utils.LogUtil;
import io.dataease.controller.ResultHolder;
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
import io.dataease.provider.ProviderFactory;
import io.dataease.provider.datasource.ApiProvider;
import io.dataease.provider.datasource.DatasourceProvider;
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

    @DeCleaner(DePermissionType.DATASOURCE)
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

    public void updateDatasource(Datasource datasource) {
        checkName(datasource);
        datasource.setCreateTime(null);
        datasource.setUpdateTime(System.currentTimeMillis());
        checkAndUpdateDatasourceStatus(datasource);
        datasourceMapper.updateByPrimaryKeySelective(datasource);
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

    public List<DBTableDTO> getTables(Datasource datasource) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasource.getId());
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
                System.out.println(System.currentTimeMillis());
                try {
                    handleConnectionPool(datasource, "add");
                } catch (Exception e) {
                    LogUtil.error("Failed to init datasource: " + datasource.getName(), e);
                }
            });
        });
    }

    private void checkName(Datasource datasource) {
        DatasourceExample example = new DatasourceExample();
        DatasourceExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(datasource.getName());
        criteria.andTypeEqualTo(datasource.getType());
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

    public ApiDefinition checkApiDatasource(ApiDefinition apiDefinition) throws Exception {
        String response = ApiProvider.execHttpRequest(apiDefinition);
        if(StringUtils.isEmpty(response)){
            throw new Exception("该请求返回数据为空");
        }
        List<LinkedHashMap> datas = new ArrayList<>();
        try {
            datas = JsonPath.read(response,apiDefinition.getDataPath());
        }catch (Exception e){
            throw new Exception("jsonPath 路径错误：" + e.getMessage());
        }

        List<JSONObject> dataList = new ArrayList<>();
        List<DatasetTableField> fields = new ArrayList<>();
        Set<String> fieldKeys = new HashSet<>();
        //第一遍获取 field
        for (LinkedHashMap data : datas) {
            Set<String> keys = data.keySet();
            for (String key : keys) {
                if(!fieldKeys.contains(key)){
                    fieldKeys.add(key);
                    DatasetTableField tableField = new DatasetTableField();
                    tableField.setOriginName(key);
                    tableField.setName(key);
                    tableField.setSize(65535);
                    tableField.setDeExtractType(0);
                    tableField.setDeType(0);
                    tableField.setExtField(0);
                    fields.add(tableField);
                }
            }
        }
        //第二遍获取 data
        for (LinkedHashMap data : datas) {
            JSONObject jsonObject = new JSONObject();
            for (String key : fieldKeys) {
                jsonObject.put(key, Optional.ofNullable(data.get(key)).orElse("").toString().replaceAll("\n", " ").replaceAll("\r", " "));
            }
            dataList.add(jsonObject);
        }
        apiDefinition.setDatas(dataList);
        apiDefinition.setFields(fields);
        return apiDefinition;
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
