package io.dataease.datasource.server;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.api.dataset.dto.PreviewSqlDTO;
import io.dataease.api.ds.DatasourceApi;
import io.dataease.api.ds.vo.*;
import io.dataease.api.permissions.relation.api.RelationApi;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.constant.LogOT;
import io.dataease.constant.LogST;
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
import io.dataease.datasource.manage.EngineManage;
import io.dataease.datasource.provider.ApiUtils;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.datasource.provider.ExcelUtils;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.*;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.extensions.datasource.vo.XpackPluginsDatasourceVO;
import io.dataease.i18n.Translator;
import io.dataease.job.schedule.CheckDsStatusJob;
import io.dataease.job.schedule.ScheduleManager;
import io.dataease.license.config.XpackInteract;
import io.dataease.license.utils.LicenseUtil;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.stream.Collectors;

import static io.dataease.datasource.server.DatasourceTaskServer.ScheduleType.MANUAL;
import static io.dataease.datasource.server.DatasourceTaskServer.ScheduleType.RIGHTNOW;


@RestController
@RequestMapping("/datasource")
public class DatasourceServer implements DatasourceApi {
    @Resource
    private CoreDatasourceMapper datasourceMapper;
    @Resource
    private EngineManage engineManage;
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
    @Autowired(required = false)
    private PluginManageApi pluginManage;
    @Autowired(required = false)
    private RelationApi relationManage;

    public enum UpdateType {
        all_scope, add_scope
    }

    private TypeReference<List<ApiDefinition>> listTypeReference = new TypeReference<List<ApiDefinition>>() {
    };
    @Resource
    private CommonThreadPool commonThreadPool;
    private boolean isUpdatingStatus = false;
    private static List<Long> syncDsIds = new ArrayList<>();


    @Override
    public List<DatasourceDTO> query(String keyWord) {
        return null;
    }

