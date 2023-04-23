package io.dataease.datasource.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.ds.DatasourceApi;
import io.dataease.api.ds.vo.ApiDefinition;
import io.dataease.api.ds.vo.DatasourceConfiguration;
import io.dataease.api.ds.vo.DatasourceDTO;
import io.dataease.api.ds.vo.TableField;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTask;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTaskLog;
import io.dataease.datasource.dao.auto.entity.CoreDeEngine;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.datasource.dto.ExcelSheetData;
import io.dataease.datasource.provider.*;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.datasource.request.EngineRequest;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.LogUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/datasource")
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

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<DatasourceDTO> query(String keyWord) {
        return null;
    }

    public enum UpdateType {
        all_scope, add_scope
    }

    public enum SpecialDatasourceType {
        API, EXCEL
    }

    @Override
    public DatasourceDTO save(DatasourceDTO dataSourceDTO) throws Exception {
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        preCheckDs(dataSourceDTO);
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        coreDatasource.setCreateTime(System.currentTimeMillis());
        checkDatasourceStatus(coreDatasource);
        datasourceMapper.insert(coreDatasource);
        if (dataSourceDTO.getType().equals(SpecialDatasourceType.EXCEL.name())) {
            //TODO sync excel at once

        }
        return dataSourceDTO;
    }

    @Override
    public DatasourceDTO update(DatasourceDTO dataSourceDTO) throws Exception {
        if (dataSourceDTO.getId() == null) {
            return save(dataSourceDTO);
        }
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        preCheckDs(dataSourceDTO);
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        coreDatasource.setUpdateTime(System.currentTimeMillis());
        checkDatasourceStatus(coreDatasource);
        datasourceMapper.updateById(coreDatasource);
        return dataSourceDTO;
    }

    @Override
    public Collection<DatasourceConfiguration> datasourceTypes() {
        Collection<DatasourceConfiguration> datasourceConfigurations = CommonBeanFactory.getApplicationContext().getBeansOfType(DatasourceConfiguration.class).values();
        return datasourceConfigurations;
    }

    @Override
    public DatasourceDTO validate(DatasourceDTO dataSourceDTO) throws Exception {
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        checkDatasourceStatus(coreDatasource);
        dataSourceDTO.setStatus(coreDatasource.getStatus());
        return dataSourceDTO;
    }

    @Override
    public DatasourceDTO validate(String datasourceId) throws Exception {
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
        datasourceMapper.selectList(null).forEach(coreDatasource -> {
            DatasourceDTO datasourceDTO = new DatasourceDTO();
            BeanUtils.copyBean(datasourceDTO, coreDatasource);
            datasourceConfigurations.forEach(datasourceConfiguration -> {
                if (StringUtils.equals(datasourceDTO.getType(), datasourceConfiguration.getType())) {
                    datasourceDTO.setTypeAlias(datasourceConfiguration.getName());
                }
            });
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
        if (coreDatasource.getType().equals("EXCEL")) {
            return ExcelUtils.getTables(datasourceRequest);
        }
        return calciteProvider.getTables(datasourceRequest);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件", required = true, dataType = "MultipartFile"),
            @ApiImplicitParam(name = "tableId", value = "数据表ID", required = true, dataType = "String"),
            @ApiImplicitParam(name = "editType", value = "编辑类型", required = true, dataType = "Integer")
    })
    public List<ExcelSheetData> excelUpload(@RequestParam("file") MultipartFile file, @RequestParam("datasourceId") String datasourceId) throws Exception {
        return ExcelUtils.excelSaveAndParse(file, datasourceId);
    }


    public ApiDefinition checkApiDatasource(@RequestBody Map<String, String> request) throws Exception {
        ApiDefinition apiDefinition = JsonUtil.parse(new String(java.util.Base64.getDecoder().decode(request.get("data"))), ApiDefinition.class);
        String response = ApiUtils.execHttpRequest(apiDefinition, 10);
        return ApiUtils.checkApiDefinition(apiDefinition, response);
    }

    private void preCheckDs(DatasourceDTO datasource) throws Exception {
        if (!datasourceTypes().stream().map(DatasourceConfiguration::getType).collect(Collectors.toList()).contains(datasource.getType())) {
            throw new Exception("Datasource type not supported.");
        }
        //TODO check Configuration
        checkName(datasource.getName(), datasource.getType(), datasource.getId());
    }

    private void checkName(String name, String type, Long id) throws Exception {
        QueryWrapper<CoreDatasource> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        queryWrapper.eq("type", type);
        if (id != null) {
            queryWrapper.ne("id", id);
        }
        if (CollectionUtils.isEmpty(datasourceMapper.selectList(queryWrapper))) {
            throw new Exception("ds_name_exists");
        }
    }

    public void checkDatasourceStatus(CoreDatasource coreDatasource) {
        if (coreDatasource.getType().equals("EXCEL")) {
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
        }
    }

    public void extractExcelData(String datasourceId, String type) {
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        if (coreDatasource == null) {
            LogUtil.error("Can not find CoreDatasource: " + datasourceId);
            return;
        }
        Long startTime = System.currentTimeMillis();
        CoreDatasourceTaskLog datasetTableTaskLog = datasourceTaskServer.initTaskLog(datasourceId, null, startTime, coreDatasource.getName());
        UpdateType updateType = UpdateType.valueOf(type);

        boolean msg = false;

        Long execTime = System.currentTimeMillis();
        try {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            List<DatasetTableDTO> tables = ApiUtils.getTables(datasourceRequest);
            int sucsess = 0;
            for (DatasetTableDTO api : tables) {
                datasourceRequest.setTable(api.getTableName());
                try {
                    datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Begin to sync datatable: " + datasourceRequest.getTable());
                    datasourceTaskServer.saveLog(datasetTableTaskLog);
                    List<TableField> tableFields = ApiUtils.getTableFields(datasourceRequest);
                    createEngineTable(datasourceRequest.getTable(), tableFields);
                    if (updateType.equals(UpdateType.all_scope)) {
                        createEngineTable(TableUtils.tmpName(datasourceRequest.getTable()), tableFields);
                    }
                    extractApiData(datasourceRequest, updateType);
                    if (updateType.equals(UpdateType.all_scope)) {
                        replaceTable(datasourceRequest.getTable());
                    }
                    datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n End to sync datatable: " + datasourceRequest.getTable());
                    datasourceTaskServer.saveLog(datasetTableTaskLog);
                    sucsess++;
                } catch (Exception e) {
                    datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Failed to sync datatable: " + datasourceRequest.getTable() + ", " + e.getMessage());
                    datasourceTaskServer.saveLog(datasetTableTaskLog);
                }
            }
            datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Complete to sync datasourc.");
            if (sucsess == 0) {
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Failed to sync datasourc.");
                datasetTableTaskLog.setStatus(TaskStatus.Error.name());
            }
            if (sucsess == tables.size()) {
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Complete to sync datasourc.");
                datasetTableTaskLog.setStatus(TaskStatus.Completed.name());
            }
            if (0 < sucsess && sucsess < tables.size()) {
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Complete to sync datasourc.");
                datasetTableTaskLog.setStatus(TaskStatus.Warning.name());
            }
            datasetTableTaskLog.setEndTime(System.currentTimeMillis());
            datasourceTaskServer.saveLog(datasetTableTaskLog);
        } catch (Exception e) {
            datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Failed to sync datasourc.");
            datasetTableTaskLog.setEndTime(System.currentTimeMillis());
            datasetTableTaskLog.setStatus(TaskStatus.Error.name());
            datasourceTaskServer.saveLog(datasetTableTaskLog);
        } finally {
            //DELETE
        }
    }


    public void extractData(String datasourceId, String taskId, String type, JobExecutionContext context) {
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        if (coreDatasource == null) {
            LogUtil.error("Can not find DatasetTable: " + datasourceId);
            return;
        }
        CoreDatasourceTask coreDatasourceTask = datasourceTaskServer.selectById(taskId);
        if (coreDatasourceTask == null) {
            return;
        }
        if (coreDatasourceTask.getStatus().equalsIgnoreCase(TaskStatus.Stopped.name()) || coreDatasourceTask.getStatus().equalsIgnoreCase(TaskStatus.Suspend.name())) {
            LogUtil.info("Skip synchronization task: {} ,due to task status is {}", coreDatasourceTask.getId(), coreDatasourceTask.getStatus());
            datasourceTaskServer.checkTaskIsStopped(coreDatasourceTask);
            return;
        }

        Long startTime = System.currentTimeMillis();
        if (datasourceTaskServer.existUnderExecutionTask(datasourceId, coreDatasourceTask.getId(), startTime)) {
            LogUtil.info("Skip synchronization task for datasource due to exist others, datasource ID : " + datasourceId);
            return;
        }
        CoreDatasourceTaskLog datasetTableTaskLog = datasourceTaskServer.initTaskLog(datasourceId, taskId, startTime, coreDatasource.getName());
        UpdateType updateType = UpdateType.valueOf(type);
        if (context != null) {
            UpdateWrapper<CoreDatasource> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", datasourceId);
            CoreDatasource record = new CoreDatasource();
            record.setQrtzInstance(context.getFireInstanceId());
            datasourceMapper.update(record, updateWrapper);
        }

        boolean msg = false;
        TaskStatus lastExecStatus = TaskStatus.Completed;
        Long execTime = System.currentTimeMillis();
        try {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            List<DatasetTableDTO> tables = ApiUtils.getTables(datasourceRequest);
            int sucsess = 0;

            for (DatasetTableDTO api : tables) {
                datasourceRequest.setTable(api.getTableName());
                try {
                    datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Begin to sync datatable: " + datasourceRequest.getTable());
                    datasourceTaskServer.saveLog(datasetTableTaskLog);
                    List<TableField> tableFields = ApiUtils.getTableFields(datasourceRequest);
                    createEngineTable(datasourceRequest.getTable(), tableFields);
                    if (updateType.equals(UpdateType.all_scope)) {
                        createEngineTable(TableUtils.tmpName(datasourceRequest.getTable()), tableFields);
                    }
                    extractApiData(datasourceRequest, updateType);
                    if (updateType.equals(UpdateType.all_scope)) {
                        replaceTable(datasourceRequest.getTable());
                    }
                    datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n End to sync datatable: " + datasourceRequest.getTable());
                    datasourceTaskServer.saveLog(datasetTableTaskLog);
                    sucsess++;
                } catch (Exception e) {
                    datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Failed to sync datatable: " + datasourceRequest.getTable() + ", " + e.getMessage());
                    datasourceTaskServer.saveLog(datasetTableTaskLog);
                }
            }
            datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Complete to sync datasourc.");
            if (sucsess == 0) {
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Failed to sync datasourc.");
                datasetTableTaskLog.setStatus(TaskStatus.Error.name());
                lastExecStatus = TaskStatus.Error;
            }
            if (sucsess == tables.size()) {
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Complete to sync datasourc.");
                datasetTableTaskLog.setStatus(TaskStatus.Completed.name());
                lastExecStatus = TaskStatus.Completed;
            }
            if (0 < sucsess && sucsess < tables.size()) {
                datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Complete to sync datasourc.");
                datasetTableTaskLog.setStatus(TaskStatus.Warning.name());
                lastExecStatus = TaskStatus.Warning;
            }
            datasetTableTaskLog.setEndTime(System.currentTimeMillis());
            datasourceTaskServer.saveLog(datasetTableTaskLog);
        } catch (Exception e) {
            datasetTableTaskLog.setInfo(datasetTableTaskLog.getInfo() + "/n Failed to sync datasourc.");
            datasetTableTaskLog.setEndTime(System.currentTimeMillis());
            datasetTableTaskLog.setStatus(TaskStatus.Error.name());
            datasourceTaskServer.saveLog(datasetTableTaskLog);
        } finally {
            try {
                datasourceTaskServer.updateTaskStatus(coreDatasourceTask, lastExecStatus);
            } catch (Exception ignore) {
                LogUtil.error(ignore);
            }
        }
    }

    public void createEngineTable(String tableName, List<TableField> tableFields) throws Exception {
        CoreDeEngine engine = engineServer.info();
        EngineRequest engineRequest = new EngineRequest();
        engineRequest.setEngine(engine);
        EngineProvider engineProvider = ProviderUtil.getEngineProvider(engine.getType());
        engineRequest.setQuery(engineProvider.createTableSql(tableName, tableFields, engine));
        engineProvider.exec(engineRequest);
    }

    public void updateDemoDs() {
        CoreDatasource datasource = datasourceMapper.selectById(1);
        if (datasource == null) {
            return;
        }
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(datasource.getConfiguration());
        } catch (Exception e) {
        }
        ObjectNode objectNode = (ObjectNode) rootNode;
        Pattern WITH_SQL_FRAGMENT = Pattern.compile("jdbc:mysql://(.*):(\\d+)/(.*)");
        Matcher matcher = WITH_SQL_FRAGMENT.matcher(env.getProperty("spring.datasource.url"));
        if (!matcher.find()) {
            return;
        }
        objectNode.put("port", Integer.valueOf(matcher.group(2)));
        objectNode.put("dataBase", matcher.group(3).split("\\?")[0]);
        objectNode.put("extraParams", matcher.group(3).split("\\?")[1]);
        objectNode.put("username", env.getProperty("spring.datasource.username"));
        objectNode.put("password", env.getProperty("spring.datasource.password"));
        datasource.setConfiguration(JsonUtil.toJSONString(objectNode).toString());
        datasourceMapper.updateById(datasource);
    }


    private void extractApiData(DatasourceRequest datasourceRequest, UpdateType extractType) throws Exception {
        Map<String, Object> result = ApiUtils.fetchResultField(datasourceRequest);
        List<String[]> dataList = (List<String[]>) result.get("dataList");
        String engineTableName;
        switch (extractType) {
            case all_scope:
                engineTableName = TableUtils.tmpName(TableUtils.tableName(datasourceRequest.getTable()));
                break;
            default:
                engineTableName = TableUtils.tableName(datasourceRequest.getTable());
                break;
        }
        CoreDeEngine engine = engineServer.info();

        EngineRequest engineRequest = new EngineRequest();
        engineRequest.setEngine(engine);
        EngineProvider engineProvider = ProviderUtil.getEngineProvider(engine.getType());
        int pageNumber = 1000; //一次插入 1000条
        int totalPage;
        if (dataList.size() % pageNumber > 0) {
            totalPage = dataList.size() / pageNumber + 1;
        } else {
            totalPage = dataList.size() / pageNumber;
        }

        for (int page = 1; page <= totalPage; page++) {
            engineRequest.setQuery(engineProvider.insertSql(engineTableName, dataList, page, pageNumber));
            engineProvider.exec(engineRequest);
        }
    }

    private void replaceTable(String tableName) throws Exception {
        CoreDeEngine engine = engineServer.info();
        EngineRequest engineRequest = new EngineRequest();
        engineRequest.setEngine(engine);
        EngineProvider engineProvider = ProviderUtil.getEngineProvider(engine.getType());
        String[] replaceTableSql = engineProvider.replaceTable(tableName).split(";");
        for (int i = 0; i < replaceTableSql.length; i++) {
            if (StringUtils.isNotEmpty(replaceTableSql[i])) {
                engineRequest.setQuery(replaceTableSql[i]);
                engineProvider.exec(engineRequest);
            }
        }
    }

}
