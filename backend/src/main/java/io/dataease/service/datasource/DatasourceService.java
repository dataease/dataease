package io.dataease.service.datasource;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.dataease.auth.annotation.DeCleaner;
import io.dataease.commons.constants.RedisConstants;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.sys.response.BasicInfo;
import io.dataease.dto.TaskInstance;
import io.dataease.ext.ExtDataSourceMapper;
import io.dataease.ext.ExtTaskInstanceMapper;
import io.dataease.ext.UtilMapper;
import io.dataease.ext.query.GridExample;
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
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.base.ConditionEntity;
import io.dataease.dto.DatasourceDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.dto.datasource.*;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.DatasetTableMapper;
import io.dataease.plugins.common.base.mapper.DatasourceMapper;
import io.dataease.plugins.common.base.mapper.QrtzSchedulerStateMapper;
import io.dataease.plugins.common.constants.DatasetType;
import io.dataease.plugins.common.constants.DatasourceCalculationMode;
import io.dataease.plugins.common.constants.DatasourceTypes;
import io.dataease.plugins.common.dto.datasource.DataSourceType;
import io.dataease.plugins.common.dto.datasource.TableDesc;
import io.dataease.plugins.common.entity.GlobalTaskEntity;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import io.dataease.plugins.datasource.provider.Provider;
import io.dataease.provider.ProviderFactory;
import io.dataease.provider.datasource.ApiProvider;
import io.dataease.service.ScheduleService;
import io.dataease.service.dataset.DataSetGroupService;
import io.dataease.service.message.DeMsgutil;
import io.dataease.service.sys.SysAuthService;
import io.dataease.service.system.SystemParameterService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
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
    @Resource
    private Environment env;
    @Resource
    private SystemParameterService systemParameterService;
    @Autowired
    private ScheduleService scheduleService;
    @Resource
    private QrtzSchedulerStateMapper qrtzSchedulerStateMapper;
    @Resource
    private UtilMapper utilMapper;
    @Resource
    private ExtTaskInstanceMapper extTaskInstanceMapper;

    public Collection<DataSourceType> types() {
        Collection<DataSourceType> types = new ArrayList<>();
        types.addAll(SpringContextUtil.getApplicationContext().getBeansOfType(DataSourceType.class).values());
        SpringContextUtil.getApplicationContext().getBeansOfType(io.dataease.plugins.datasource.service.DatasourceService.class).values().forEach(datasourceService -> {
            types.add(datasourceService.getDataSourceType());
        });
        return types;
    }

    @DeCleaner(DePermissionType.DATASOURCE)
    @Transactional(rollbackFor = Exception.class)
    public Datasource addDatasource(Datasource datasource) throws Exception {
        if (!types().stream().map(DataSourceType::getType).collect(Collectors.toList()).contains(datasource.getType())) {
            throw new Exception("Datasource type not supported.");
        }

        Provider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
        datasourceProvider.checkConfiguration(datasource);

        checkName(datasource.getName(), datasource.getType(), datasource.getId());
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


    public void handleConnectionPool(String datasourceId, String type) {
        Datasource datasource = datasourceMapper.selectByPrimaryKey(datasourceId);
        if (datasource == null) {
            return;
        }
        handleConnectionPool(datasource, type);
    }

    public void handleConnectionPool(Datasource datasource, String type) {
        commonThreadPool.addTask(() -> {
            try {
                Provider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(datasource);
                datasourceProvider.handleDatasource(datasourceRequest, type);
                LogUtil.info("Success to {} datasource connection pool: {}", type, datasource.getName());
            } catch (Exception e) {
                LogUtil.error("Failed to handle datasource connection pool: " + datasource.getName(), e);
            }
        });
    }

    public List<DatasourceDTO> getDatasourceList(DatasourceUnionRequest request) throws Exception {
        request.setSort("update_time desc");
        List<DatasourceDTO> datasourceDTOS = extDataSourceMapper.queryUnion(request);
        datasourceDTOS.forEach(datasourceDTO -> {
            types().forEach(dataSourceType -> {
                if (dataSourceType.getType().equalsIgnoreCase(datasourceDTO.getType())) {
                    datasourceDTO.setTypeDesc(dataSourceType.getName());
                    datasourceDTO.setCalculationMode(dataSourceType.getCalculationMode());
                }
            });
            if (!datasourceDTO.getType().equalsIgnoreCase(DatasourceTypes.api.toString())) {
                JdbcConfiguration configuration = new Gson().fromJson(datasourceDTO.getConfiguration(), JdbcConfiguration.class);
                if (StringUtils.isNotEmpty(configuration.getCustomDriver()) && !configuration.getCustomDriver().equalsIgnoreCase("default")) {
                    datasourceDTO.setCalculationMode(DatasourceCalculationMode.DIRECT);
                }
                JSONObject jsonObject = JSONObject.parseObject(datasourceDTO.getConfiguration());
                if (jsonObject.getString("queryTimeout") == null) {
                    jsonObject.put("queryTimeout", 30);
                    datasourceDTO.setConfiguration(jsonObject.toString());
                }
            }

            if (datasourceDTO.getType().equalsIgnoreCase(DatasourceTypes.mysql.toString())) {
                MysqlConfiguration mysqlConfiguration = new Gson().fromJson(datasourceDTO.getConfiguration(), MysqlConfiguration.class);
                datasourceDTO.setConfiguration(new Gson().toJson(mysqlConfiguration));
            }
            if (datasourceDTO.getType().equalsIgnoreCase(DatasourceTypes.api.toString())) {
                List<ApiDefinition> apiDefinitionList = new Gson().fromJson(datasourceDTO.getConfiguration(), new TypeToken<ArrayList<ApiDefinition>>() {
                }.getType());
                List<ApiDefinition> apiDefinitionListWithStatus = new ArrayList<>();
                int success = 0;
                if (StringUtils.isNotEmpty(datasourceDTO.getStatus())) {
                    JsonObject apiItemStatuses = JsonParser.parseString(datasourceDTO.getStatus()).getAsJsonObject();

                    for (int i = 0; i < apiDefinitionList.size(); i++) {
                        String status = null;
                        if (apiItemStatuses.get(apiDefinitionList.get(i).getName()) != null) {
                            status = apiItemStatuses.get(apiDefinitionList.get(i).getName()).getAsString();
                        }
                        apiDefinitionList.get(i).setStatus(status);
                        apiDefinitionList.get(i).setSerialNumber(i);
                        apiDefinitionListWithStatus.add(apiDefinitionList.get(i));
                        if (StringUtils.isNotEmpty(status) && status.equalsIgnoreCase("Success")) {
                            success++;
                        }
                    }
                }
                datasourceDTO.setApiConfiguration(apiDefinitionListWithStatus);
                if (success == apiDefinitionList.size()) {
                    datasourceDTO.setStatus("Success");
                } else {
                    if (success > 0 && success < apiDefinitionList.size()) {
                        datasourceDTO.setStatus("Warning");
                    } else {
                        datasourceDTO.setStatus("Error");
                    }
                }
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
        if (CollectionUtils.isNotEmpty(datasetTables)) {
            return ResultHolder.error(datasetTables.size() + Translator.get("i18n_datasource_not_allow_delete_msg"));
        }
        Datasource datasource = datasourceMapper.selectByPrimaryKey(datasourceId);
        datasourceMapper.deleteByPrimaryKey(datasourceId);
        handleConnectionPool(datasource, "delete");
        return ResultHolder.success("success");
    }

    public void updateDatasource(UpdataDsRequest updataDsRequest) throws Exception {
        if (!types().stream().map(DataSourceType::getType).collect(Collectors.toList()).contains(updataDsRequest.getType())) {
            throw new Exception("Datasource type not supported.");
        }
        checkName(updataDsRequest.getName(), updataDsRequest.getType(), updataDsRequest.getId());
        Datasource datasource = new Datasource();
        datasource.setName(updataDsRequest.getName());
        datasource.setDesc(updataDsRequest.getDesc());
        datasource.setConfiguration(updataDsRequest.getConfiguration());
        datasource.setCreateTime(null);
        datasource.setType(updataDsRequest.getType());
        datasource.setUpdateTime(System.currentTimeMillis());

        Provider datasourceProvider = ProviderFactory.getProvider(updataDsRequest.getType());
        datasourceProvider.checkConfiguration(datasource);

        checkAndUpdateDatasourceStatus(datasource);
        DatasourceExample example = new DatasourceExample();
        example.createCriteria().andIdEqualTo(updataDsRequest.getId());
        datasourceMapper.updateByExampleSelective(datasource, example);
        handleConnectionPool(updataDsRequest.getId());
    }

    private void handleConnectionPool(String datasourceId) {
        String cacheType = env.getProperty("spring.cache.type");
        if (cacheType != null && cacheType.equalsIgnoreCase("redis")) {
            handleConnectionPool(datasourceId, "delete");
            RedisTemplate redisTemplate = SpringContextUtil.getBean("redisTemplate", RedisTemplate.class);
            redisTemplate.convertAndSend(RedisConstants.DS_REDIS_TOPIC, datasourceId);
        } else {
            handleConnectionPool(datasourceId, "edit");
        }
    }

    public ResultHolder validate(Datasource datasource) throws Exception {
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, datasource);
        try {
            Provider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
            datasourceProvider.checkConfiguration(datasource);
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasource);
            String datasourceStatus = datasourceProvider.checkStatus(datasourceRequest);
            if (datasource.getType().equalsIgnoreCase("api")) {
                int success = 0;
                List<ApiDefinition> apiDefinitionList = new Gson().fromJson(datasource.getConfiguration(), new TypeToken<List<ApiDefinition>>() {
                }.getType());
                List<ApiDefinition> apiDefinitionListWithStatus = new ArrayList<>();

                if (StringUtils.isNotEmpty(datasourceStatus)) {
                    JsonObject apiItemStatuses = JsonParser.parseString(datasourceStatus).getAsJsonObject();
                    for (ApiDefinition apiDefinition : apiDefinitionList) {
                        String status = apiItemStatuses.get(apiDefinition.getName()).getAsString();
                        apiDefinition.setStatus(status);
                        apiDefinitionListWithStatus.add(apiDefinition);
                        if (StringUtils.isNotEmpty(status) && status.equalsIgnoreCase("Success")) {
                            success++;
                        }
                    }
                }

                datasourceDTO.setApiConfiguration(apiDefinitionListWithStatus);
                if (success == apiDefinitionList.size()) {
                    return ResultHolder.success(datasourceDTO);
                }
                if (success > 0 && success < apiDefinitionList.size()) {
                    return ResultHolder.error(Translator.get("I18N_DS_INVALID_TABLE"), datasourceDTO);
                }
                return ResultHolder.error(Translator.get("I18N_DS_INVALID"), datasourceDTO);
            }
            return ResultHolder.success(datasourceDTO);
        } catch (Exception e) {
            return ResultHolder.error(Translator.get("I18N_DS_INVALID") + ": " + e.getMessage());
        }
    }

    public ResultHolder validate(String datasourceId) {
        Datasource datasource = datasourceMapper.selectByPrimaryKey(datasourceId);
        if (datasource == null) {
            return ResultHolder.error("Can not find datasource: " + datasourceId);
        }
        String datasourceStatus = null;
        try {
            Provider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasource);
            datasourceStatus = datasourceProvider.checkStatus(datasourceRequest);

            if (datasource.getType().equalsIgnoreCase("api")) {
                List<ApiDefinition> apiDefinitionList = new Gson().fromJson(datasource.getConfiguration(), new TypeToken<List<ApiDefinition>>() {
                }.getType());
                JsonObject apiItemStatuses = JsonParser.parseString(datasourceStatus).getAsJsonObject();
                int success = 0;
                for (ApiDefinition apiDefinition : apiDefinitionList) {
                    String status = apiItemStatuses.get(apiDefinition.getName()).getAsString();
                    apiDefinition.setStatus(status);
                    if (status.equalsIgnoreCase("Success")) {
                        success++;
                    }
                }
                if (success == apiDefinitionList.size()) {
                    return ResultHolder.success(datasource);
                }
                if (success > 0 && success < apiDefinitionList.size()) {
                    return ResultHolder.error(Translator.get("I18N_DS_INVALID_TABLE"), datasource);
                }
                return ResultHolder.error(Translator.get("I18N_DS_INVALID"), datasource);
            }

            return ResultHolder.success("Success");
        } catch (Exception e) {
            datasourceStatus = "Error";
            return ResultHolder.error(Translator.get("I18N_DS_INVALID") + ": " + e.getMessage());
        } finally {
            Datasource record = new Datasource();
            record.setStatus(datasourceStatus);
            DatasourceExample example = new DatasourceExample();
            example.createCriteria().andIdEqualTo(datasource.getId());
            datasourceMapper.updateByExampleSelective(record, example);
        }
    }

    public List<String> getSchema(Datasource datasource) throws Exception {
        Provider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(datasource);
        return datasourceProvider.getSchema(datasourceRequest);
    }

    public List<DBTableDTO> getTables(String id) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(id);
        Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        if (!ds.getType().equalsIgnoreCase(DatasetType.API.name())) {
            datasourceProvider.checkStatus(datasourceRequest);
        }

        List<TableDesc> tables = datasourceProvider.getTables(datasourceRequest);

        // 获取当前数据源下的db、api类型数据集
        DatasetTableExample datasetTableExample = new DatasetTableExample();
        datasetTableExample.createCriteria().andTypeIn(Arrays.asList(DatasetType.DB.name(), DatasetType.API.name())).andDataSourceIdEqualTo(ds.getId());
        List<DatasetTable> datasetTables = datasetTableMapper.selectByExample(datasetTableExample);
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

    public List<Datasource> selectByType(String type) {
        DatasourceExample datasourceExample = new DatasourceExample();
        datasourceExample.createCriteria().andTypeEqualTo(type);
        return datasourceMapper.selectByExampleWithBLOBs(datasourceExample);
    }

    public void initAllDataSourceConnectionPool() {
        List<Datasource> datasources = datasourceMapper.selectByExampleWithBLOBs(new DatasourceExample());
        datasources.forEach(datasource -> {
            commonThreadPool.addTask(() -> {
                try {
                    handleConnectionPool(datasource, "add");
                } catch (Exception e) {
                    LogUtil.error("Failed to init datasource: " + datasource.getName(), e);
                }
            });
        });
    }

    public void checkName(String datasourceName, String type, String id) {
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

    public void checkDatasourceJob() {
        List<QrtzSchedulerState> qrtzSchedulerStates = qrtzSchedulerStateMapper.selectByExample(null);
        List<String> activeQrtzInstances = qrtzSchedulerStates.stream()
                .filter(qrtzSchedulerState -> qrtzSchedulerState.getLastCheckinTime()
                        + qrtzSchedulerState.getCheckinInterval() + 1000 > utilMapper.currentTimestamp())
                .map(QrtzSchedulerStateKey::getInstanceName).collect(Collectors.toList());


        List<TaskInstance> taskInstances = extTaskInstanceMapper.select();
        taskInstances.forEach(taskInstance -> {
            if (StringUtils.isNotEmpty(taskInstance.getQrtzInstance()) && !activeQrtzInstances.contains(taskInstance.getQrtzInstance().substring(0, taskInstance.getQrtzInstance().length() - 13))) {
                TaskInstance update = new TaskInstance();
                update.setTaskId("Datasource_check_status");
                extTaskInstanceMapper.update(update);
            }
        });
    }

    public void updateDatasourceStatus() {
        List<Datasource> datasources = datasourceMapper.selectByExampleWithBLOBs(new DatasourceExample());
        datasources.forEach(datasource -> checkAndUpdateDatasourceStatus(datasource, true));
    }

    public ApiDefinition checkApiDatasource(ApiDefinition apiDefinition) throws Exception {
        BasicInfo basicInfo = systemParameterService.basicInfo();
        String response = ApiProvider.execHttpRequest(apiDefinition, StringUtils.isNotBlank(basicInfo.getFrontTimeOut()) ? Integer.parseInt(basicInfo.getFrontTimeOut()) : 10);
        return ApiProvider.checkApiDefinition(apiDefinition, response);
    }

    public List<Datasource> listByType(String type) {
        DatasourceExample example = new DatasourceExample();
        example.createCriteria().andTypeEqualTo(type);
        return datasourceMapper.selectByExampleWithBLOBs(example);
    }

    private void checkAndUpdateDatasourceStatus(Datasource datasource) {
        try {
            Provider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasource);
            String status = datasourceProvider.checkStatus(datasourceRequest);
            datasource.setStatus(status);
        } catch (Exception e) {
            datasource.setStatus("Error");
        }
    }

    private void checkAndUpdateDatasourceStatus(Datasource datasource, Boolean withMsg) {
        Datasource record = new Datasource();
        DatasourceExample example = new DatasourceExample();
        example.createCriteria().andIdEqualTo(datasource.getId());
        try {
            Provider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasource);
            String status = datasourceProvider.checkStatus(datasourceRequest);
            record.setStatus(status);
            datasourceMapper.updateByExampleSelective(record, example);
        } catch (Exception e) {
            Datasource temp = datasourceMapper.selectByPrimaryKey(datasource.getId());
            record.setStatus("Error");
            if (!StringUtils.equals(temp.getStatus(), "Error")) {
                sendWebMsg(datasource);
                datasourceMapper.updateByExampleSelective(record, example);
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
            DeMsgutil.sendMsg(userId, typeId, content, gson.toJson(param));
        });
    }

    public void updateDatasourceStatusJob(BasicInfo basicInfo, List<SystemParameter> parameters) {
        String type = "";
        Integer interval = 30;

        boolean changeDsCheckTime = false;
        basicInfo.getDsCheckInterval();
        basicInfo.getDsCheckIntervalType();
        for (SystemParameter parameter : parameters) {
            if (parameter.getParamKey().equalsIgnoreCase("basic.dsCheckInterval") && !parameter.getParamValue().equalsIgnoreCase(basicInfo.getDsCheckInterval())) {
                changeDsCheckTime = true;
                interval = Integer.valueOf(parameter.getParamValue());
            }
            if (parameter.getParamKey().equalsIgnoreCase("basic.dsCheckIntervalType") && !parameter.getParamValue().equalsIgnoreCase(basicInfo.getDsCheckInterval())) {
                changeDsCheckTime = true;
                type = parameter.getParamValue();
            }
        }
        if (!changeDsCheckTime) {
            return;
        }
        addJob(type, interval);
    }

    private void addJob(String type, Integer interval) {
        String cron = "";
        switch (type) {
            case "hour":
                cron = "0 0 0/hour *  * ? *".replace("hour", interval.toString());
                break;
            default:
                cron = "0 0/minute * *  * ? *".replace("minute", interval.toString());
        }

        GlobalTaskEntity globalTask = new GlobalTaskEntity();
        globalTask.setCron(cron);
        globalTask.setCreateTime(System.currentTimeMillis());
        globalTask.setJobKey("Datasource_check_status");
        globalTask.setTaskName("Datasource check status");
        globalTask.setTaskType("dsTaskHandler");
        globalTask.setStartTime(System.currentTimeMillis());
        try {
            scheduleService.addSchedule(globalTask);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initDsCheckJob() {
        BasicInfo basicInfo = systemParameterService.basicInfo();
        addJob(basicInfo.getDsCheckIntervalType(), Integer.valueOf(basicInfo.getDsCheckInterval()));
    }

}