    public boolean checkRepeat(@RequestBody BusiDsRequest dataSourceDTO) {
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

    @DeLog(id = "#p0.id", ot = LogOT.MODIFY, st = LogST.DATASOURCE)
    @Transactional
    public DatasourceDTO move(BusiCreateFolderRequest busiDsRequest) {
        DatasourceDTO dataSourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(dataSourceDTO, busiDsRequest);
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
        return dataSourceDTO;
    }

    @Transactional
    public DatasourceDTO reName(BusiRenameRequest busiDsRequest) {
        DatasourceDTO dataSourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(dataSourceDTO, busiDsRequest);
        if (StringUtils.isEmpty(dataSourceDTO.getName())) {
            DEException.throwException("名称不能为空！");
        }
        CoreDatasource datasource = datasourceMapper.selectById(dataSourceDTO.getId());
        datasource.setName(dataSourceDTO.getName());
        dataSourceDTO.setPid(datasource.getPid());
        dataSourceManage.checkName(dataSourceDTO);
        dataSourceManage.innerEdit(datasource);
        return dataSourceDTO;
    }

    @DeLog(id = "#p0.id", pid = "#p0.pid", ot = LogOT.CREATE, st = LogST.DATASOURCE)
    @Transactional
    public DatasourceDTO createFolder(BusiCreateFolderRequest busiDsRequest) {
        DatasourceDTO dataSourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(dataSourceDTO, busiDsRequest);
        dataSourceDTO.setCreateTime(System.currentTimeMillis());
        dataSourceDTO.setUpdateTime(System.currentTimeMillis());
        dataSourceDTO.setType(dataSourceDTO.getNodeType());
        dataSourceDTO.setId(IDUtils.snowID());
        dataSourceDTO.setConfiguration("");
        dataSourceManage.innerSave(dataSourceDTO);
        return dataSourceDTO;
    }

    @DeLog(id = "#p0.id", pid = "#p0.pid", ot = LogOT.CREATE, st = LogST.DATASOURCE)
    @Transactional
    @Override
    public DatasourceDTO save(BusiDsRequest busiDsRequest) throws DEException {
        DatasourceDTO dataSourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(dataSourceDTO, busiDsRequest);
        if (dataSourceDTO.getId() != null && dataSourceDTO.getId() > 0) {
            return update(busiDsRequest);
        }
        if (StringUtils.isNotEmpty(dataSourceDTO.getConfiguration())) {
            dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        }
        preCheckDs(dataSourceDTO);
        dataSourceDTO.setId(IDUtils.snowID());
        dataSourceDTO.setCreateTime(System.currentTimeMillis());
        dataSourceDTO.setUpdateTime(System.currentTimeMillis());
        try {
            checkDatasourceStatus(dataSourceDTO);
        } catch (Exception ignore) {
            dataSourceDTO.setStatus("Error");
        }
        dataSourceDTO.setTaskStatus(TaskStatus.WaitingForExecution.name());
        dataSourceDTO.setCreateBy(AuthUtils.getUser().getUserId().toString());
        dataSourceDTO.setUpdateBy(AuthUtils.getUser().getUserId());

        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        dataSourceManage.innerSave(dataSourceDTO);

        if (dataSourceDTO.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name())) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(dataSourceDTO);
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
            datasourceRequest.setDatasource(dataSourceDTO);
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

    @DeLog(id = "#p0.id", ot = LogOT.MODIFY, st = LogST.DATASOURCE)
    @Transactional
    @Override
    public DatasourceDTO update(BusiDsRequest busiDsRequest) throws DEException {
        DatasourceDTO dataSourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(dataSourceDTO, busiDsRequest);
        Long pk = null;
        if (ObjectUtils.isEmpty(pk = dataSourceDTO.getId())) {
            return save(busiDsRequest);
        }
        DatasourceDTO sourceData = dataSourceManage.getDs(pk);
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        dataSourceDTO.setPid(sourceData.getPid());
        preCheckDs(dataSourceDTO);

        dataSourceDTO.setUpdateTime(System.currentTimeMillis());
        dataSourceDTO.setUpdateBy(AuthUtils.getUser().getUserId());
        try {
            checkDatasourceStatus(dataSourceDTO);
        } catch (Exception e) {
            dataSourceDTO.setStatus("Error");
        }

        CoreDatasource requestDatasource = new CoreDatasource();
        BeanUtils.copyBean(requestDatasource, dataSourceDTO);

        DatasourceRequest sourceTableRequest = new DatasourceRequest();
        sourceTableRequest.setDatasource(sourceData);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dataSourceDTO);
        List<String> toCreateTables = new ArrayList<>();
        List<String> toDeleteTables = new ArrayList<>();
        if (dataSourceDTO.getType().equals(DatasourceConfiguration.DatasourceType.API.name())) {
            requestDatasource.setEnableDataFill(null);
            List<String> sourceTables = ApiUtils.getTables(sourceTableRequest).stream().map(DatasetTableDTO::getTableName).toList();
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
            dataSourceManage.checkName(dataSourceDTO);
            dataSourceManage.innerEdit(requestDatasource);
        } else if (dataSourceDTO.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name())) {
            requestDatasource.setEnableDataFill(null);
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
                dataSourceManage.checkName(dataSourceDTO);
                dataSourceManage.innerEdit(requestDatasource);
            } else {
                datasourceSyncManage.extractExcelData(requestDatasource, "add_scope");
                dataSourceManage.checkName(dataSourceDTO);
                dataSourceManage.innerEdit(requestDatasource);
            }
        } else {
            if (!LicenseUtil.licenseValid()) {
                requestDatasource.setEnableDataFill(null);
            }
            checkParams(dataSourceDTO.getConfiguration());
            dataSourceManage.checkName(dataSourceDTO);
            dataSourceManage.innerEdit(requestDatasource);
            calciteProvider.update(dataSourceDTO);
        }
        return dataSourceDTO;
    }


    @Override
    public List<DatasourceConfiguration.DatasourceType> datasourceTypes() {
        return Arrays.asList(DatasourceConfiguration.DatasourceType.values());
    }

    @Override
    public DatasourceDTO validate(BusiDsRequest busiDsRequest) throws DEException {
        DatasourceDTO dataSourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(dataSourceDTO, busiDsRequest);
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        checkDatasourceStatus(dataSourceDTO);
        DatasourceDTO result = new DatasourceDTO();
        result.setType(dataSourceDTO.getType());
        result.setStatus(dataSourceDTO.getStatus());
        return result;
    }

    @Override
    public List<String> getSchema(BusiDsRequest busiDsRequest) throws DEException {
        DatasourceDTO dataSourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(dataSourceDTO, busiDsRequest);
        dataSourceDTO.setConfiguration(new String(Base64.getDecoder().decode(dataSourceDTO.getConfiguration())));
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dataSourceDTO);
        Provider provider = ProviderFactory.getProvider(dataSourceDTO.getType());
        return provider.getSchema(datasourceRequest);
    }

    @Override
    public DatasourceDTO hidePw(Long datasourceId) throws DEException {
        return getDatasourceDTOById(datasourceId, true);
    }

    @Override
    public DatasourceDTO get(Long datasourceId) throws DEException {
        return getDatasourceDTOById(datasourceId, false);
    }

    @Override
    public DatasourceDTO innerGet(Long datasourceId) throws DEException {
        return getDatasourceDTOById(datasourceId, false);
    }

    @Override
    public List<DatasourceDTO> innerList(List<Long> ids, List<String> types) throws DEException {
        List<DatasourceDTO> list = new ArrayList<>();
        LambdaQueryWrapper<CoreDatasource> queryWrapper = new LambdaQueryWrapper<>();
        if (ids != null) {
            if (ids.isEmpty()) {
                return list;
            } else {
                queryWrapper.in(CoreDatasource::getId, ids);
            }
        }
        if (types != null) {
            if (types.isEmpty()) {
                return list;
            } else {
                queryWrapper.in(CoreDatasource::getType, types);
            }
        }
        List<CoreDatasource> dsList = datasourceMapper.selectList(queryWrapper);
        for (CoreDatasource datasource : dsList) {
            try {
                list.add(convertCoreDatasource(datasource.getId(), false, datasource));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public boolean perDelete(Long id) {
        if (LicenseUtil.licenseValid()) {
            try {
                relationManage.checkAuth();
            } catch (Exception e) {
                return false;
            }
            Long count = relationManage.getDsResource(id);
            if (count > 0) {
                return true;
            }
        }
        return false;
    }

    @Transactional
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
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, coreDatasource);
        if (coreDatasource.getType().equals(DatasourceConfiguration.DatasourceType.Excel.name())) {
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasourceDTO);
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
            datasourceRequest.setDatasource(datasourceDTO);
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
        CoreDatasource coreDatasource = new CoreDatasource();
        BeanUtils.copyBean(coreDatasource, datasourceMapper.selectById(datasourceId));
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

    @Override
    public List<BusiNodeVO> tree(BusiNodeRequest request) throws DEException {
        return dataSourceManage.tree(request);
    }

    @Override
    public List<DatasetTableDTO> getTables(DatasetTableDTO datasetTableDTO) throws DEException {
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasetTableDTO.getDatasourceId());
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, coreDatasource);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(datasourceDTO);
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
        Provider provider = ProviderFactory.getProvider(datasourceDTO.getType());
        return provider.getTables(datasourceRequest);
    }

    @Override
    public List<TableField> getTableField(Map<String, String> req) throws DEException {
        String tableName = req.get("tableName");
        String datasourceId = req.get("datasourceId");
        CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(transDTO(coreDatasource));
        if (coreDatasource.getType().equals("API") || coreDatasource.getType().equals("Excel")) {
            datasourceRequest.setDatasource(transDTO(engineManage.getDeEngine()));
            DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
            BeanUtils.copyBean(datasourceSchemaDTO, engineManage.getDeEngine());
            datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
            datasourceRequest.setDsList(Map.of(datasourceSchemaDTO.getId(), datasourceSchemaDTO));
            datasourceRequest.setQuery(TableUtils.tableName2Sql(datasourceSchemaDTO, tableName) + " LIMIT 0 OFFSET 0");
            datasourceRequest.setTable(tableName);
            Provider provider = ProviderFactory.getProvider(datasourceSchemaDTO.getType());
            List<TableField> tableFields = (List<TableField>) provider.fetchTableField(datasourceRequest);
            return tableFields.stream().filter(tableField -> {
                return !tableField.getOriginName().equalsIgnoreCase("dataease_uuid");
            }).collect(Collectors.toList());
        }

        DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
        BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
        datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, datasourceSchemaDTO.getId()));
        datasourceRequest.setDsList(Map.of(datasourceSchemaDTO.getId(), datasourceSchemaDTO));
        datasourceRequest.setQuery(TableUtils.tableName2Sql(datasourceSchemaDTO, tableName) + " LIMIT 0 OFFSET 0");
        datasourceRequest.setTable(tableName);
        Provider provider = ProviderFactory.getProvider(datasourceSchemaDTO.getType());
        return (List<TableField>) provider.fetchTableField(datasourceRequest);
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

    public static <T> List<T> deepCopy(List<T> originalList) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(originalList);
            oos.close();

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            List<T> newList = (List<T>) ois.readObject();
            ois.close();

            return newList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ExcelFileData excelUpload(@RequestParam("file") MultipartFile file, @RequestParam("id") long datasourceId, @RequestParam("editType") Integer editType) throws DEException {
        ExcelUtils excelUtils = new ExcelUtils();
        ExcelFileData excelFileData = excelUtils.excelSaveAndParse(file);
        if (editType == 1 || editType == 0) { //按照excel sheet 名称匹配
            CoreDatasource coreDatasource = datasourceMapper.selectById(datasourceId);
            if (coreDatasource != null) {
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(transDTO(coreDatasource));
                List<DatasetTableDTO> datasetTableDTOS = ExcelUtils.getTables(datasourceRequest);
                List<ExcelSheetData> excelSheetDataList = new ArrayList<>();
                for (ExcelSheetData sheet : excelFileData.getSheets()) {
                    for (DatasetTableDTO datasetTableDTO : datasetTableDTOS) {
                        if (excelDataTableName(datasetTableDTO.getTableName()).equals(sheet.getTableName()) || isCsv(file.getOriginalFilename())) {
                            List<TableField> newTableFields = deepCopy(sheet.getFields());
                            newTableFields.sort((o1, o2) -> {
                                return o1.getName().compareTo(o2.getName());
                            });
                            datasourceRequest.setTable(datasetTableDTO.getTableName());
                            List<TableField> oldTableFields = ExcelUtils.getTableFields(datasourceRequest);
                            oldTableFields.sort((o1, o2) -> {
                                return o1.getName().compareTo(o2.getName());
                            });

                            if (isEqual(newTableFields, oldTableFields)) {
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

    private boolean isEqual(List<TableField> newTableFields, List<TableField> oldTableFields) {
        boolean isEqual = true;
        if (CollectionUtils.isEmpty(newTableFields) || CollectionUtils.isEmpty(oldTableFields)) {
            isEqual = false;
        }
        for (int i = 0; i < newTableFields.size(); i++) {
            if (!newTableFields.get(i).getName().equals(oldTableFields.get(i).getName())) {
                isEqual = false;
                break;
            }
            if (!newTableFields.get(i).getFieldType().equals(oldTableFields.get(i).getFieldType())) {
                if (oldTableFields.get(i).getFieldType().equals("TEXT")) {
                    continue;
                }
                if (oldTableFields.get(i).getFieldType().equals("DOUBLE")) {
                    if (newTableFields.get(i).getFieldType().equals("LONG")) {
                        continue;
                    }
                }
                isEqual = false;
                break;
            }
        }

        return isEqual;

    }

    private boolean isCsv(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        return suffix.equalsIgnoreCase("csv");
    }

    public ApiDefinition checkApiDatasource(Map<String, String> request) throws DEException {

        ApiDefinition apiDefinition = JsonUtil.parseObject(new String(java.util.Base64.getDecoder().decode(request.get("data"))), ApiDefinition.class);
        List<ApiDefinition> paramsList = JsonUtil.parseList(new String(java.util.Base64.getDecoder().decode(request.get("paramsList"))), listTypeReference);
        String response = ApiUtils.execHttpRequest(apiDefinition, apiDefinition.getApiQueryTimeout() == null || apiDefinition.getApiQueryTimeout() <= 0 ? 10 : apiDefinition.getApiQueryTimeout(), paramsList);
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
        List<String> list = datasourceTypes().stream().map(DatasourceConfiguration.DatasourceType::getType).collect(Collectors.toList());
        if (LicenseUtil.licenseValid()) {
            List<XpackPluginsDatasourceVO> xpackPluginsDatasourceVOS = pluginManage.queryPluginDs();
            xpackPluginsDatasourceVOS.forEach(ele -> list.add(ele.getType()));
        }

        if (!list.contains(datasource.getType())) {
            DEException.throwException("Datasource type not supported.");
        }
    }

    public void checkDatasourceStatus(DatasourceDTO coreDatasource) {
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
                Provider provider = ProviderFactory.getProvider(coreDatasource.getType());
                status = provider.checkStatus(datasourceRequest);
            }
            coreDatasource.setStatus(status);
        } catch (Exception e) {
            DEException.throwException(e.getMessage());
        }
    }


    public void updateDemoDs() {
    }

    @Override
    public Map<String, Object> previewDataWithLimit(Map<String, Object> req) throws DEException {
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
        datasourceRequest.setDatasource(transDTO(coreDatasource));
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
            if (!syncDsIds.contains(datasource.getId())) {
                syncDsIds.add(datasource.getId());
            }
            commonThreadPool.addTask(() -> {
                try {
                    LicenseUtil.validate();
                    validate(datasource);
                } catch (Exception e) {
                } finally {
                    syncDsIds.removeIf(id -> id.equals(datasource.getId()));
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

    private DatasourceDTO transDTO(CoreDatasource record) {
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, record);
        return datasourceDTO;
    }

    private void getParents(Long pid, List<Long> ids) {
        CoreDatasource parent = datasourceMapper.selectById(pid);// 查找父级folder
        ids.add(parent.getId());
        if (parent.getPid() != null && parent.getPid() != 0) {
            getParents(parent.getPid(), ids);
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

    private String excelDataTableName(String name) {
        return StringUtils.substring(name, 6, name.length() - 11);
    }

    private DatasourceDTO getDatasourceDTOById(Long datasourceId, boolean hidePw) throws DEException {
        CoreDatasource datasource = datasourceMapper.selectById(datasourceId);
        if (datasource == null) {
            DEException.throwException("不存在的数据源！");
        }
        return convertCoreDatasource(datasourceId, hidePw, datasource);
    }

    private DatasourceDTO convertCoreDatasource(Long datasourceId, boolean hidePw, CoreDatasource datasource) {
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, datasource);

        if (datasourceDTO.getType().equalsIgnoreCase(DatasourceConfiguration.DatasourceType.API.toString())) {
            List<ApiDefinition> apiDefinitionList = JsonUtil.parseList(datasourceDTO.getConfiguration(), listTypeReference);
            List<ApiDefinition> apiDefinitionListWithStatus = new ArrayList<>();
            List<ApiDefinition> params = new ArrayList<>();
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


                if (StringUtils.isEmpty(apiDefinition.getType()) || apiDefinition.getType().equalsIgnoreCase("table")) {
                    apiDefinitionListWithStatus.add(apiDefinition);
                } else {
                    params.add(apiDefinition);
                }
            }
            datasourceDTO.setApiConfigurationStr(new String(Base64.getEncoder().encode(Objects.requireNonNull(JsonUtil.toJSONString(apiDefinitionListWithStatus)).toString().getBytes())));
            datasourceDTO.setParamsStr(new String(Base64.getEncoder().encode(Objects.requireNonNull(JsonUtil.toJSONString(params)).toString().getBytes())));
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
        } else {
            if (hidePw) {
                Provider provider = ProviderFactory.getProvider(datasourceDTO.getType());
                provider.hidePW(datasourceDTO);
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

    private DatasourceDTO validate(CoreDatasource coreDatasource) {
        DatasourceDTO datasourceDTO = new DatasourceDTO();
        BeanUtils.copyBean(datasourceDTO, coreDatasource);
        try {
            checkDatasourceStatus(datasourceDTO);
            if (!Arrays.asList("API", "Excel", "folder").contains(coreDatasource.getType())) {
                calciteProvider.updateDsPoolAfterCheckStatus(datasourceDTO);
            }
        } catch (Exception e) {
            coreDatasource.setStatus("Error");
            DEException.throwException(e.getMessage());
        } finally {
            coreDatasource.setStatus(datasourceDTO.getStatus());
            dataSourceManage.innerEditStatus(coreDatasource);
        }
        datasourceDTO.setConfiguration("");
        return datasourceDTO;
    }

}
