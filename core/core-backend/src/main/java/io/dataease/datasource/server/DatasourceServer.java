package io.dataease.datasource.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.DatasourceApi;
import io.dataease.api.ds.vo.*;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiResourceCreator;
import io.dataease.api.permissions.auth.dto.BusiResourceEditor;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.dataset.dto.DatasourceSchemaDTO;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTask;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.datasource.manage.DatasourceSyncManage;
import io.dataease.datasource.provider.ApiUtils;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.datasource.provider.ExcelUtils;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.exception.DEException;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/datasource")
@Transactional
public class DatasourceServer implements DatasourceApi {
    @Resource
    private CoreDatasourceMapper datasourceMapper;
    @Resource
    private EngineServer engineServer;
    @Resource
    private Environment env;
    @Resource
    private DatasourceTaskServer datasourceTaskServer;
    @Resource
    private CalciteProvider calciteProvider;
    @Resource
    private DatasourceSyncManage datasourceSyncManage;

    @Autowired(required = false)
    private InteractiveAuthApi interactiveAuthApi;

    private static final String RESOURCE_FLAG = "datasource";

    private static ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public List<DatasourceDTO> query(String keyWord) {
        return null;
    }

    public enum UpdateType {
        all_scope, add_scope
    }
    @Override
    public DatasourceDTO save(DatasourceDTO dataSourceDTO) throws Exception {
        if (dataSourceDTO.getId() != null && dataSourceDTO.getId() > 0) {
            return update(dataSourceDTO);
        }
        if (StringUtils.isNotEmpty(dataSourceDTO.getConfiguration())) {
            dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        }
        preCheckDs(dataSourceDTO);
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        coreDatasource.setCreateTime(System.currentTimeMillis());
        coreDatasource.setUpdateTime(System.currentTimeMillis());
        checkDatasourceStatus(coreDatasource);
        coreDatasource.setTaskStatus(TaskStatus.WaitingForExecution.name());
        coreDatasource.setId(IDUtils.snowID());
        datasourceMapper.insert(coreDatasource);
        if (dataSourceDTO.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name())) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            List<DatasetTableDTO> tables = ExcelUtils.getTables(datasourceRequest);
            for (DatasetTableDTO table : tables) {
                datasourceRequest.setTable(table.getTableName());
                List<TableField> tableFields = ExcelUtils.getTableFields(datasourceRequest);
                datasourceSyncManage.createEngineTable(datasourceRequest.getTable(), tableFields);
            }
            //TODO sync data
            datasourceSyncManage.extractExcelData(coreDatasource.getId(), "all_scope");
        }
        if (dataSourceDTO.getType().equals(DatasourceConfiguration.DatasourceType.API.name())) {
            CoreDatasourceTask coreDatasourceTask = new CoreDatasourceTask();
            BeanUtils.copyBean(coreDatasourceTask, dataSourceDTO.getSyncSetting());
            coreDatasourceTask.setName(coreDatasource.getName() + "-task");
            coreDatasourceTask.setDsId(coreDatasource.getId());
            datasourceTaskServer.insert(coreDatasourceTask);
            datasourceSyncManage.addSchedule(coreDatasourceTask);
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            List<DatasetTableDTO> tables = ApiUtils.getTables(datasourceRequest);
            for (DatasetTableDTO api : tables) {
                datasourceRequest.setTable(api.getTableName());
                List<TableField> tableFields = ApiUtils.getTableFields(datasourceRequest);
                datasourceSyncManage.createEngineTable(datasourceRequest.getTable(), tableFields);
            }
        }
        if (ObjectUtils.isNotEmpty(interactiveAuthApi)) {
            BusiResourceCreator creator = new BusiResourceCreator();
            creator.setId(coreDatasource.getId());
            creator.setPid(0L);
            creator.setFlag(RESOURCE_FLAG);
            creator.setName(dataSourceDTO.getName());
            creator.setLeaf(true);
            interactiveAuthApi.saveResource(creator);
        }
        return dataSourceDTO;
    }

    @Override
    public DatasourceDTO update(DatasourceDTO dataSourceDTO) throws Exception {
        Long pk = null;
        if (ObjectUtils.isEmpty(pk = dataSourceDTO.getId())) {
            return save(dataSourceDTO);
        }
        CoreDatasource sourceData = datasourceMapper.selectById(pk);
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        preCheckDs(dataSourceDTO);
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        coreDatasource.setUpdateTime(System.currentTimeMillis());
        checkDatasourceStatus(coreDatasource);
        datasourceMapper.updateById(coreDatasource);
        if (dataSourceDTO.getType().equals(DatasourceConfiguration.DatasourceType.API.name())) {
            CoreDatasourceTask coreDatasourceTask = new CoreDatasourceTask();
            BeanUtils.copyBean(coreDatasourceTask, dataSourceDTO.getSyncSetting());
            coreDatasourceTask.setName(coreDatasource.getName() + "-task");
            coreDatasourceTask.setDsId(coreDatasource.getId());
            datasourceTaskServer.update(coreDatasourceTask);
        }
        if (ObjectUtils.isNotEmpty(interactiveAuthApi) && ObjectUtils.isNotEmpty(sourceData) && !StringUtils.equals(dataSourceDTO.getName(), sourceData.getName())) {
            BusiResourceEditor editor = new BusiResourceEditor();
            editor.setId(pk);
            editor.setFlag(RESOURCE_FLAG);
            editor.setName(dataSourceDTO.getName());
            interactiveAuthApi.editResource(editor);
        }
        return dataSourceDTO;
    }

    @Override
    public Collection<DatasourceConfiguration> datasourceTypes() {
        Collection<DatasourceConfiguration> datasourceConfigurations = CommonBeanFactory.getApplicationContext().getBeansOfType(DatasourceConfiguration.class).values();
        return datasourceConfigurations;
    }

    @Override
    public DatasourceDTO validate(DatasourceDTO dataSourceDTO) throws DEException {
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        checkDatasourceStatus(coreDatasource);
        dataSourceDTO.setStatus(coreDatasource.getStatus());
        return dataSourceDTO;
    }

    @Override
    public DatasourceDTO validate(Long datasourceId) throws DEException {
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        checkDatasourceStatus(coreDatasource);
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, coreDatasource);
        return datasourceDTO;
    }

    @Override
    public List<DatasourceDTO> list() {
        List<DatasourceDTO> datasourceDTOS = new ArrayList<>();
        Collection<DatasourceConfiguration> datasourceConfigurations = datasourceTypes();
        QueryWrapper<CoreDatasource> queryWrapper = new QueryWrapper();
        if (ObjectUtils.isNotEmpty(interactiveAuthApi)) {
            List<Long> ids = interactiveAuthApi.resourceIds(RESOURCE_FLAG);
            queryWrapper.in("id", ids);
        }
        datasourceMapper.selectList(queryWrapper).forEach(coreDatasource -> {
            DatasourceDTO datasourceDTO = new DatasourceDTO();
            BeanUtils.copyBean(datasourceDTO, coreDatasource);
            datasourceConfigurations.forEach(datasourceConfiguration -> {
                if (StringUtils.equals(datasourceDTO.getType(), datasourceConfiguration.getType())) {
                    datasourceDTO.setTypeAlias(datasourceConfiguration.getName());
                    datasourceDTO.setCatalog(datasourceConfiguration.getCatalog());
                    datasourceDTO.setCatalogDesc(datasourceConfiguration.getCatalogDesc());
                }
            });
            TypeReference<List<ApiDefinition>> listTypeReference = new TypeReference<List<ApiDefinition>>() {};
            if (datasourceDTO.getType().equalsIgnoreCase(DatasourceConfiguration.DatasourceType.API.toString())) {
                List<ApiDefinition> apiDefinitionList = JsonUtil.parseList(datasourceDTO.getConfiguration(), listTypeReference);

                List<ApiDefinition> apiDefinitionListWithStatus = new ArrayList<>();
                int success = 0;
                for (int i = 0; i < apiDefinitionList.size(); i++) {
                    String status = null;
                    if (StringUtils.isNotEmpty(datasourceDTO.getStatus())) {
                        JsonNode jsonNode = null;
                        try {
                            jsonNode = objectMapper.readTree(datasourceDTO.getStatus());
                        } catch (Exception e) {
                            DEException.throwException(e);
                        }
                        if (jsonNode.get(apiDefinitionList.get(i).getName()) != null) {
                            status = jsonNode.get(apiDefinitionList.get(i).getName()).asText();
                        }
                        apiDefinitionList.get(i).setStatus(status);
                    }
                    if (StringUtils.isNotEmpty(status) && status.equalsIgnoreCase("Success")) {
                        success++;
                    }
                    apiDefinitionListWithStatus.add(apiDefinitionList.get(i));
                }
                datasourceDTO.setApiConfigurationStr(new String(Base64.getEncoder().encode(JsonUtil.toJSONString(apiDefinitionListWithStatus).toString().getBytes())));
                if (success == apiDefinitionList.size()) {
                    datasourceDTO.setStatus("Success");
                } else {
                    if (success > 0 && success < apiDefinitionList.size()) {
                        datasourceDTO.setStatus("Warning");
                    } else {
                        datasourceDTO.setStatus("Error");
                    }
                }
                CoreDatasourceTask coreDatasourceTask = datasourceTaskServer.selectByDSId(datasourceDTO.getId());
                TaskDTO taskDTO = new TaskDTO();
                BeanUtils.copyBean(taskDTO, coreDatasourceTask);
                datasourceDTO.setSyncSetting(taskDTO);
            }
            datasourceDTO.setConfiguration(new String(Base64.getEncoder().encode(coreDatasource.getConfiguration().getBytes())));
            datasourceDTOS.add(datasourceDTO);
        });
        return datasourceDTOS;
    }

    @Override
    public List<DatasetTableDTO> getTables(String datasourceId) throws Exception {
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(coreDatasource);
        if (coreDatasource.getType().equals("API")) {
            return ApiUtils.getTables(datasourceRequest);
        }
        if (coreDatasource.getType().equals("Excel")) {
            return ExcelUtils.getTables(datasourceRequest);
        }
        return calciteProvider.getTables(datasourceRequest);
    }

    @Override
    public List<TableField> getTableField(@PathVariable("datasourceId") String datasourceId, @PathVariable("tableName") String tableName) throws Exception {
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(coreDatasource);
        if (coreDatasource.getType().equals("API") || coreDatasource.getType().equals("Excel")) {
            datasourceRequest.setDatasource(engineServer.getDeEngine());
            DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
            BeanUtils.copyBean(datasourceSchemaDTO, engineServer.getDeEngine());
            datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, 0));
            datasourceRequest.setDsList(Map.of(datasourceSchemaDTO.getId(), datasourceSchemaDTO));
            datasourceRequest.setQuery(TableUtils.tableName2Sql(datasourceSchemaDTO, tableName));
            List<TableField> tableFields = (List<TableField>) calciteProvider.fetchResultField(datasourceRequest).get("fields");
            return tableFields.stream().filter(tableField -> {
                return !tableField.getOriginName().equalsIgnoreCase("dataease_uuid");
            }).collect(Collectors.toList());
        }

        DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
        BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
        datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, 0));
        datasourceRequest.setDsList(Map.of(datasourceSchemaDTO.getId(), datasourceSchemaDTO));
        datasourceRequest.setQuery(TableUtils.tableName2Sql(datasourceSchemaDTO, tableName));
        return (List<TableField>) calciteProvider.fetchResultField(datasourceRequest).get("fields");
    }

    ;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件", required = true, dataType = "MultipartFile"),
            @ApiImplicitParam(name = "tableId", value = "数据表ID", required = true, dataType = "String"),
            @ApiImplicitParam(name = "editType", value = "编辑类型", required = true, dataType = "Integer")
    })
    public ExcelFileData excelUpload(@RequestParam("file") MultipartFile file, @RequestParam("id") long datasourceId) throws DEException {
        return ExcelUtils.excelSaveAndParse(file, datasourceId);
    }


    public ApiDefinition checkApiDatasource(@RequestBody Map<String, String> request) throws DEException {
        ApiDefinition apiDefinition = JsonUtil.parseObject(new String(java.util.Base64.getDecoder().decode(request.get("data"))), ApiDefinition.class);
        String response = ApiUtils.execHttpRequest(apiDefinition, 10);
        return ApiUtils.checkApiDefinition(apiDefinition, response);
    }

    private void preCheckDs(DatasourceDTO datasource) throws DEException {
        if (!datasourceTypes().stream().map(DatasourceConfiguration::getType).collect(Collectors.toList()).contains(datasource.getType())) {
            DEException.throwException("Datasource type not supported.");
        }
        //TODO check Configuration
        checkName(datasource.getName(), datasource.getType(), datasource.getId());
    }

    private void checkName(String name, String type, Long id) throws DEException {
        QueryWrapper<CoreDatasource> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        queryWrapper.eq("type", type);
        if (id != null) {
            queryWrapper.ne("id", id);
        }
        if (!CollectionUtils.isEmpty(datasourceMapper.selectList(queryWrapper))) {
            DEException.throwException("ds_name_exists");
        }
    }

    public void checkDatasourceStatus(CoreDatasource coreDatasource) throws DEException {
        if (coreDatasource.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name())) {
            return;
        }
        try {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            String status = null;
            if (coreDatasource.getType().equals("API")) {
                status = ApiUtils.checkStatus(datasourceRequest);
            } else {
                status = calciteProvider.checkStatus(datasourceRequest);
            }
            coreDatasource.setStatus(status);
        } catch (Exception e) {
            coreDatasource.setStatus("Error");
            DEException.throwException("校验失败: " + e.getMessage());
        }
    }


    public void updateDemoDs() {
//        CoreDatasource datasource = datasourceMapper.selectById(1);
//        if (datasource == null) {
//            return;
//        }
//        JsonNode rootNode = null;
//        try {
//            rootNode = objectMapper.readTree(datasource.getConfiguration());
//        } catch (Exception e) {
//        }
//        ObjectNode objectNode = (ObjectNode) rootNode;
//        Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
//        Matcher matcher = WITH_SQL_FRAGMENT.matcher(env.getProperty("spring.datasource.url"));
//        if (!matcher.find()) {
//            return;
//        }
//        objectNode.put("port", Integer.valueOf(matcher.group(2)));
//        objectNode.put("dataBase", matcher.group(3).split("\\?")[0]);
//        objectNode.put("extraParams", matcher.group(3).split("\\?")[1]);
//        objectNode.put("username", env.getProperty("spring.datasource.username"));
//        objectNode.put("password", env.getProperty("spring.datasource.password"));
//        datasource.setConfiguration(JsonUtil.toJSONString(objectNode).toString());
//        datasourceMapper.updateById(datasource);
    }


}
