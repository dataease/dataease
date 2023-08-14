package io.dataease.datasource.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.dataset.dto.PreviewSqlDTO;
import io.dataease.api.ds.DatasourceApi;
import io.dataease.api.ds.vo.*;
import io.dataease.api.permissions.auth.api.InteractiveAuthApi;
import io.dataease.api.permissions.auth.dto.BusiResourceCreator;
import io.dataease.api.permissions.auth.dto.BusiResourceEditor;
import io.dataease.api.permissions.auth.dto.BusiResourceMover;
import io.dataease.commons.constants.DataSourceType;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.dataset.dto.DatasourceSchemaDTO;
import io.dataease.dataset.manage.DatasetDataManage;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.entity.CoreDatasourceTask;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.datasource.dto.DatasourceNodeBO;
import io.dataease.datasource.manage.DataSourceManage;
import io.dataease.datasource.manage.DatasourceSyncManage;
import io.dataease.datasource.provider.ApiUtils;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.datasource.provider.ExcelUtils;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
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

    @Resource
    private DataSourceManage dataSourceManage;

    @Resource
    private DatasetDataManage datasetDataManage;

    @Override
    public List<DatasourceDTO> query(String keyWord) {
        return null;
    }

    public enum UpdateType {
        all_scope, add_scope
    }

    @Override
    public DatasourceDTO move(DatasourceDTO dataSourceDTO) throws Exception {
        switch (dataSourceDTO.getAction()) {
            case "move":
                if (Objects.equals(dataSourceDTO.getId(), dataSourceDTO.getPid())) {
                    DEException.throwException("pid can not equal to id.");
                }
                CoreDatasource sourceData = datasourceMapper.selectById(dataSourceDTO.getId());
                checkName(dataSourceDTO.getName(), sourceData.getType(), dataSourceDTO.getId(), dataSourceDTO.getPid());
                UpdateWrapper<CoreDatasource> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("id", dataSourceDTO.getId());
                CoreDatasource record = new CoreDatasource();
                record.setPid(dataSourceDTO.getPid());
                record.setName(dataSourceDTO.getName());
                datasourceMapper.update(record, updateWrapper);
                if (ObjectUtils.isNotEmpty(interactiveAuthApi) && ObjectUtils.isNotEmpty(sourceData) && (!StringUtils.equals(dataSourceDTO.getName(), sourceData.getName()) || !sourceData.getPid().equals(dataSourceDTO.getPid()))) {
                    BusiResourceMover mover = new BusiResourceMover();
                    mover.setId(record.getId());
                    mover.setPid(record.getPid());
                    interactiveAuthApi.moveResource(mover);
                }
                break;
            case "rename":
                CoreDatasource datasource = datasourceMapper.selectById(dataSourceDTO.getId());
                checkName(dataSourceDTO.getName(), datasource.getType(), dataSourceDTO.getId(), dataSourceDTO.getPid());
                UpdateWrapper<CoreDatasource> wrapper = new UpdateWrapper<>();
                wrapper.eq("id", dataSourceDTO.getId());
                CoreDatasource coreDatasourceRecord = new CoreDatasource();
                coreDatasourceRecord.setPid(dataSourceDTO.getPid());
                coreDatasourceRecord.setName(dataSourceDTO.getName());
                datasourceMapper.update(coreDatasourceRecord, wrapper);
                if (ObjectUtils.isNotEmpty(interactiveAuthApi) && ObjectUtils.isNotEmpty(datasource) && (!StringUtils.equals(dataSourceDTO.getName(), datasource.getName()) || !datasource.getPid().equals(dataSourceDTO.getPid()))) {
                    BusiResourceEditor editor = new BusiResourceEditor();
                    editor.setId(dataSourceDTO.getId());
                    editor.setFlag(RESOURCE_FLAG);
                    editor.setName(dataSourceDTO.getName());
                    interactiveAuthApi.editResource(editor);
                }

                break;
            case "create":
                checkName(dataSourceDTO.getName(), dataSourceDTO.getNodeType(), dataSourceDTO.getId(), dataSourceDTO.getPid());
                CoreDatasource coreDatasource = new CoreDatasource();
                BeanUtils.copyBean(coreDatasource, dataSourceDTO);
                coreDatasource.setCreateTime(System.currentTimeMillis());
                coreDatasource.setUpdateTime(System.currentTimeMillis());
                coreDatasource.setTaskStatus(TaskStatus.WaitingForExecution.name());
                coreDatasource.setType(dataSourceDTO.getNodeType());
                coreDatasource.setId(IDUtils.snowID());
                coreDatasource.setConfiguration("");
                datasourceMapper.insert(coreDatasource);
                if (ObjectUtils.isNotEmpty(interactiveAuthApi)) {
                    BusiResourceCreator creator = new BusiResourceCreator();
                    creator.setId(coreDatasource.getId());
                    creator.setPid(coreDatasource.getPid());
                    creator.setFlag(RESOURCE_FLAG);
                    creator.setName(dataSourceDTO.getName());
                    creator.setLeaf(!coreDatasource.getType().equals("folder"));
                    creator.setExtraFlag(DataSourceType.valueOf(coreDatasource.getType()).getFlag());
                    interactiveAuthApi.saveResource(creator);
                }
                break;
            default:
                break;
        }
        return dataSourceDTO;
    }

    @Override
    public DatasourceDTO save(DatasourceDTO dataSourceDTO) throws Exception {

        boolean leaf = true;
        if (StringUtils.isNotEmpty(dataSourceDTO.getAction())) {
            move(dataSourceDTO);
            return dataSourceDTO;
        }
        if (StringUtils.isNotEmpty(dataSourceDTO.getNodeType()) && dataSourceDTO.getNodeType().equalsIgnoreCase("folder")) {
            dataSourceDTO.setType("folder");
            dataSourceDTO.setConfiguration("");
            leaf = false;
        }
        if (dataSourceDTO.getId() != null && dataSourceDTO.getId() > 0) {
            return update(dataSourceDTO);
        }
        if (StringUtils.isNotEmpty(dataSourceDTO.getConfiguration())) {
            dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        }
        preCheckDs(dataSourceDTO);
        dataSourceDTO.setId(IDUtils.snowID());
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        coreDatasource.setCreateTime(System.currentTimeMillis());
        coreDatasource.setUpdateTime(System.currentTimeMillis());
        try {
            checkDatasourceStatus(coreDatasource);
        } catch (Exception ignore) {
        }
        coreDatasource.setTaskStatus(TaskStatus.WaitingForExecution.name());
        coreDatasource.setCreateBy(AuthUtils.getUser().getUserId().toString());
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
            creator.setPid(coreDatasource.getPid());
            creator.setFlag(RESOURCE_FLAG);
            creator.setName(dataSourceDTO.getName());
            creator.setLeaf(leaf);
            creator.setExtraFlag(DataSourceType.valueOf(dataSourceDTO.getType()).getFlag());
            interactiveAuthApi.saveResource(creator);
        }
        calciteProvider.update(dataSourceDTO);
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
        try {
            checkDatasourceStatus(coreDatasource);
        } catch (Exception ignore) {
        }

        DatasourceRequest sourceTableRequest = new DatasourceRequest();
        sourceTableRequest.setDatasource(sourceData);
        DatasourceRequest coreDatasourceRequest = new DatasourceRequest();
        coreDatasourceRequest.setDatasource(coreDatasource);
        List<String> toCreateTables = new ArrayList<>();
        List<String> toDeleteTables = new ArrayList<>();
        if (dataSourceDTO.getType().equals(DatasourceConfiguration.DatasourceType.API.name())) {
            List<String> sourceTables = ApiUtils.getTables(sourceTableRequest).stream().map(DatasetTableDTO::getTableName).collect(Collectors.toList());
            List<String> tables = ApiUtils.getTables(coreDatasourceRequest).stream().map(DatasetTableDTO::getTableName).collect(Collectors.toList());
            toCreateTables = tables.stream().filter(table -> !sourceTables.contains(table)).collect(Collectors.toList());
            toDeleteTables = sourceTables.stream().filter(table -> !tables.contains(table)).collect(Collectors.toList());
            for (String table : tables) {
                for (String sourceTable : sourceTables) {
                    if (table.equals(sourceTable)) {
                        coreDatasourceRequest.setTable(table);
                        List<String> tableFields = ApiUtils.getTableFields(coreDatasourceRequest).stream().map(TableField::getName).sorted().collect(Collectors.toList());
                        sourceTableRequest.setTable(sourceTable);
                        List<String> sourceTableFields = ApiUtils.getTableFields(sourceTableRequest).stream().map(TableField::getName).sorted().collect(Collectors.toList());
                        if (!String.join(",", tableFields).equals(String.join(",", sourceTableFields))) {
                            toDeleteTables.add(table);
                            toCreateTables.add(table);
                        }
                    }
                }
            }
            CoreDatasourceTask coreDatasourceTask = new CoreDatasourceTask();
            BeanUtils.copyBean(coreDatasourceTask, dataSourceDTO.getSyncSetting());
            coreDatasourceTask.setName(coreDatasource.getName() + "-task");
            coreDatasourceTask.setDsId(coreDatasource.getId());
            datasourceTaskServer.update(coreDatasourceTask);
            for (String deleteTable : toDeleteTables) {
                datasourceSyncManage.dropEngineTable(deleteTable);
            }
            for (String toCreateTable : toCreateTables) {
                coreDatasourceRequest.setTable(toCreateTable);
                datasourceSyncManage.createEngineTable(toCreateTable, ApiUtils.getTableFields(coreDatasourceRequest));
            }
            datasourceMapper.updateById(coreDatasource);
            datasourceSyncManage.addSchedule(coreDatasourceTask);
        } else if (dataSourceDTO.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name())) {
            List<String> sourceTables = ExcelUtils.getTables(sourceTableRequest).stream().map(DatasetTableDTO::getTableName).collect(Collectors.toList());
            List<String> tables = ExcelUtils.getTables(coreDatasourceRequest).stream().map(DatasetTableDTO::getTableName).collect(Collectors.toList());
            if (dataSourceDTO.getEditType() == 0) {
                toCreateTables = tables;
                toDeleteTables = sourceTables;
                for (String deleteTable : toDeleteTables) {
                    datasourceSyncManage.dropEngineTable(deleteTable);
                }
                for (String toCreateTable : toCreateTables) {
                    coreDatasourceRequest.setTable(toCreateTable);
                    datasourceSyncManage.createEngineTable(toCreateTable, ExcelUtils.getTableFields(coreDatasourceRequest));
                }
                datasourceMapper.updateById(coreDatasource);
                datasourceSyncManage.extractExcelData(coreDatasource.getId(), "all_scope");
            } else {
                datasourceMapper.updateById(coreDatasource);
                datasourceSyncManage.extractExcelData(coreDatasource.getId(), "add_scope");
            }
        } else {
            datasourceMapper.updateById(coreDatasource);
        }
        if (ObjectUtils.isNotEmpty(interactiveAuthApi) && ObjectUtils.isNotEmpty(sourceData) && !StringUtils.equals(dataSourceDTO.getName(), sourceData.getName())) {
            BusiResourceEditor editor = new BusiResourceEditor();
            editor.setId(pk);
            editor.setFlag(RESOURCE_FLAG);
            editor.setName(dataSourceDTO.getName());
            interactiveAuthApi.editResource(editor);
        }
        calciteProvider.update(dataSourceDTO);
        return dataSourceDTO;
    }

    private String excelDataTableName(String name) {
        return StringUtils.substring(name, 6, name.length() - 37);
    }

    @Override
    public List<DatasourceConfiguration.DatasourceType> datasourceTypes() {
        return Arrays.asList(DatasourceConfiguration.DatasourceType.values());
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
    public List<String> getSchema(DatasourceDTO dataSourceDTO) throws Exception {
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(coreDatasource);
        return calciteProvider.getSchema(datasourceRequest);
    }


    @Override
    public DatasourceDTO get(Long datasourceId) throws Exception {
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        CoreDatasource datasource = datasourceMapper.selectById(datasourceId);
        BeanUtils.copyBean(datasourceDTO, datasource);
        TypeReference<List<ApiDefinition>> listTypeReference = new TypeReference<List<ApiDefinition>>() {
        };
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
        if (datasourceDTO.getType().equalsIgnoreCase(DatasourceConfiguration.DatasourceType.Excel.toString())) {
            datasourceDTO.setFileName(ExcelUtils.getFileName(datasource));
            datasourceDTO.setSize(ExcelUtils.getSize(datasource));
        }
        datasourceDTO.setConfiguration(new String(Base64.getEncoder().encode(datasourceDTO.getConfiguration().getBytes())));
        return datasourceDTO;
    }

    @Override
    public void delete(Long datasourceId) throws Exception {
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        if (ObjectUtils.isEmpty(coreDatasource)) {
            return;
        }
        if (ObjectUtils.isNotEmpty(interactiveAuthApi) && !interactiveAuthApi.checkDel(datasourceId)) {
            return;
        }
        if (coreDatasource.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name())) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            List<DatasetTableDTO> tables = ExcelUtils.getTables(datasourceRequest);
            for (DatasetTableDTO table : tables) {
                datasourceRequest.setTable(table.getTableName());
                datasourceSyncManage.dropEngineTable(datasourceRequest.getTable());
            }
        }
        if (coreDatasource.getType().equals(DatasourceConfiguration.DatasourceType.API.name())) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            List<DatasetTableDTO> tables = ApiUtils.getTables(datasourceRequest);
            for (DatasetTableDTO api : tables) {
                datasourceRequest.setTable(api.getTableName());
                datasourceSyncManage.dropEngineTable(datasourceRequest.getTable());
            }
            datasourceTaskServer.deleteByDSId(datasourceId);
        }
        datasourceMapper.deleteById(datasourceId);

        if (ObjectUtils.isNotEmpty(interactiveAuthApi)) {
            interactiveAuthApi.delResource(datasourceId);
        }
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
    public List<BusiNodeVO> tree(BusiNodeRequest request) throws DEException {
        return dataSourceManage.tree(request);
    }

    /*private DatasourceNodeBO rootNode() {
        DatasourceNodeBO bo = new DatasourceNodeBO();
        bo.setId(0L);
        bo.setName("root");
        bo.setLeaf(false);
        bo.setWeight(3);
        bo.setPid(-1L);
        bo.setExtraFlag(0);
        return bo;
    }*/

    /*public List<DatasourceDTO> list() {
        List<DatasourceDTO> datasourceDTOS = new ArrayList<>();
        List<DatasourceConfiguration.DatasourceType> datasourceConfigurations = datasourceTypes();
        QueryWrapper<CoreDatasource> queryWrapper = new QueryWrapper();
        datasourceMapper.selectList(queryWrapper).forEach(coreDatasource -> {
            DatasourceDTO datasourceDTO = new DatasourceDTO();
            BeanUtils.copyBean(datasourceDTO, coreDatasource);
            datasourceConfigurations.forEach(datasourceConfiguration -> {
                if (StringUtils.equals(datasourceDTO.getType(), datasourceConfiguration.getType())) {
                    datasourceDTO.setTypeAlias(datasourceConfiguration.getName());
                    datasourceDTO.setCatalog(datasourceConfiguration.getCatalog());
                }
            });
            TypeReference<List<ApiDefinition>> listTypeReference = new TypeReference<List<ApiDefinition>>() {
            };
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
    }*/

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
            datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
            datasourceRequest.setDsList(Map.of(datasourceSchemaDTO.getId(), datasourceSchemaDTO));
            datasourceRequest.setQuery(TableUtils.tableName2Sql(datasourceSchemaDTO, tableName) + " LIMIT 0 OFFSET 0");
            List<TableField> tableFields = (List<TableField>) calciteProvider.fetchResultField(datasourceRequest).get("fields");
            return tableFields.stream().filter(tableField -> {
                return !tableField.getOriginName().equalsIgnoreCase("dataease_uuid");
            }).collect(Collectors.toList());
        }

        DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
        BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
        datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
        datasourceRequest.setDsList(Map.of(datasourceSchemaDTO.getId(), datasourceSchemaDTO));
        datasourceRequest.setQuery(TableUtils.tableName2Sql(datasourceSchemaDTO, tableName) + " LIMIT 0 OFFSET 0");
        return (List<TableField>) calciteProvider.fetchResultField(datasourceRequest).get("fields");
    }

    public ExcelFileData excelUpload(@RequestParam("file") MultipartFile file, @RequestParam("id") long datasourceId, @RequestParam("editType") Integer editType) throws Exception {
        ExcelUtils excelUtils = new ExcelUtils();
        ExcelFileData excelFileData = excelUtils.excelSaveAndParse(file);
        if (editType == 1) { //追加，判断是否能追加成功，按照excel sheet 名称匹配
            CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            List<DatasetTableDTO> datasetTableDTOS = ExcelUtils.getTables(datasourceRequest);
            List<ExcelSheetData> excelSheetDataList = new ArrayList<>();
            for (ExcelSheetData sheet : excelFileData.getSheets()) {
                for (DatasetTableDTO datasetTableDTO : datasetTableDTOS) {
                    if (excelDataTableName(datasetTableDTO.getTableName()).equals(sheet.getTableName())) {
                        sheet.setDeTableName(datasetTableDTO.getTableName());
                        excelSheetDataList.add(sheet);
                    }
                }

            }
            if (CollectionUtils.isEmpty(excelSheetDataList)) {
                DEException.throwException("无匹配的Sheet页!");
            }
            excelFileData.setSheets(excelSheetDataList);
        }
        for (ExcelSheetData sheet : excelFileData.getSheets()) {
            for (int i = 0; i < sheet.getFields().size() -1 ; i++) {
                for (int j = i + 1; j < sheet.getFields().size()  ; j++) {
                    if(sheet.getFields().get(i).getName().equalsIgnoreCase(sheet.getFields().get(j).getName())){
                        DEException.throwException(sheet.getExcelLabel() + Translator.get("i18n_field_name_repeat") + sheet.getFields().get(i).getName());
                    }
                }
            }
        }
        return excelFileData;
    }


    public ApiDefinition checkApiDatasource(@RequestBody Map<String, String> request) throws DEException {
        ApiDefinition apiDefinition = JsonUtil.parseObject(new String(java.util.Base64.getDecoder().decode(request.get("data"))), ApiDefinition.class);
        String response = ApiUtils.execHttpRequest(apiDefinition, 10);
        return ApiUtils.checkApiDefinition(apiDefinition, response);
    }

    private void preCheckDs(DatasourceDTO datasource) throws DEException {
        if (!datasourceTypes().stream().map(DatasourceConfiguration.DatasourceType::getType).collect(Collectors.toList()).contains(datasource.getType())) {
            DEException.throwException("Datasource type not supported.");
        }
        checkName(datasource.getName(), datasource.getType(), datasource.getId(), datasource.getPid());
    }

    private void checkName(String name, String type, Long id, Long pid) throws DEException {
        QueryWrapper<CoreDatasource> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", name);
        if (id != null && id != 0) {
            queryWrapper.ne("id", id);
        }
        if (pid != null) {
            queryWrapper.eq("pid", id);
        }
        CoreDatasource datasource = datasourceMapper.selectById(id);
        if (datasource != null) {
            queryWrapper.eq("pid", datasource.getPid());
        }

        if (!CollectionUtils.isEmpty(datasourceMapper.selectList(queryWrapper))) {
            DEException.throwException(Translator.get("i18n_ds_name_exists"));
        }
    }

    public void checkDatasourceStatus(CoreDatasource coreDatasource) throws DEException {
        if (coreDatasource.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name()) || coreDatasource.getType().equals(DatasourceConfiguration.DatasourceType.folder.name())) {
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
    }

    @Override
    public Map<String, Object> previewDataWithLimit(Map<String, Object> req) throws Exception {
        String tableName = req.get("table").toString();
        Long id = Long.valueOf(req.get("id").toString());
        if (ObjectUtils.isEmpty(tableName) || ObjectUtils.isEmpty(id)) {
            return null;
        }
        String sql = "SELECT * FROM `" + tableName + "`";
        sql = new String(Base64.getEncoder().encode(sql.getBytes()));
        PreviewSqlDTO previewSqlDTO = new PreviewSqlDTO();
        previewSqlDTO.setSql(sql);
        previewSqlDTO.setDatasourceId(id);
        return datasetDataManage.previewSql(previewSqlDTO);
    }

    @Override
    public List<String> latestUse(){
        List<String> types = new ArrayList<>();
        QueryWrapper<CoreDatasource> queryWrapper = new QueryWrapper();
        queryWrapper.eq("create_by", AuthUtils.getUser().getUserId());
        queryWrapper.ge("create_time", System.currentTimeMillis() - 24 * 60 * 1000);
        List<CoreDatasource> coreDatasources = datasourceMapper.selectList(queryWrapper);
        if(CollectionUtils.isEmpty(coreDatasources)){
            return types;
        }
        for (CoreDatasource coreDatasource : coreDatasources) {
            if(!coreDatasource.getType().equalsIgnoreCase("folder") && !types.contains(coreDatasource.getType())){
                types.add(coreDatasource.getType());
            }
        }
        return types;
    }
}
