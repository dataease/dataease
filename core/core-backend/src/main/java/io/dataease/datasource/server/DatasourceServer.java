package io.dataease.datasource.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.dataset.dto.PreviewSqlDTO;
import io.dataease.api.ds.DatasourceApi;
import io.dataease.api.ds.vo.*;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.commons.utils.CommonThreadPool;
import io.dataease.constant.DataSourceType;
import io.dataease.constant.LogOT;
import io.dataease.constant.LogST;
import io.dataease.dataset.dto.DatasourceSchemaDTO;
import io.dataease.dataset.manage.DatasetDataManage;
import io.dataease.dataset.utils.TableUtils;
import io.dataease.datasource.dao.auto.entity.*;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.datasource.dao.auto.mapper.CoreDsFinishPageMapper;
import io.dataease.datasource.dao.auto.mapper.QrtzSchedulerStateMapper;
import io.dataease.datasource.dao.ext.mapper.DataSourceExtMapper;
import io.dataease.datasource.dao.ext.mapper.TaskLogExtMapper;
import io.dataease.datasource.manage.DataSourceManage;
import io.dataease.datasource.manage.DatasourceSyncManage;
import io.dataease.datasource.provider.ApiUtils;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.datasource.provider.ExcelUtils;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.job.sechedule.CheckDsStatusJob;
import io.dataease.job.sechedule.ScheduleManager;
import io.dataease.license.config.XpackInteract;
import io.dataease.log.DeLog;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.manage.CoreUserManage;
import io.dataease.utils.*;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

import static io.dataease.datasource.server.DatasourceTaskServer.ScheduleType.MANUAL;
import static io.dataease.datasource.server.DatasourceTaskServer.ScheduleType.RIGHTNOW;


@RestController
@RequestMapping("/datasource")
@Transactional
public class DatasourceServer implements DatasourceApi {
    @Resource
    private CoreDatasourceMapper datasourceMapper;
    @Resource
    private EngineServer engineServer;
    @Resource
    private DatasourceTaskServer datasourceTaskServer;
    @Resource
    private CalciteProvider calciteProvider;
    @Resource
    private DatasourceSyncManage datasourceSyncManage;
    @Resource
    private TaskLogExtMapper taskLogExtMapper;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private DataSourceManage dataSourceManage;
    @Resource
    private QrtzSchedulerStateMapper qrtzSchedulerStateMapper;
    @Resource
    private DataSourceExtMapper dataSourceExtMapper;
    @Resource
    private CoreDsFinishPageMapper coreDsFinishPageMapper;
    @Resource
    private DatasetDataManage datasetDataManage;

    @Resource
    private ScheduleManager scheduleManager;
    @Resource
    private CoreUserManage coreUserManage;

    @Override
    public List<DatasourceDTO> query(String keyWord) {
        return null;
    }

    public enum UpdateType {
        all_scope, add_scope
    }

    @Resource
    private CommonThreadPool commonThreadPool;

    private boolean isUpdatingStatus = false;

    private void getParents(Long pid, List<Long> ids) {
        CoreDatasource parent = datasourceMapper.selectById(pid);// 查找父级folder
        ids.add(parent.getId());
        if (parent.getPid() != null && parent.getPid() != 0) {
            getParents(parent.getPid(), ids);
        }
    }

    public void move(DatasourceDTO dataSourceDTO) throws DEException {
        switch (dataSourceDTO.getAction()) {
            case "move" -> {
                if (dataSourceDTO.getPid() == null) {
                    DEException.throwException("目录必选！");
                }
                if (Objects.equals(dataSourceDTO.getId(), dataSourceDTO.getPid())) {
                    DEException.throwException(Translator.get("i18n_pid_not_eq_id"));
                }
                if (dataSourceDTO.getPid() != 0) {
                    List<Long> ids = new ArrayList<>();
                    getParents(dataSourceDTO.getPid(), ids);
                    if (ids.contains(dataSourceDTO.getId())) {
                        DEException.throwException(Translator.get("i18n_pid_not_eq_id"));
                    }
                }
                dataSourceManage.move(dataSourceDTO);
            }
            case "rename" -> {
                if (StringUtils.isEmpty(dataSourceDTO.getName())) {
                    DEException.throwException("名称不能为空！");
                }
                CoreDatasource datasource = datasourceMapper.selectById(dataSourceDTO.getId());
                datasource.setName(dataSourceDTO.getName());
                dataSourceManage.innerEdit(datasource);
            }
            case "create" -> {
                CoreDatasource coreDatasource = new CoreDatasource();
                BeanUtils.copyBean(coreDatasource, dataSourceDTO);
                coreDatasource.setCreateTime(System.currentTimeMillis());
                coreDatasource.setUpdateTime(System.currentTimeMillis());
                coreDatasource.setTaskStatus(TaskStatus.WaitingForExecution.name());
                coreDatasource.setType(dataSourceDTO.getNodeType());
                coreDatasource.setId(IDUtils.snowID());
                coreDatasource.setConfiguration("");
                dataSourceManage.innerSave(coreDatasource);
            }
            default -> {
            }
        }
    }

    private void filterDs(List<BusiNodeVO> busiNodeVOS, List<Long> ids, String type, Long id) {
        for (BusiNodeVO busiNodeVO : busiNodeVOS) {
            if (busiNodeVO.getType() != null && busiNodeVO.getType().equalsIgnoreCase(type)) {
                if (id != null) {
                    if (!busiNodeVO.getId().equals(id)) {
                        ids.add(busiNodeVO.getId());
                    }
                } else {
                    ids.add(busiNodeVO.getId());
                }
            }
            if (CollectionUtils.isNotEmpty(busiNodeVO.getChildren())) {
                filterDs(busiNodeVO.getChildren(), ids, type, id);
            }
        }
    }

    public boolean checkRepeat(@RequestBody DatasourceDTO dataSourceDTO) {
        if (Arrays.asList("API", "Excel", "folder").contains(dataSourceDTO.getType())) {
            return false;
        }
        BusiNodeRequest request = new BusiNodeRequest();
        request.setBusiFlag("datasource");
        List<BusiNodeVO> busiNodeVOS = dataSourceManage.tree(request);
        List<Long> ids = new ArrayList<>();
        filterDs(busiNodeVOS, ids, dataSourceDTO.getType(), dataSourceDTO.getId());

        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        QueryWrapper<CoreDatasource> wrapper = new QueryWrapper<>();
        wrapper.in("id", ids);

        List<CoreDatasource> datasources = datasourceMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(datasources)) {
            return false;
        }
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        DatasourceConfiguration configuration = JsonUtil.parseObject(dataSourceDTO.getConfiguration(), DatasourceConfiguration.class);
        boolean hasRepeat = false;
        for (CoreDatasource datasource : datasources) {
            if (Arrays.asList("API", "Excel", "folder").contains(datasource.getType())) {
                continue;
            }
            DatasourceConfiguration compare = JsonUtil.parseObject(datasource.getConfiguration(), DatasourceConfiguration.class);
            switch (dataSourceDTO.getType()) {
                case "sqlServer":
                case "db2":
                case "oracle":
                case "pg":
                case "redshift":
                    if (configuration.getHost().equalsIgnoreCase(compare.getHost()) && Objects.equals(configuration.getPort(), compare.getPort()) && configuration.getDataBase().equalsIgnoreCase(compare.getDataBase()) && configuration.getSchema().equalsIgnoreCase(compare.getSchema())) {
                        hasRepeat = true;
                    } else {
                        hasRepeat = false;
                    }
                    break;
                default:
                    if (configuration.getHost().equalsIgnoreCase(compare.getHost()) && Objects.equals(configuration.getPort(), compare.getPort()) && configuration.getDataBase().equalsIgnoreCase(compare.getDataBase())) {
                        hasRepeat = true;
                    } else {
                        hasRepeat = false;
                    }
                    break;
            }
        }
        return hasRepeat;
    }

    @Override
    public DatasourceDTO save(DatasourceDTO dataSourceDTO) throws DEException {
        if (StringUtils.isNotEmpty(dataSourceDTO.getAction())) {
            move(dataSourceDTO);
            return dataSourceDTO;
        }
        if (StringUtils.isNotEmpty(dataSourceDTO.getNodeType()) && dataSourceDTO.getNodeType().equalsIgnoreCase("folder")) {
            dataSourceDTO.setType("folder");
            dataSourceDTO.setConfiguration("");
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
        coreDatasource.setUpdateBy(AuthUtils.getUser().getUserId());
        dataSourceManage.innerSave(coreDatasource);

        if (dataSourceDTO.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name())) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            List<DatasetTableDTO> tables = ExcelUtils.getTables(datasourceRequest);
            for (DatasetTableDTO table : tables) {
                datasourceRequest.setTable(table.getTableName());
                List<TableField> tableFields = ExcelUtils.getTableFields(datasourceRequest);
                try {
                    datasourceSyncManage.createEngineTable(datasourceRequest.getTable(), tableFields);
                } catch (Exception e) {
                    DEException.throwException("Failed to create table " + datasourceRequest.getTable() + ", " + e.getMessage());
                }
            }
            commonThreadPool.addTask(() -> {
                datasourceSyncManage.extractExcelData(coreDatasource, "all_scope");
            });
        } else if (dataSourceDTO.getType().equals(DatasourceConfiguration.DatasourceType.API.name())) {
            CoreDatasourceTask coreDatasourceTask = new CoreDatasourceTask();
            BeanUtils.copyBean(coreDatasourceTask, dataSourceDTO.getSyncSetting());
            coreDatasourceTask.setName(coreDatasource.getName() + "-task");
            coreDatasourceTask.setDsId(coreDatasource.getId());
            if (coreDatasourceTask.getStartTime() == null) {
                coreDatasourceTask.setStartTime(System.currentTimeMillis() - 20 * 1000);
            }
            if (StringUtils.equalsIgnoreCase(coreDatasourceTask.getSyncRate(), RIGHTNOW.toString())) {
                coreDatasourceTask.setCron(null);
            } else {
                if (StringUtils.equalsIgnoreCase(coreDatasourceTask.getEndLimit(), "1") && coreDatasourceTask.getStartTime() > coreDatasourceTask.getEndTime()) {
                    DEException.throwException("结束时间不能小于开始时间！");
                }
            }
            coreDatasourceTask.setTaskStatus(TaskStatus.WaitingForExecution.name());
            datasourceTaskServer.insert(coreDatasourceTask);
            datasourceSyncManage.addSchedule(coreDatasourceTask);
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            List<DatasetTableDTO> tables = ApiUtils.getTables(datasourceRequest);
            checkName(tables.stream().map(DatasetTableDTO::getName).collect(Collectors.toList()));
            for (DatasetTableDTO api : tables) {
                datasourceRequest.setTable(api.getTableName());
                List<TableField> tableFields = ApiUtils.getTableFields(datasourceRequest);
                try {
                    datasourceSyncManage.createEngineTable(datasourceRequest.getTable(), tableFields);
                } catch (Exception e) {
                    DEException.throwException("Failed to create table " + datasourceRequest.getTable() + ": " + e.getMessage());
                }
            }
        } else {
            checkParams(dataSourceDTO.getConfiguration());
            calciteProvider.update(dataSourceDTO);
        }
        return dataSourceDTO;
    }

    private static void checkParams(String configurationStr) {
        DatasourceConfiguration configuration = JsonUtil.parseObject(configurationStr, DatasourceConfiguration.class);
        if (configuration.getInitialPoolSize() < configuration.getMinPoolSize()) {
            DEException.throwException("初始连接数不能小于最小连接数！");
        }
        if (configuration.getInitialPoolSize() > configuration.getMaxPoolSize()) {
            DEException.throwException("初始连接数不能大于最大连接数！");
        }
        if (configuration.getMaxPoolSize() < configuration.getMinPoolSize()) {
            DEException.throwException("最大连接数不能小于最小连接数！");
        }
        if (configuration.getQueryTimeout() < 0) {
            DEException.throwException("查询超时不能小于0！");
        }
    }

    private static void checkName(List<String> tables) {
        for (int i = 0; i < tables.size() - 1; i++) {
            for (int j = i + 1; j < tables.size(); j++) {
                if (tables.get(i).equalsIgnoreCase(tables.get(j))) {
                    DEException.throwException(Translator.get("i18n_table_name_repeat") + tables.get(i));
                }
            }
        }
    }

    public DatasourceDTO update(DatasourceDTO dataSourceDTO) throws DEException {
        Long pk = null;
        if (ObjectUtils.isEmpty(pk = dataSourceDTO.getId())) {
            return save(dataSourceDTO);
        }
        CoreDatasource sourceData = datasourceMapper.selectById(pk);
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        dataSourceDTO.setPid(sourceData.getPid());
        preCheckDs(dataSourceDTO);

        CoreDatasource requestDatasource = new CoreDatasource();
        BeanUtils.copyBean(requestDatasource, dataSourceDTO);
        requestDatasource.setUpdateTime(System.currentTimeMillis());
        requestDatasource.setUpdateBy(AuthUtils.getUser().getUserId());
        try {
            checkDatasourceStatus(requestDatasource);
        } catch (Exception ignore) {
        }

        DatasourceRequest sourceTableRequest = new DatasourceRequest();
        sourceTableRequest.setDatasource(sourceData);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(requestDatasource);
        List<String> toCreateTables = new ArrayList<>();
        List<String> toDeleteTables = new ArrayList<>();
        if (dataSourceDTO.getType().equals(DatasourceConfiguration.DatasourceType.API.name())) {
            List<String> sourceTables = ApiUtils.getTables(sourceTableRequest).stream().map(DatasetTableDTO::getTableName).collect(Collectors.toList());
            List<DatasetTableDTO> datasetTableDTOS = ApiUtils.getTables(datasourceRequest);
            List<String> tables = datasetTableDTOS.stream().map(DatasetTableDTO::getTableName).collect(Collectors.toList());
            checkName(datasetTableDTOS.stream().map(DatasetTableDTO::getName).collect(Collectors.toList()));
            toCreateTables = tables.stream().filter(table -> !sourceTables.contains(table)).collect(Collectors.toList());
            toDeleteTables = sourceTables.stream().filter(table -> !tables.contains(table)).collect(Collectors.toList());
            for (String table : tables) {
                for (String sourceTable : sourceTables) {
                    if (table.equals(sourceTable)) {
                        datasourceRequest.setTable(table);
                        List<String> tableFields = ApiUtils.getTableFields(datasourceRequest).stream().map(TableField::getName).sorted().collect(Collectors.toList());
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
            coreDatasourceTask.setName(requestDatasource.getName() + "-task");
            coreDatasourceTask.setDsId(requestDatasource.getId());
            if (StringUtils.equalsIgnoreCase(coreDatasourceTask.getSyncRate(), RIGHTNOW.toString())) {
                coreDatasourceTask.setStartTime(System.currentTimeMillis() - 20 * 1000);
                coreDatasourceTask.setCron(null);
            } else {
                if (StringUtils.equalsIgnoreCase(coreDatasourceTask.getEndLimit(), "1") && coreDatasourceTask.getStartTime() > coreDatasourceTask.getEndTime()) {
                    DEException.throwException("结束时间不能小于开始时间！");
                }
            }
            coreDatasourceTask.setTaskStatus(TaskStatus.WaitingForExecution.toString());
            datasourceTaskServer.update(coreDatasourceTask);
            for (String deleteTable : toDeleteTables) {
                try {
                    datasourceSyncManage.dropEngineTable(deleteTable);
                } catch (Exception e) {
                    DEException.throwException("Failed to drop table " + deleteTable);
                }
            }
            for (String toCreateTable : toCreateTables) {
                datasourceRequest.setTable(toCreateTable);
                try {
                    datasourceSyncManage.createEngineTable(toCreateTable, ApiUtils.getTableFields(datasourceRequest));
                } catch (Exception e) {
                    DEException.throwException("Failed to create table " + toCreateTable + ", " + e.getMessage());
                }
            }
            datasourceSyncManage.deleteSchedule(datasourceTaskServer.selectByDSId(dataSourceDTO.getId()));
            datasourceSyncManage.addSchedule(coreDatasourceTask);
            dataSourceManage.innerEdit(requestDatasource);
        } else if (dataSourceDTO.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name())) {
            List<String> sourceTables = ExcelUtils.getTables(sourceTableRequest).stream().map(DatasetTableDTO::getTableName).collect(Collectors.toList());
            List<String> tables = ExcelUtils.getTables(datasourceRequest).stream().map(DatasetTableDTO::getTableName).collect(Collectors.toList());
            if (dataSourceDTO.getEditType() == 0) {
                toCreateTables = tables;
                toDeleteTables = sourceTables;
                for (String deleteTable : toDeleteTables) {
                    try {
                        datasourceSyncManage.dropEngineTable(deleteTable);
                    } catch (Exception e) {
                        DEException.throwException("Failed to drop table " + deleteTable + ", " + e.getMessage());
                    }
                }
                for (String toCreateTable : toCreateTables) {
                    datasourceRequest.setTable(toCreateTable);
                    try {
                        datasourceSyncManage.createEngineTable(toCreateTable, ExcelUtils.getTableFields(datasourceRequest));
                    } catch (Exception e) {
                        DEException.throwException("Failed to create table " + toCreateTable + ", " + e.getMessage());
                    }
                }
                datasourceSyncManage.extractExcelData(requestDatasource, "all_scope");
                dataSourceManage.innerEdit(requestDatasource);
            } else {
                datasourceSyncManage.extractExcelData(requestDatasource, "add_scope");
                dataSourceManage.innerEdit(requestDatasource);
            }
        } else {
            checkParams(dataSourceDTO.getConfiguration());
            dataSourceManage.innerEdit(requestDatasource);
            calciteProvider.update(dataSourceDTO);
        }
        return dataSourceDTO;
    }

    private String excelDataTableName(String name) {
        return StringUtils.substring(name, 6, name.length() - 11);
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
        DatasourceDTO result = new DatasourceDTO();
        result.setStatus(coreDatasource.getStatus());
        return result;
    }

    @Override
    public List<String> getSchema(DatasourceDTO dataSourceDTO) throws DEException {
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(coreDatasource);
        return calciteProvider.getSchema(datasourceRequest);
    }


    @Override
    public DatasourceDTO get(Long datasourceId) throws DEException {
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        CoreDatasource datasource = datasourceMapper.selectById(datasourceId);
        BeanUtils.copyBean(datasourceDTO, datasource);
        TypeReference<List<ApiDefinition>> listTypeReference = new TypeReference<List<ApiDefinition>>() {
        };
        if (datasourceDTO.getType().equalsIgnoreCase(DatasourceConfiguration.DatasourceType.API.toString())) {
            List<ApiDefinition> apiDefinitionList = JsonUtil.parseList(datasourceDTO.getConfiguration(), listTypeReference);
            List<ApiDefinition> apiDefinitionListWithStatus = new ArrayList<>();
            int success = 0;
            for (ApiDefinition apiDefinition : apiDefinitionList) {
                String status = null;
                if (StringUtils.isNotEmpty(datasourceDTO.getStatus())) {
                    JsonNode jsonNode = null;
                    try {
                        jsonNode = objectMapper.readTree(datasourceDTO.getStatus());
                    } catch (Exception e) {
                        DEException.throwException(e);
                    }
                    for (JsonNode node : jsonNode) {
                        if (node.get("name").asText().equals(apiDefinition.getName())) {
                            status = node.get("status").asText();
                        }
                    }
                    apiDefinition.setStatus(status);
                }
                if (StringUtils.isNotEmpty(status) && status.equalsIgnoreCase("Success")) {
                    success++;
                }
                CoreDatasourceTaskLog log = datasourceTaskServer.lastSyncLogForTable(datasourceId, apiDefinition.getDeTableName());
                if (log != null) {
                    apiDefinition.setUpdateTime(log.getStartTime());
                }
                apiDefinitionListWithStatus.add(apiDefinition);
            }
            datasourceDTO.setApiConfigurationStr(new String(Base64.getEncoder().encode(Objects.requireNonNull(JsonUtil.toJSONString(apiDefinitionListWithStatus)).toString().getBytes())));
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

            CoreDatasourceTask task = datasourceTaskServer.selectByDSId(datasourceDTO.getId());
            if (task != null) {
                datasourceDTO.setLastSyncTime(task.getStartTime());
            }
        }
        if (datasourceDTO.getType().equalsIgnoreCase(DatasourceConfiguration.DatasourceType.Excel.toString())) {
            datasourceDTO.setFileName(ExcelUtils.getFileName(datasource));
            datasourceDTO.setSize(ExcelUtils.getSize(datasource));
        }
        datasourceDTO.setConfiguration(new String(Base64.getEncoder().encode(datasourceDTO.getConfiguration().getBytes())));

        datasourceDTO.setCreator(coreUserManage.getUserName(Long.valueOf(datasourceDTO.getCreateBy())));

        return datasourceDTO;
    }

    @DeLog(id = "#p0", ot = LogOT.DELETE, st = LogST.DATASOURCE)
    @Override
    @XpackInteract(value = "datasourceResourceTree", before = false)
    public void delete(Long datasourceId) throws DEException {
        Objects.requireNonNull(CommonBeanFactory.getBean(DatasourceServer.class)).recursionDel(datasourceId);
    }

    public void recursionDel(Long datasourceId) throws DEException {
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        if (ObjectUtils.isEmpty(coreDatasource)) {
            return;
        }
        if (coreDatasource.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name())) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            List<DatasetTableDTO> tables = ExcelUtils.getTables(datasourceRequest);
            for (DatasetTableDTO table : tables) {
                datasourceRequest.setTable(table.getTableName());
                try {
                    datasourceSyncManage.dropEngineTable(datasourceRequest.getTable());
                } catch (Exception e) {
                    DEException.throwException("Failed to drop table " + datasourceRequest.getTable());
                }
            }
        }
        if (coreDatasource.getType().equals(DatasourceConfiguration.DatasourceType.API.name())) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            List<DatasetTableDTO> tables = ApiUtils.getTables(datasourceRequest);
            for (DatasetTableDTO api : tables) {
                datasourceRequest.setTable(api.getTableName());
                try {
                    datasourceSyncManage.dropEngineTable(datasourceRequest.getTable());
                } catch (Exception e) {
                    DEException.throwException("Failed to drop table " + datasourceRequest.getTable());
                }

            }

            datasourceTaskServer.deleteByDSId(datasourceId);
        }

        datasourceMapper.deleteById(datasourceId);
        if (!Arrays.asList("API", "Excel", "folder").contains(coreDatasource.getType())) {
            calciteProvider.delete(coreDatasource);
        }

        if (coreDatasource.getType().equals(DatasourceConfiguration.DatasourceType.folder.name())) {
            QueryWrapper<CoreDatasource> wrapper = new QueryWrapper<>();
            wrapper.eq("pid", datasourceId);
            List<CoreDatasource> coreDatasources = datasourceMapper.selectList(wrapper);
            if (ObjectUtils.isNotEmpty(coreDatasources)) {
                for (CoreDatasource record : coreDatasources) {
                    delete(record.getId());
                }
            }
        }
    }


    @Override
    public DatasourceDTO validate(Long datasourceId) throws DEException {
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        return validate(coreDatasource);
    }

    public void addJob(List<CoreSysSetting> sysSettings) {
        String type = "minute";
        String interval = "30";
        for (CoreSysSetting sysSetting : sysSettings) {
            if (sysSetting.getPkey().equalsIgnoreCase("basic.dsExecuteTime")) {
                type = sysSetting.getPval();
            }
            if (sysSetting.getPkey().equalsIgnoreCase("basic.dsIntervalTime")) {
                interval = sysSetting.getPval();
            }
        }
        String cron = "";
        switch (type) {
            case "hour":
                cron = "0 0 0/hour *  * ? *".replace("hour", interval.toString());
                break;
            default:
                cron = "0 0/minute * *  * ? *".replace("minute", interval.toString());
        }
        scheduleManager.addOrUpdateCronJob(new JobKey("Datasource", "check_status"),
                new TriggerKey("Datasource", "check_status"),
                CheckDsStatusJob.class,
                cron, new Date(System.currentTimeMillis()), null, new JobDataMap());
    }

    private DatasourceDTO validate(CoreDatasource coreDatasource) {
        checkDatasourceStatus(coreDatasource);
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, coreDatasource);
        CoreDatasource record = new CoreDatasource();
        record.setStatus(coreDatasource.getStatus());
        QueryWrapper<CoreDatasource> wrapper = new QueryWrapper<>();
        wrapper.eq("id", coreDatasource.getId());
        CoreDatasource originData = datasourceMapper.selectById(coreDatasource.getId());
        String originStatus = originData.getStatus();
        if (StringUtils.equals(coreDatasource.getStatus(), originStatus)) {
            return datasourceDTO;
        }
        dataSourceManage.innerEditStatus(coreDatasource);
        return datasourceDTO;
    }

    private int getExtraFlag(Object typeObj, Object statusObj) {
        if (ObjectUtils.isNotEmpty(statusObj)) {
            String status = statusObj.toString();
            if (typeObj.toString().equalsIgnoreCase("API")) {
                TypeReference<List<ObjectNode>> listTypeReference = new TypeReference<List<ObjectNode>>() {
                };
                List<ObjectNode> apiStatus = JsonUtil.parseList(status, listTypeReference);
                boolean hasError = false;
                for (ObjectNode jsonNodes : apiStatus) {
                    if (jsonNodes.get("status") != null && jsonNodes.get("status").asText().equalsIgnoreCase("Error")) {
                        hasError = true;
                        break;
                    }
                }
                if (hasError) {
                    return -DataSourceType.valueOf(typeObj.toString()).getFlag();
                } else {
                    return DataSourceType.valueOf(typeObj.toString()).getFlag();
                }

            } else {
                if (StringUtils.equalsIgnoreCase(status, "Error")) {
                    return -DataSourceType.valueOf(typeObj.toString()).getFlag();
                } else {
                    return DataSourceType.valueOf(typeObj.toString()).getFlag();
                }
            }
        } else {
            return DataSourceType.valueOf(typeObj.toString()).getFlag();
        }
    }

    @Override
    public List<BusiNodeVO> tree(BusiNodeRequest request) throws DEException {
        return dataSourceManage.tree(request);
    }

    @Override
    public List<DatasetTableDTO> getTables(DatasetTableDTO datasetTableDTO) throws DEException {
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasetTableDTO.getDatasourceId());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(coreDatasource);
        if (coreDatasource.getType().equals("API")) {
            List<DatasetTableDTO> datasetTableDTOS = ApiUtils.getTables(datasourceRequest);
            datasetTableDTOS.forEach(datasetTableDTO1 -> {
                CoreDatasourceTaskLog log = datasourceTaskServer.lastSyncLogForTable(datasetTableDTO.getDatasourceId(), datasetTableDTO1.getTableName());
                if (log != null) {
                    datasetTableDTO1.setLastUpdateTime(log.getStartTime());
                    datasetTableDTO1.setStatus(log.getTaskStatus());
                }
            });
            return datasetTableDTOS;
        }
        if (coreDatasource.getType().equals("Excel")) {
            return ExcelUtils.getTables(datasourceRequest);
        }
        return calciteProvider.getTables(datasourceRequest);
    }

    @Override
    public List<TableField> getTableField(Map<String, String> req) throws DEException {
        String tableName = req.get("tableName");
        String datasourceId = req.get("datasourceId");
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

    @Override
    public void syncApiTable(Map<String, String> req) throws DEException {
        String tableName = req.get("tableName");
        String name = req.get("name");
        Long datasourceId = Long.valueOf(req.get("datasourceId"));
        datasourceSyncManage.extractDataForTable(datasourceId, name, tableName, datasourceTaskServer.selectByDSId(datasourceId).getUpdateType());
    }

    @Override
    public void syncApiDs(Map<String, String> req) throws Exception {
        Long datasourceId = Long.valueOf(req.get("datasourceId"));
        CoreDatasourceTask coreDatasourceTask = datasourceTaskServer.selectByDSId(datasourceId);
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        DatasourceServer.UpdateType updateType = DatasourceServer.UpdateType.valueOf(coreDatasourceTask.getUpdateType());
        datasourceSyncManage.extractedData(null, coreDatasource, updateType, MANUAL.toString());
    }

    public ExcelFileData excelUpload(@RequestParam("file") MultipartFile file, @RequestParam("id") long datasourceId, @RequestParam("editType") Integer editType) throws DEException {
        ExcelUtils excelUtils = new ExcelUtils();
        ExcelFileData excelFileData = excelUtils.excelSaveAndParse(file);
        if (editType == 1 || editType == 0) { //按照excel sheet 名称匹配
            CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
            if (coreDatasource != null) {
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(coreDatasource);
                List<DatasetTableDTO> datasetTableDTOS = ExcelUtils.getTables(datasourceRequest);
                List<ExcelSheetData> excelSheetDataList = new ArrayList<>();
                for (ExcelSheetData sheet : excelFileData.getSheets()) {
                    for (DatasetTableDTO datasetTableDTO : datasetTableDTOS) {
                        if (excelDataTableName(datasetTableDTO.getTableName()).equals(sheet.getTableName()) || isCsv(file.getOriginalFilename())) {
                            List<String> fieldNames = sheet.getFields().stream().map(TableField::getName).collect(Collectors.toList());
                            List<String> fieldTypes = sheet.getFields().stream().map(TableField::getFieldType).collect(Collectors.toList());
                            Collections.sort(fieldNames);
                            Collections.sort(fieldTypes);
                            datasourceRequest.setTable(datasetTableDTO.getTableName());
                            List<String> oldFieldNames = ExcelUtils.getTableFields(datasourceRequest).stream().map(TableField::getName).collect(Collectors.toList());
                            List<String> oldFieldTypes = ExcelUtils.getTableFields(datasourceRequest).stream().map(TableField::getFieldType).collect(Collectors.toList());
                            Collections.sort(oldFieldNames);
                            Collections.sort(oldFieldTypes);
                            if (fieldNames.equals(oldFieldNames) && fieldTypes.equals(oldFieldTypes)) {
                                sheet.setDeTableName(datasetTableDTO.getTableName());
                                excelSheetDataList.add(sheet);
                            }
                        }
                    }
                }
                if (CollectionUtils.isEmpty(excelSheetDataList) || excelSheetDataList.size() != datasetTableDTOS.size()) {
                    DEException.throwException("上传文件与源文件不一致，请检查文件!");
                }
                excelFileData.setSheets(excelSheetDataList);
            }
        }

        for (ExcelSheetData sheet : excelFileData.getSheets()) {
            for (int i = 0; i < sheet.getFields().size() - 1; i++) {
                for (int j = i + 1; j < sheet.getFields().size(); j++) {
                    if (sheet.getFields().get(i).getName().equalsIgnoreCase(sheet.getFields().get(j).getName())) {
                        DEException.throwException(sheet.getExcelLabel() + Translator.get("i18n_field_name_repeat") + sheet.getFields().get(i).getName());
                    }
                }
            }
        }
        return excelFileData;
    }

    private boolean isCsv(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        return suffix.equalsIgnoreCase("csv");
    }

    public ApiDefinition checkApiDatasource(Map<String, String> request) throws DEException {
        ApiDefinition apiDefinition = JsonUtil.parseObject(new String(java.util.Base64.getDecoder().decode(request.get("data"))), ApiDefinition.class);
        String response = ApiUtils.execHttpRequest(apiDefinition, 10);
        if (request.keySet().contains("type") && request.get("type").equals("apiStructure")) {
            apiDefinition.setShowApiStructure(true);
        }
        ApiUtils.checkApiDefinition(apiDefinition, response);
        if (apiDefinition.getRequest().getAuthManager() != null && StringUtils.isNotBlank(apiDefinition.getRequest().getAuthManager().getUsername()) && StringUtils.isNotBlank(apiDefinition.getRequest().getAuthManager().getPassword()) && apiDefinition.getRequest().getAuthManager().getVerification().equals("Basic Auth")) {
            apiDefinition.getRequest().getAuthManager().setUsername(new String(Base64.getEncoder().encode(apiDefinition.getRequest().getAuthManager().getUsername().getBytes())));
            apiDefinition.getRequest().getAuthManager().setPassword(new String(Base64.getEncoder().encode(apiDefinition.getRequest().getAuthManager().getPassword().getBytes())));
        }
        return apiDefinition;
    }

    private void preCheckDs(DatasourceDTO datasource) throws DEException {
        if (!datasourceTypes().stream().map(DatasourceConfiguration.DatasourceType::getType).toList().contains(datasource.getType())) {
            DEException.throwException("Datasource type not supported.");
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
    public Map<String, Object> previewDataWithLimit(Map<String, Object> req) {
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
    public List<String> latestUse() {
        List<String> types = new ArrayList<>();
        QueryWrapper<CoreDatasource> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("create_by", AuthUtils.getUser().getUserId());
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last(" limit 5");
        List<CoreDatasource> coreDatasources = datasourceMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(coreDatasources)) {
            return types;
        }
        for (CoreDatasource coreDatasource : coreDatasources) {
            if (!coreDatasource.getType().equalsIgnoreCase("folder") && !types.contains(coreDatasource.getType())) {
                types.add(coreDatasource.getType());
            }
        }
        return types;
    }

    public IPage<CoreDatasourceTaskLogDTO> listSyncRecord(int goPage, int pageSize, Long dsId) {


        QueryWrapper<CoreDatasourceTaskLogDTO> wrapper = new QueryWrapper<>();
        wrapper.eq("ds_id", dsId);
        wrapper.orderByDesc("start_time");
        Page<CoreDatasourceTaskLogDTO> page = new Page<>(goPage, pageSize);
        IPage<CoreDatasourceTaskLogDTO> pager = taskLogExtMapper.pager(page, wrapper);
        CoreDatasource coreDatasource = datasourceMapper.selectById(dsId);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(coreDatasource);
        List<DatasetTableDTO> datasetTableDTOS = ApiUtils.getTables(datasourceRequest);
        for (int i = 0; i < pager.getRecords().size(); i++) {
            for (int i1 = 0; i1 < datasetTableDTOS.size(); i1++) {
                if (pager.getRecords().get(i).getTableName().equalsIgnoreCase(datasetTableDTOS.get(i1).getTableName())) {
                    pager.getRecords().get(i).setName(datasetTableDTOS.get(i1).getName());
                }
            }
        }
        return pager;
    }


    public void updateDatasourceStatus() {
        QueryWrapper<CoreDatasource> wrapper = new QueryWrapper<>();
        wrapper.notIn("type", Arrays.asList("Excel", "folder"));
        List<CoreDatasource> datasources = datasourceMapper.selectList(wrapper);
        datasources.forEach(datasource -> {
            commonThreadPool.addTask(() -> {
                try {
                    validate(datasource);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public void updateStopJobStatus() {
        if (this.isUpdatingStatus) {
            return;
        } else {
            this.isUpdatingStatus = true;
        }

        try {
            doUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.isUpdatingStatus = false;
        }
    }

    private void doUpdate() {
        List<QrtzSchedulerState> qrtzSchedulerStates = qrtzSchedulerStateMapper.selectList(null);
        List<String> activeQrtzInstances = qrtzSchedulerStates.stream().filter(qrtzSchedulerState -> qrtzSchedulerState.getLastCheckinTime() + qrtzSchedulerState.getCheckinInterval() + 1000 > dataSourceExtMapper.selectTimestamp().getCurrentTimestamp() * 1000).map(QrtzSchedulerState::getInstanceName).collect(Collectors.toList());

        QueryWrapper<CoreDatasource> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("task_status", TaskStatus.UnderExecution.name());
        List<CoreDatasource> datasources = datasourceMapper.selectList(queryWrapper);

        List<CoreDatasource> syncCoreDatasources = new ArrayList<>();
        List<CoreDatasource> jobStoppedCoreDatasources = new ArrayList<>();
        datasources.forEach(coreDatasource -> {
            if (StringUtils.isNotEmpty(coreDatasource.getQrtzInstance()) && !activeQrtzInstances.contains(coreDatasource.getQrtzInstance().substring(0, coreDatasource.getQrtzInstance().length() - 13))) {
                jobStoppedCoreDatasources.add(coreDatasource);
            } else {
                syncCoreDatasources.add(coreDatasource);
            }
        });

        if (CollectionUtils.isEmpty(jobStoppedCoreDatasources)) {
            return;
        }

        queryWrapper.clear();
        queryWrapper.in("id", jobStoppedCoreDatasources.stream().map(CoreDatasource::getId).collect(Collectors.toList()));
        CoreDatasource record = new CoreDatasource();
        record.setTaskStatus(TaskStatus.WaitingForExecution.name());
        datasourceMapper.update(record, queryWrapper);
        //Task
        datasourceTaskServer.updateByDsIds(jobStoppedCoreDatasources.stream().map(CoreDatasource::getId).collect(Collectors.toList()));
    }

    public boolean showFinishPage() throws DEException {
        return coreDsFinishPageMapper.selectById(AuthUtils.getUser().getUserId()) == null;
    }


    public void setShowFinishPage() throws DEException {
        CoreDsFinishPage coreDsFinishPage = new CoreDsFinishPage();
        coreDsFinishPage.setId(AuthUtils.getUser().getUserId());
        coreDsFinishPageMapper.insert(coreDsFinishPage);
    }
}
