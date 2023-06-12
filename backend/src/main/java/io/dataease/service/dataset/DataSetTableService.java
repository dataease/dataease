package io.dataease.service.dataset;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import io.dataease.auth.annotation.DeCleaner;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.commons.constants.*;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.*;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.request.dataset.DataSetExportRequest;
import io.dataease.controller.request.dataset.DataSetGroupRequest;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.controller.request.dataset.DataSetTaskRequest;
import io.dataease.controller.response.DataSetDetail;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.base.ConditionEntity;
import io.dataease.dto.SysLogDTO;
import io.dataease.dto.dataset.*;
import io.dataease.dto.dataset.union.UnionDTO;
import io.dataease.dto.dataset.union.UnionItemDTO;
import io.dataease.dto.dataset.union.UnionParamDTO;
import io.dataease.exception.DataEaseException;
import io.dataease.ext.ExtDataSetGroupMapper;
import io.dataease.ext.ExtDataSetTableMapper;
import io.dataease.ext.UtilMapper;
import io.dataease.i18n.Translator;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.*;
import io.dataease.plugins.common.constants.DatasetType;
import io.dataease.plugins.common.constants.DatasourceTypes;
import io.dataease.plugins.common.constants.DeTypeConstants;
import io.dataease.plugins.common.dto.dataset.SqlVariableDetails;
import io.dataease.plugins.common.dto.datasource.DataSourceType;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.common.request.permission.DataSetRowPermissionsTreeDTO;
import io.dataease.plugins.common.request.permission.DatasetRowPermissionsTreeObj;
import io.dataease.plugins.datasource.provider.Provider;
import io.dataease.plugins.datasource.query.QueryProvider;
import io.dataease.plugins.loader.ClassloaderResponsity;
import io.dataease.plugins.xpack.auth.dto.request.ColumnPermissionItem;
import io.dataease.provider.DDLProvider;
import io.dataease.provider.ProviderFactory;
import io.dataease.provider.datasource.JdbcProvider;
import io.dataease.service.chart.util.ChartDataBuild;
import io.dataease.service.datasource.DatasourceService;
import io.dataease.service.engine.EngineService;
import io.dataease.service.sys.SysAuthService;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @Author gin
 * @Date 2021/2/23 2:54 下午
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DataSetTableService {
    @Resource
    private DatasetTableMapper datasetTableMapper;
    @Resource
    private DatasourceMapper datasourceMapper;
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;
    @Resource
    private DataSetTableTaskService dataSetTableTaskService;
    @Resource
    private CommonThreadPool commonThreadPool;
    @Resource
    private ExtractDataService extractDataService;
    @Resource
    private ExtDataSetTableMapper extDataSetTableMapper;
    @Resource
    private DatasetTableIncrementalConfigMapper datasetTableIncrementalConfigMapper;
    @Resource
    private DataSetTableUnionService dataSetTableUnionService;
    @Resource
    private QrtzSchedulerStateMapper qrtzSchedulerStateMapper;
    @Resource
    private DatasetTableTaskLogMapper datasetTableTaskLogMapper;
    @Resource
    private ExtDataSetGroupMapper extDataSetGroupMapper;
    @Resource
    private DatasetTableFieldMapper datasetTableFieldMapper;
    @Resource
    private PermissionService permissionService;
    @Resource
    private EngineService engineService;
    @Resource
    private SysAuthService sysAuthService;
    @Resource
    private ChartViewMapper chartViewMapper;
    @Resource
    private DataSetTableTaskLogService dataSetTableTaskLogService;
    @Resource
    private PermissionsTreeService permissionsTreeService;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private DatasetSqlLogMapper datasetSqlLogMapper;

    private static boolean isUpdatingDatasetTableStatus = false;
    private static final String lastUpdateTime = "${__last_update_time__}";
    private static final String currentUpdateTime = "${__current_update_time__}";
    public static final String regex = "\\$\\{(.*?)\\}";
    private static final String SubstitutedParams = "DATAEASE_PATAMS_BI";
    private static final String SubstitutedSql = " 'BI' = 'BI' ";
    private static final String SubstitutedSqlVirtualData = " 1 > 2 ";

    @Value("${upload.file.path}")
    private String path;

    private static final Logger logger = LoggerFactory.getLogger(ClassloaderResponsity.class);

    @DeCleaner(value = DePermissionType.DATASET, key = "sceneId")
    public List<DatasetTable> batchInsert(List<DataSetTableRequest> datasetTable) throws Exception {
        // 保存之前校验table名称
        checkNames(datasetTable);
        List<DatasetTable> list = new ArrayList<>();
        for (DataSetTableRequest table : datasetTable) {
            list.add(save(table));
            // 清理权限缓存
            CacheUtils.removeAll(AuthConstants.USER_DATASET_NAME);
            CacheUtils.removeAll(AuthConstants.ROLE_DATASET_NAME);
            CacheUtils.removeAll(AuthConstants.DEPT_DATASET_NAME);
        }
        return list;
    }

    private void extractData(DataSetTableRequest datasetTable) throws Exception {
        if (datasetTable.getMode() == 1 && StringUtils.isNotEmpty(datasetTable.getSyncType())
                && datasetTable.getSyncType().equalsIgnoreCase("sync_now")) {
            DataSetTaskRequest dataSetTaskRequest = new DataSetTaskRequest();
            DatasetTableTask datasetTableTask = new DatasetTableTask();
            datasetTableTask.setTableId(datasetTable.getId());
            datasetTableTask.setRate(ScheduleType.SIMPLE.toString());
            datasetTableTask.setType("all_scope");
            datasetTableTask.setName(datasetTable.getName() + " 更新设置-" + System.currentTimeMillis());
            datasetTableTask.setEnd("0");
            datasetTableTask.setStatus(TaskStatus.Underway.name());
            datasetTableTask.setStartTime(System.currentTimeMillis());
            dataSetTaskRequest.setDatasetTableTask(datasetTableTask);
            dataSetTableTaskService.save(dataSetTaskRequest);
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @DeCleaner(value = DePermissionType.DATASET, key = "sceneId")
    public List<DatasetTable> saveExcel(DataSetTableRequest datasetTable) {
        List<String> datasetIdList = new ArrayList<>();

        if (StringUtils.isEmpty(datasetTable.getId())) {
            List<DatasetTable> list = new ArrayList<>();
            if (datasetTable.isMergeSheet()) {
                Map<String, List<ExcelSheetData>> map = datasetTable.getSheets().stream()
                        .collect(Collectors.groupingBy(ExcelSheetData::getFieldsMd5));
                for (String s : map.keySet()) {
                    DataSetTableRequest sheetTable = new DataSetTableRequest();
                    BeanUtils.copyBean(sheetTable, datasetTable);
                    sheetTable.setId(UUID.randomUUID().toString());
                    sheetTable.setCreateBy(AuthUtils.getUser().getUsername());
                    sheetTable.setCreateTime(System.currentTimeMillis());
                    List<ExcelSheetData> excelSheetDataList = map.get(s);
                    sheetTable.setName(excelSheetDataList.get(0).getDatasetName());
                    checkName(sheetTable);
                    excelSheetDataList.forEach(excelSheetData -> {
                        String[] fieldArray = excelSheetData.getFields().stream().map(TableField::getFieldName)
                                .toArray(String[]::new);
                        if (checkIsRepeat(fieldArray)) {
                            DataEaseException.throwException(Translator.get("i18n_excel_field_repeat"));
                        }
                        excelSheetData.setData(null);
                        excelSheetData.setJsonArray(null);
                    });
                }
                for (String s : map.keySet()) {
                    DataSetTableRequest sheetTable = new DataSetTableRequest();
                    BeanUtils.copyBean(sheetTable, datasetTable);
                    sheetTable.setId(UUID.randomUUID().toString());
                    sheetTable.setCreateBy(AuthUtils.getUser().getUsername());
                    sheetTable.setCreateTime(System.currentTimeMillis());
                    List<ExcelSheetData> excelSheetDataList = map.get(s);
                    sheetTable.setName(excelSheetDataList.get(0).getDatasetName());
                    excelSheetDataList.forEach(excelSheetData -> {
                        excelSheetData.setData(null);
                        excelSheetData.setJsonArray(null);
                    });
                    DataTableInfoDTO info = new DataTableInfoDTO();
                    info.setExcelSheetDataList(excelSheetDataList);
                    sheetTable.setInfo(new Gson().toJson(info));
                    datasetTableMapper.insert(sheetTable);
                    sysAuthService.copyAuth(sheetTable.getId(), SysAuthConstants.AUTH_SOURCE_TYPE_DATASET);
                    saveExcelTableField(sheetTable.getId(), excelSheetDataList.get(0).getFields(), true);
                    datasetIdList.add(sheetTable.getId());
                    list.add(sheetTable);
                    DeLogUtils.save(SysLogConstants.OPERATE_TYPE.CREATE, SysLogConstants.SOURCE_TYPE.DATASET, datasetTable.getId(), datasetTable.getSceneId(), null, null);
                }
                datasetIdList.forEach(datasetId -> commonThreadPool.addTask(() ->
                        extractDataService.extractExcelData(datasetId, "all_scope", "初始导入",
                                null, datasetIdList)));
            } else {
                for (ExcelSheetData sheet : datasetTable.getSheets()) {
                    String[] fieldArray = sheet.getFields().stream().map(TableField::getFieldName)
                            .toArray(String[]::new);
                    if (checkIsRepeat(fieldArray)) {
                        DataEaseException.throwException(Translator.get("i18n_excel_field_repeat"));
                    }
                }

                for (ExcelSheetData sheet : datasetTable.getSheets()) {
                    DataSetTableRequest sheetTable = new DataSetTableRequest();
                    BeanUtils.copyBean(sheetTable, datasetTable);
                    sheetTable.setId(UUID.randomUUID().toString());
                    sheetTable.setCreateBy(AuthUtils.getUser().getUsername());
                    sheetTable.setCreateTime(System.currentTimeMillis());
                    sheetTable.setName(sheet.getDatasetName());
                    checkName(sheetTable);
                    sheet.setData(null);
                    sheet.setJsonArray(null);
                    List<ExcelSheetData> excelSheetDataList = new ArrayList<>();
                    excelSheetDataList.add(sheet);
                    DataTableInfoDTO info = new DataTableInfoDTO();
                    info.setExcelSheetDataList(excelSheetDataList);
                    sheetTable.setInfo(new Gson().toJson(info));
                    datasetTableMapper.insert(sheetTable);
                    sysAuthService.copyAuth(sheetTable.getId(), SysAuthConstants.AUTH_SOURCE_TYPE_DATASET);
                    saveExcelTableField(sheetTable.getId(), sheet.getFields(), true);
                    datasetIdList.add(sheetTable.getId());
                    list.add(sheetTable);
                    DeLogUtils.save(SysLogConstants.OPERATE_TYPE.MODIFY, SysLogConstants.SOURCE_TYPE.DATASET, datasetTable.getId(), datasetTable.getSceneId(), null, null);
                }
                datasetIdList.forEach(datasetId -> commonThreadPool.addTask(() ->
                        extractDataService.extractExcelData(datasetId, "all_scope", "初始导入",
                                null, datasetIdList)));
            }

            return list;
        }

        List<ExcelSheetData> excelSheetDataList = new ArrayList<>();
        List<String> oldFields = datasetTable.getSheets().get(0).getFields().stream().map(TableField::getRemarks)
                .collect(Collectors.toList());
        for (ExcelSheetData sheet : datasetTable.getSheets()) {
            // 替换时，
            if (datasetTable.getEditType() == 0) {
                List<String> newFields = sheet.getFields().stream().map(TableField::getRemarks)
                        .collect(Collectors.toList());
                if (!oldFields.equals(newFields)) {
                    DataEaseException.throwException(Translator.get("i18n_excel_column_inconsistent"));
                }
                oldFields = newFields;
            }

            String[] fieldArray = sheet.getFields().stream().map(TableField::getFieldName).toArray(String[]::new);
            if (checkIsRepeat(fieldArray)) {
                DataEaseException.throwException(Translator.get("i18n_excel_field_repeat"));
            }
            sheet.setData(null);
            sheet.setJsonArray(null);
            excelSheetDataList.add(sheet);
        }
        DataTableInfoDTO info = new DataTableInfoDTO();
        info.setExcelSheetDataList(excelSheetDataList);
        datasetTable.setInfo(new Gson().toJson(info));
        datasetTableMapper.updateByPrimaryKeySelective(datasetTable);
        // 替換時，先不刪除旧字段；同步成功后再删除
        if (datasetTable.getEditType() == 0) {
            commonThreadPool.addTask(() -> extractDataService.extractExcelData(datasetTable.getId(), "all_scope", "替换",
                    saveExcelTableField(datasetTable.getId(), datasetTable.getSheets().get(0).getFields(), false),
                    Collections.singletonList(datasetTable.getId())));
        } else if (datasetTable.getEditType() == 1) {
            commonThreadPool.addTask(() -> extractDataService.extractExcelData(datasetTable.getId(), "add_scope", "追加",
                    null, Collections.singletonList(datasetTable.getId())));
        }
        DeLogUtils.save(SysLogConstants.OPERATE_TYPE.MODIFY, SysLogConstants.SOURCE_TYPE.DATASET, datasetTable.getId(), datasetTable.getSceneId(), null, null);
        return Collections.singletonList(datasetTable);
    }

    @DeCleaner(value = DePermissionType.DATASET, key = "sceneId")
    public DatasetTable save(DataSetTableRequest datasetTable) throws Exception {
        if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.SQL.name()) && !"appApply".equalsIgnoreCase(datasetTable.getOptFrom())) {
            DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
            BeanUtils.copyBean(dataSetTableRequest, datasetTable);
            getSQLPreview(dataSetTableRequest, false);
        }
        checkName(datasetTable);
        if (StringUtils.isEmpty(datasetTable.getId())) {
            datasetTable.setId(UUID.randomUUID().toString());
            datasetTable.setCreateBy(AuthUtils.getUser().getUsername());
            datasetTable.setCreateTime(System.currentTimeMillis());
            int insert = datasetTableMapper.insert(datasetTable);


            // 清理权限缓存
            CacheUtils.removeAll(AuthConstants.USER_PERMISSION_CACHE_NAME);
            sysAuthService.copyAuth(datasetTable.getId(), SysAuthConstants.AUTH_SOURCE_TYPE_DATASET);

            // 添加表成功后，获取当前表字段和类型，抽象到dataease数据库
            if (insert == 1) {
                if (datasetTable.getOptFrom() == null || !"appApply".equalsIgnoreCase(datasetTable.getOptFrom())) {
                    saveTableField(datasetTable);
                }
                extractData(datasetTable);
                DeLogUtils.save(SysLogConstants.OPERATE_TYPE.CREATE, SysLogConstants.SOURCE_TYPE.DATASET, datasetTable.getId(), datasetTable.getSceneId(), null, null);
            }
        } else {
            int update = datasetTableMapper.updateByPrimaryKeySelective(datasetTable);
            if (datasetTable.getIsRename() == null || !datasetTable.getIsRename()) {
                // 更新数据和字段
                if (update == 1) {
                    if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.SQL.name())
                            || StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.CUSTOM.name())
                            || StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.UNION.name())) {
                        saveTableField(datasetTable);
                    }
                    extractData(datasetTable);
                    DeLogUtils.save(SysLogConstants.OPERATE_TYPE.MODIFY, SysLogConstants.SOURCE_TYPE.DATASET, datasetTable.getId(), datasetTable.getSceneId(), null, null);
                }
            }
        }
        return datasetTable;
    }

    public void alter(DataSetTableRequest request) {
        checkName(request);
        datasetTableMapper.updateByPrimaryKeySelective(request);
    }

    public void delete(String id) {
        DatasetTable table = datasetTableMapper.selectByPrimaryKey(id);
        SysLogDTO sysLogDTO = DeLogUtils.buildLog(SysLogConstants.OPERATE_TYPE.DELETE, SysLogConstants.SOURCE_TYPE.DATASET, table.getId(), table.getSceneId(), null, null);
        datasetTableMapper.deleteByPrimaryKey(id);
        dataSetTableFieldsService.deleteByTableId(id);
        // 删除同步任务
        dataSetTableTaskService.deleteByTableId(id);
        // 删除关联关系
        dataSetTableUnionService.deleteUnionByTableId(id);
        try {
            // 抽取的数据集删除doris
            if (table.getMode() == 1) {
                deleteDorisTable(id, table);
            }
            DeLogUtils.save(sysLogDTO);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteDorisTable(String datasetId, DatasetTable table) throws Exception {
        String dorisTableName = TableUtils.tableName(datasetId);
        Datasource dorisDatasource = engineService.getDeEngine();
        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(dorisDatasource);
        DDLProvider ddlProvider = ProviderFactory.getDDLProvider(dorisDatasource.getType());
        if (StringUtils.equalsIgnoreCase(DatasetType.CUSTOM.name(), table.getType())
                || StringUtils.equalsIgnoreCase(DatasetType.UNION.name(), table.getType())) {
            datasourceRequest.setQuery(ddlProvider.dropView(dorisTableName));
            jdbcProvider.exec(datasourceRequest);
            datasourceRequest.setQuery(ddlProvider.dropView(TableUtils.tmpName(dorisTableName)));
            jdbcProvider.exec(datasourceRequest);
        } else {
            datasourceRequest.setQuery(ddlProvider.dropTable(dorisTableName));
            jdbcProvider.exec(datasourceRequest);
            datasourceRequest.setQuery(ddlProvider.dropTable(TableUtils.tmpName(dorisTableName)));
            jdbcProvider.exec(datasourceRequest);
        }
    }

    public List<String> getDatasetNameFromGroup(String sceneId) {
        DatasetTableExample example = new DatasetTableExample();
        example.createCriteria().andSceneIdEqualTo(sceneId);
        List<DatasetTable> datasetTables = datasetTableMapper.selectByExample(example);
        return datasetTables.stream().map(DatasetTable::getName).collect(Collectors.toList());
    }

    public List<DataSetTableDTO> list(DataSetTableRequest dataSetTableRequest) {
        dataSetTableRequest.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        dataSetTableRequest.setTypeFilter(dataSetTableRequest.getTypeFilter());
        return extDataSetTableMapper.search(dataSetTableRequest);
    }

    public List<DatasetTable> list(List<String> datasetIds) {
        DatasetTableExample example = new DatasetTableExample();
        example.createCriteria().andIdIn(datasetIds);
        return datasetTableMapper.selectByExample(example);
    }

    public List<DataSetTableDTO> listAndGroup(DataSetTableRequest dataSetTableRequest) {
        dataSetTableRequest.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        dataSetTableRequest.setTypeFilter(dataSetTableRequest.getTypeFilter());
        List<DataSetTableDTO> ds = extDataSetTableMapper.search(dataSetTableRequest);
        ds.forEach(ele -> ele.setIsLeaf(true));
        // 获取group下的子group
        DataSetGroupRequest datasetGroup = new DataSetGroupRequest();
        datasetGroup.setLevel(null);
        datasetGroup.setType("group");
        datasetGroup.setPid(dataSetTableRequest.getSceneId());
        datasetGroup.setUserId(String.valueOf(AuthUtils.getUser().getUserId()));
        datasetGroup.setSort("name asc,create_time desc");
        List<DataSetGroupDTO> groups = extDataSetGroupMapper.search(datasetGroup);
        List<DataSetTableDTO> group = groups.stream().map(ele -> {
            DataSetTableDTO dto = new DataSetTableDTO();
            BeanUtils.copyBean(dto, ele);
            dto.setIsLeaf(false);
            dto.setType("group");
            return dto;
        }).collect(Collectors.toList());
        group.addAll(ds);
        return group;
    }

    public List<DataSetTableDTO> search(DataSetTableRequest dataSetTableRequest) {
        String userId = String.valueOf(AuthUtils.getUser().getUserId());
        dataSetTableRequest.setUserId(userId);
        dataSetTableRequest.setSort("name asc");
        List<DataSetTableDTO> ds = extDataSetTableMapper.search(dataSetTableRequest);
        if (CollectionUtils.isEmpty(ds)) {
            return ds;
        }

        TreeSet<String> ids = new TreeSet<>();
        ds.forEach(ele -> {
            ele.setIsLeaf(true);
            ele.setPid(ele.getSceneId());
            ids.add(ele.getPid());
        });

        List<DataSetTableDTO> group = new ArrayList<>();
        DataSetGroupRequest dataSetGroupRequest = new DataSetGroupRequest();
        dataSetGroupRequest.setUserId(userId);
        dataSetGroupRequest.setIds(ids);
        List<DataSetGroupDTO> search = extDataSetGroupMapper.search(dataSetGroupRequest);
        while (CollectionUtils.isNotEmpty(search)) {
            ids.clear();
            search.forEach(ele -> {
                DataSetTableDTO dto = new DataSetTableDTO();
                BeanUtils.copyBean(dto, ele);
                dto.setIsLeaf(false);
                dto.setType("group");
                group.add(dto);
                ids.add(ele.getPid());
            });
            dataSetGroupRequest.setIds(ids);
            search = extDataSetGroupMapper.search(dataSetGroupRequest);
        }

        List<DataSetTableDTO> res = new ArrayList<>();
        Map<String, DataSetTableDTO> map = new TreeMap<>();
        group.forEach(ele -> map.put(ele.getId(), ele));
        for (Map.Entry<String, DataSetTableDTO> stringDataSetTableDTOEntry : map.entrySet()) {
            res.add(stringDataSetTableDTOEntry.getValue());
        }
        res.sort(Comparator.comparing(DatasetTable::getName));
        res.addAll(ds);
        return res;
    }

    public DatasetTable get(String id) {
        return datasetTableMapper.selectByPrimaryKey(id);
    }

    public DataSetTableDTO getWithPermission(String id, Long user) {
        CurrentUserDto currentUserDto = AuthUtils.getUser();
        Long userId = user != null ? user : currentUserDto.getUserId();
        DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
        dataSetTableRequest.setId(id);
        dataSetTableRequest.setUserId(String.valueOf(userId));
        dataSetTableRequest.setTypeFilter(dataSetTableRequest.getTypeFilter());
        return extDataSetTableMapper.searchOne(dataSetTableRequest);
    }

    public List<TableField> getFields(DatasetTable datasetTable) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasetTable.getDataSourceId());
        Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceRequest.setTable(new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getTable());
        return datasourceProvider.getTableFields(datasourceRequest);
    }

    public Map<String, List<DatasetTableField>> getFieldsFromDE(DataSetTableRequest dataSetTableRequest) {
        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(dataSetTableRequest.getId());
        datasetTableField.setChecked(Boolean.TRUE);
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
        fields = permissionService.filterColumnPermissions(fields, new HashMap<>(), dataSetTableRequest.getId(), null);
        List<DatasetTableField> dimension = new ArrayList<>();
        List<DatasetTableField> quota = new ArrayList<>();

        fields.forEach(field -> {
            if (StringUtils.equalsIgnoreCase("q", field.getGroupType())) {
                quota.add(field);
            } else {
                dimension.add(field);
            }
        });
        // quota add count
        DatasetTableField count = DatasetTableField.builder()
                .id("count")
                .tableId(dataSetTableRequest.getId())
                .originName("*")
                .name(Translator.get("i18n_chart_count"))
                .dataeaseName("*")
                .type("INT")
                .checked(true)
                .columnIndex(999)
                .deType(2)
                .extField(1)
                .groupType("q")
                .build();
        quota.add(count);

        Map<String, List<DatasetTableField>> map = new HashMap<>();
        map.put("dimension", dimension);
        map.put("quota", quota);

        return map;
    }

    public Map<String, Object> getPreviewData(DataSetTableRequest dataSetTableRequest, Integer page, Integer pageSize,
                                              List<DatasetTableField> extFields, DatasetRowPermissionsTreeObj extTree) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String syncStatus = "";
        DatasetTableField datasetTableField = DatasetTableField.builder().tableId(dataSetTableRequest.getId())
                .checked(Boolean.TRUE).build();
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
        if (CollectionUtils.isNotEmpty(extFields)) {
            fields = extFields;
        }
        if (CollectionUtils.isEmpty(fields)) {
            map.put("fields", fields);
            map.put("data", new ArrayList<>());
            map.put("page", new DataSetPreviewPage());
            return map;
        }
        DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(dataSetTableRequest.getId());
        // 行权限
        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = permissionsTreeService.getRowPermissionsTree(fields, datasetTable, null);
        // ext filter
        if (extTree != null) {
            DataSetRowPermissionsTreeDTO dto = new DataSetRowPermissionsTreeDTO();
            dto.setTree(extTree);
            rowPermissionsTree.add(dto);
        }
        // 列权限
        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        fields = permissionService.filterColumnPermissions(fields, desensitizationList, datasetTable.getId(), null);
        if (CollectionUtils.isEmpty(fields)) {
            map.put("fields", fields);
            map.put("data", new ArrayList<>());
            map.put("page", new DataSetPreviewPage());
            return map;
        }

        String[] fieldArray = fields.stream().map(DatasetTableField::getDataeaseName).toArray(String[]::new);

        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);

        List<String[]> data = new ArrayList<>();
        DataSetPreviewPage dataSetPreviewPage = new DataSetPreviewPage();
        dataSetPreviewPage.setShow(Integer.valueOf(dataSetTableRequest.getRow()));
        dataSetPreviewPage.setPage(page);
        dataSetPreviewPage.setPageSize(pageSize);
        int realSize = Integer.parseInt(dataSetTableRequest.getRow()) < pageSize
                ? Integer.parseInt(dataSetTableRequest.getRow())
                : pageSize;
        if (page == Integer.parseInt(dataSetTableRequest.getRow()) / pageSize + 1) {
            realSize = Integer.parseInt(dataSetTableRequest.getRow()) % pageSize;
        }
        if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.DB.name()) || StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.API.name())) {
            if (datasetTable.getMode() == 0) {
                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
                if (ObjectUtils.isEmpty(ds)) {
                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
                }
                if (StringUtils.isNotEmpty(ds.getStatus()) && ds.getStatus().equalsIgnoreCase("Error")) {
                    throw new Exception(Translator.get("i18n_invalid_ds"));
                }
                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = dataTableInfoDTO.getTable();
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());

                datasourceRequest.setQuery(
                        qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));

                map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
                datasourceRequest.setPage(page);
                datasourceRequest.setFetchSize(Integer.parseInt(dataSetTableRequest.getRow()));
                datasourceRequest.setPageSize(pageSize);
                datasourceRequest.setRealSize(realSize);
                datasourceRequest.setPreviewData(true);
                try {
                    datasourceRequest.setPageable(true);
                    data.addAll(datasourceProvider.getData(datasourceRequest));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }

                try {
                    datasourceRequest.setQuery(qp.createQueryTableWithLimit(table, fields,
                            Integer.valueOf(dataSetTableRequest.getRow()), false, ds, null, rowPermissionsTree));
                    datasourceRequest.setPageable(false);
                    dataSetPreviewPage.setTotal(datasourceProvider.getData(datasourceRequest).size());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
            } else {
                // check doris table
                if (!checkEngineTableIsExists(dataSetTableRequest.getId())) {
                    if (dataSetTableRequest.isPreviewForTask()) {
                        map.put("fields", fields);
                        map.put("data", new ArrayList<>());
                        map.put("page", new DataSetPreviewPage());
                        return map;
                    } else {
                        throw new RuntimeException(Translator.get("i18n_data_not_sync"));
                    }
                }
                Datasource ds = engineService.getDeEngine();
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = TableUtils.tableName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(
                        qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
                map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
                try {
                    data.addAll(jdbcProvider.getData(datasourceRequest));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
                try {
                    datasourceRequest.setQuery(qp.createQueryTableWithLimit(table, fields,
                            Integer.valueOf(dataSetTableRequest.getRow()), false, ds, null, rowPermissionsTree));
                    dataSetPreviewPage.setTotal(jdbcProvider.getData(datasourceRequest).size());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
            }

        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.SQL.name())) {
            if (datasetTable.getMode() == 0) {
                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
                if (ObjectUtils.isEmpty(ds)) {
                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
                }
                if (StringUtils.isNotEmpty(ds.getStatus()) && ds.getStatus().equalsIgnoreCase("Error")) {
                    throw new Exception(Translator.get("i18n_invalid_ds"));
                }
                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                DataTableInfoDTO dataTableInfo = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
                String sql = dataTableInfo.isBase64Encryption() ? new String(java.util.Base64.getDecoder().decode(dataTableInfo.getSql())) : dataTableInfo.getSql();
                sql = handleVariableDefaultValue(sql, datasetTable.getSqlVariableDetails(), ds.getType(), false);
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(
                        qp.createQuerySQLWithPage(sql, fields, page, pageSize, realSize, false, null, rowPermissionsTree));
                map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
                datasourceRequest.setPage(page);
                datasourceRequest.setFetchSize(Integer.parseInt(dataSetTableRequest.getRow()));
                datasourceRequest.setPageSize(pageSize);
                datasourceRequest.setRealSize(realSize);
                datasourceRequest.setPreviewData(true);
                try {
                    datasourceRequest.setPageable(true);
                    datasourceRequest.setPermissionFields(fields);
                    data.addAll(datasourceProvider.getData(datasourceRequest));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
                try {
                    datasourceRequest.setPageable(false);
                    datasourceRequest.setQuery(qp.createQuerySqlWithLimit(sql, fields,
                            Integer.valueOf(dataSetTableRequest.getRow()), false, null, rowPermissionsTree));
                    dataSetPreviewPage.setTotal(datasourceProvider.getData(datasourceRequest).size());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
            } else {
                // check doris table
                if (!checkEngineTableIsExists(dataSetTableRequest.getId())) {
                    throw new RuntimeException(Translator.get("i18n_data_not_sync"));
                }
                Datasource ds = engineService.getDeEngine();
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = TableUtils.tableName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(
                        qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
                map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
                try {
                    data.addAll(jdbcProvider.getData(datasourceRequest));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
                try {
                    datasourceRequest.setQuery(qp.createQueryTableWithLimit(table, fields,
                            Integer.valueOf(dataSetTableRequest.getRow()), false, ds, null, rowPermissionsTree));
                    dataSetPreviewPage.setTotal(jdbcProvider.getData(datasourceRequest).size());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
            }
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "excel")) {
            if (!checkEngineTableIsExists(dataSetTableRequest.getId())) {
                throw new RuntimeException(Translator.get("i18n_data_not_sync"));
            }

            Datasource ds = engineService.getDeEngine();
            JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            String table = TableUtils.tableName(dataSetTableRequest.getId());
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            datasourceRequest.setQuery(
                    qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
            map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
            try {
                data.addAll(jdbcProvider.getData(datasourceRequest));
            } catch (Exception e) {
                logger.error(e.getMessage());
                DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
            }
            try {
                datasourceRequest.setQuery(qp.createQueryTableWithLimit(table, fields,
                        Integer.valueOf(dataSetTableRequest.getRow()), false, ds, null, rowPermissionsTree));
                dataSetPreviewPage.setTotal(jdbcProvider.getData(datasourceRequest).size());
            } catch (Exception e) {
                logger.error(e.getMessage());
                DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
            }
            BaseGridRequest request = new BaseGridRequest();
            ConditionEntity entity2 = new ConditionEntity();
            entity2.setField("dataset_table_task_log.table_id");
            entity2.setOperator("eq");
            entity2.setValue(dataSetTableRequest.getId());
            List<ConditionEntity> conditionEntities = new ArrayList<>();
            conditionEntities.add(entity2);
            request.setConditions(conditionEntities);
            List<DataSetTaskLogDTO> dataSetTaskLogDTOS = dataSetTableTaskLogService.listTaskLog(request, "excel");
            if (CollectionUtils.isNotEmpty(dataSetTaskLogDTOS)) {
                dataSetTaskLogDTOS.get(0).getStatus().equalsIgnoreCase(JobStatus.Underway.name());
                syncStatus = dataSetTaskLogDTOS.get(0).getStatus();
            }

        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "custom")) {
            if (datasetTable.getMode() == 0) {
                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
                if (ObjectUtils.isEmpty(ds)) {
                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
                }
                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);

                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
                List<DataSetTableUnionDTO> list = dataSetTableUnionService
                        .listByTableId(dt.getList().get(0).getTableId());

                String sql = "";
                try {
                    sql = getCustomSQLDatasource(dt, list, ds);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(
                        qp.createQuerySQLWithPage(sql, fields, page, pageSize, realSize, false, null, rowPermissionsTree));
                map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
                datasourceRequest.setPage(page);
                datasourceRequest.setFetchSize(Integer.parseInt(dataSetTableRequest.getRow()));
                datasourceRequest.setPageSize(pageSize);
                datasourceRequest.setRealSize(realSize);
                datasourceRequest.setPreviewData(true);
                try {
                    datasourceRequest.setPageable(true);
                    data.addAll(datasourceProvider.getData(datasourceRequest));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
                try {
                    datasourceRequest.setPageable(false);
                    datasourceRequest.setQuery(qp.createQuerySqlWithLimit(sql, fields,
                            Integer.valueOf(dataSetTableRequest.getRow()), false, null, rowPermissionsTree));
                    dataSetPreviewPage.setTotal(datasourceProvider.getData(datasourceRequest).size());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
            } else {
                Datasource ds = engineService.getDeEngine();
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = TableUtils.tableName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(
                        qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
                map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
                try {
                    data.addAll(jdbcProvider.getData(datasourceRequest));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }

                try {
                    datasourceRequest.setQuery(qp.createQueryTableWithLimit(table, fields,
                            Integer.valueOf(dataSetTableRequest.getRow()), false, ds, null, rowPermissionsTree));
                    dataSetPreviewPage.setTotal(jdbcProvider.getData(datasourceRequest).size());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
            }
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "union")) {
            if (datasetTable.getMode() == 0) {
                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
                if (ObjectUtils.isEmpty(ds)) {
                    DEException.throwException(Translator.get("i18n_datasource_delete"));
                }
                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);

                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);

                String sql = "";
                try {
                    sql = (String) getUnionSQLDatasource(dt, ds).get("sql");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(
                        qp.createQuerySQLWithPage(sql, fields, page, pageSize, realSize, false, null, rowPermissionsTree));
                map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
                datasourceRequest.setPage(page);
                datasourceRequest.setFetchSize(Integer.parseInt(dataSetTableRequest.getRow()));
                datasourceRequest.setPageSize(pageSize);
                datasourceRequest.setRealSize(realSize);
                datasourceRequest.setPreviewData(true);
                try {
                    datasourceRequest.setPageable(true);
                    data.addAll(datasourceProvider.getData(datasourceRequest));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
                try {
                    datasourceRequest.setPageable(false);
                    datasourceRequest.setQuery(qp.createQuerySqlWithLimit(sql, fields,
                            Integer.valueOf(dataSetTableRequest.getRow()), false, null, rowPermissionsTree));
                    dataSetPreviewPage.setTotal(datasourceProvider.getData(datasourceRequest).size());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
            } else {
                Datasource ds = engineService.getDeEngine();
                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                String table = TableUtils.tableName(dataSetTableRequest.getId());
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(
                        qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
                map.put("sql", java.util.Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
                try {
                    data.addAll(jdbcProvider.getData(datasourceRequest));
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }

                try {
                    datasourceRequest.setQuery(qp.createQueryTableWithLimit(table, fields,
                            Integer.valueOf(dataSetTableRequest.getRow()), false, ds, null, rowPermissionsTree));
                    dataSetPreviewPage.setTotal(jdbcProvider.getData(datasourceRequest).size());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    DEException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
                }
            }
        }

        List<Map<String, Object>> jsonArray = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(data)) {
            jsonArray = data.stream().map(ele -> {
                Map<String, Object> tmpMap = new HashMap<>();
                for (int i = 0; i < ele.length; i++) {
                    if (desensitizationList.keySet().contains(fieldArray[i])) {
                        tmpMap.put(fieldArray[i], ChartDataBuild.desensitizationValue(desensitizationList.get(fieldArray[i]), String.valueOf(ele[i])));
                    } else {
                        tmpMap.put(fieldArray[i], ele[i]);
                    }
                }
                return tmpMap;
            }).collect(Collectors.toList());
        }

        if (!map.containsKey("status")) {
            map.put("status", "success");
        }
        map.put("fields", fields);
        map.put("data", jsonArray);
        map.put("page", dataSetPreviewPage);
        map.put("syncStatus", syncStatus);

        return map;
    }

    public List<SqlVariableDetails> datasetParams(String type, String id) {
        if (!Arrays.asList("DATE", "TEXT", "NUM").contains(type)) {
            return new ArrayList<>();
        }
        DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(id);
        if (datasetTable != null) {
            return getSqlVariableDetails(type, Arrays.asList(datasetTable));
        } else {
            return null;
        }
    }

    private List<SqlVariableDetails> getSqlVariableDetails(String type, List<DatasetTable> datasetTables) {
        List<SqlVariableDetails> sqlVariableDetails = new ArrayList<>();
        for (DatasetTable datasetTable : datasetTables) {
            if (StringUtils.isNotEmpty(datasetTable.getSqlVariableDetails())) {
                List<SqlVariableDetails> sqlVariables = new Gson().fromJson(datasetTable.getSqlVariableDetails(), new TypeToken<List<SqlVariableDetails>>() {
                }.getType());
                for (SqlVariableDetails sqlVariable : sqlVariables) {
                    sqlVariable.setId(datasetTable.getId() + "|DE|" + sqlVariable.getVariableName());
                    sqlVariable.setVariableName("[" + datasetTable.getName() + "]-" + sqlVariable.getVariableName());
                    sqlVariableDetails.add(sqlVariable);
                }
            }
        }

        switch (type) {
            case "DATE":
                sqlVariableDetails = sqlVariableDetails.stream().filter(item -> item.getType().get(0).contains("DATETIME")).collect(Collectors.toList());
                sqlVariableDetails.forEach(item -> {
                    if (item.getType().size() > 1) {
                        item.setAlias(item.getVariableName() + "[" + item.getType().get(1) + "]");
                    } else {
                        item.setAlias(item.getVariableName());
                    }
                });
                break;
            case "TEXT":
                sqlVariableDetails = sqlVariableDetails.stream().filter(item -> item.getType().get(0).contains("TEXT")).collect(Collectors.toList());
                sqlVariableDetails.forEach(item -> item.setAlias(item.getVariableName()));
                break;
            case "NUM":
                sqlVariableDetails = sqlVariableDetails.stream().filter(item -> item.getType().get(0).contains("LONG") || item.getType().get(0).contains("DOUBLE")).collect(Collectors.toList());
                sqlVariableDetails.forEach(item -> item.setAlias(item.getVariableName()));
                break;
        }
        return sqlVariableDetails;
    }

    public List<SqlVariableDetails> paramsWithIds(String type, List<String> viewIds) {
        if (CollectionUtils.isEmpty(viewIds)) {
            return new ArrayList<>();
        }

        if (!Arrays.asList("DATE", "TEXT", "NUM").contains(type)) {
            return new ArrayList<>();
        }
        ChartViewExample chartViewExample = new ChartViewExample();
        chartViewExample.createCriteria().andIdIn(viewIds);
        List<String> datasetIds = chartViewMapper.selectByExample(chartViewExample).stream().map(ChartView::getTableId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(datasetIds)) {
            return new ArrayList<>();
        }
        DatasetTableExample datasetTableExample = new DatasetTableExample();
        datasetTableExample.createCriteria().andIdIn(datasetIds);
        List<DatasetTable> datasetTables = datasetTableMapper.selectByExample(datasetTableExample);
        if (CollectionUtils.isEmpty(datasetTables)) {
            return new ArrayList<>();
        }
        return getSqlVariableDetails(type, datasetTables);
    }


    public void checkVariable(final String sql, String dsType) throws Exception {
        String tmpSql = removeVariables(sql, dsType);
        if (tmpSql.contains(SubstitutedParams)) {
            throw new Exception(Translator.get("I18N_SQL_variable_limit"));
        }
    }

    public String handleVariableDefaultValue(String sql, String sqlVariableDetails, String dsType, boolean isEdit) {
        if (StringUtils.isEmpty(sql)) {
            DataEaseException.throwException(Translator.get("i18n_sql_not_empty"));
        }
        if (sqlVariableDetails != null) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(sql);
            while (matcher.find()) {
                SqlVariableDetails defaultsSqlVariableDetail = null;
                List<SqlVariableDetails> defaultsSqlVariableDetails = new Gson().fromJson(sqlVariableDetails, new TypeToken<List<SqlVariableDetails>>() {
                }.getType());
                for (SqlVariableDetails sqlVariableDetail : defaultsSqlVariableDetails) {
                    if (matcher.group().substring(2, matcher.group().length() - 1).equalsIgnoreCase(sqlVariableDetail.getVariableName())) {
                        defaultsSqlVariableDetail = sqlVariableDetail;
                        break;
                    }
                }
                if (!isEdit && defaultsSqlVariableDetail != null && defaultsSqlVariableDetail.getDefaultValueScope() != null &&
                        defaultsSqlVariableDetail.getDefaultValueScope().equals(SqlVariableDetails.DefaultValueScope.ALLSCOPE) && StringUtils.isNotEmpty(defaultsSqlVariableDetail.getDefaultValue())) {
                    sql = sql.replace(matcher.group(), defaultsSqlVariableDetail.getDefaultValue());
                }
                if (isEdit && defaultsSqlVariableDetail != null && StringUtils.isNotEmpty(defaultsSqlVariableDetail.getDefaultValue())) {
                    sql = sql.replace(matcher.group(), defaultsSqlVariableDetail.getDefaultValue());
                }
            }
        }

        try {
            sql = removeVariables(sql, dsType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sql;
    }

    public String removeVariables(final String sql, String dsType) throws Exception {
        String tmpSql = sql;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(tmpSql);
        boolean hasVariables = false;
        while (matcher.find()) {
            hasVariables = true;
            tmpSql = tmpSql.replace(matcher.group(), SubstitutedParams);
        }
        if (!hasVariables && !tmpSql.contains(SubstitutedParams)) {
            return tmpSql;
        }

        Statement statement = CCJSqlParserUtil.parse(tmpSql);
        Select select = (Select) statement;

        if (select.getSelectBody() instanceof PlainSelect) {
            return handlePlainSelect((PlainSelect) select.getSelectBody(), select, dsType);
        } else {
            StringBuilder result = new StringBuilder();
            SetOperationList setOperationList = (SetOperationList) select.getSelectBody();
            for (int i = 0; i < setOperationList.getSelects().size(); i++) {
                result.append(handlePlainSelect((PlainSelect) setOperationList.getSelects().get(i), null, dsType));
                if (i < setOperationList.getSelects().size() - 1) {
                    result.append(" ").append(setOperationList.getOperations().get(i).toString()).append(" ");
                }
            }
            return result.toString();
        }
    }

    private String handlePlainSelect(PlainSelect plainSelect, Select statementSelect, String dsType) throws Exception {
        FromItem fromItem = plainSelect.getFromItem();
        if (fromItem instanceof SubSelect) {
            SelectBody selectBody = ((SubSelect) fromItem).getSelectBody();
            SubSelect subSelect = new SubSelect();
            Select subSelectTmp = (Select) CCJSqlParserUtil.parse(removeVariables(selectBody.toString(), dsType));
            subSelect.setSelectBody(subSelectTmp.getSelectBody());
            if (dsType.equals(DatasourceTypes.oracle.getType())) {
                subSelect.setAlias(new Alias(fromItem.getAlias().toString(), false));
            } else {
                if (fromItem.getAlias() == null) {
                    throw new Exception("Failed to parse sql, Every derived table must have its own alias！");
                }
                subSelect.setAlias(new Alias(fromItem.getAlias().toString(), false));
            }
            plainSelect.setFromItem(subSelect);
        }
        List<Join> joins = plainSelect.getJoins();
        if (joins != null) {
            List<Join> joinsList = new ArrayList<>();
            for (Join join : joins) {
                FromItem rightItem = join.getRightItem();
                if (rightItem instanceof SubSelect) {
                    SelectBody selectBody = ((SubSelect) rightItem).getSelectBody();
                    SubSelect subSelect = new SubSelect();
                    Select subSelectTmp = (Select) CCJSqlParserUtil.parse(removeVariables(selectBody.toString(), dsType));
                    PlainSelect subPlainSelect = ((PlainSelect) subSelectTmp.getSelectBody());
                    subSelect.setSelectBody(subPlainSelect);
                    if (dsType.equals(DatasourceTypes.oracle.getType())) {
                        subSelect.setAlias(new Alias(rightItem.getAlias().toString(), false));
                    } else {
                        if (rightItem.getAlias() == null) {
                            throw new Exception("Failed to parse sql, Every derived table must have its own alias！");
                        }
                        subSelect.setAlias(new Alias(rightItem.getAlias().toString(), false));
                    }
                    join.setRightItem(subSelect);
                }
                joinsList.add(join);
            }
            plainSelect.setJoins(joinsList);
        }
        Expression expr = plainSelect.getWhere();
        if (expr == null) {
            return handleWith(plainSelect, statementSelect, dsType);
        }
        StringBuilder stringBuilder = new StringBuilder();
        BinaryExpression binaryExpression = null;
        try {
            binaryExpression = (BinaryExpression) expr;
        } catch (Exception e) {
        }
        if (binaryExpression != null) {
            if (!(binaryExpression.getLeftExpression() instanceof BinaryExpression) && !(binaryExpression.getLeftExpression() instanceof InExpression) && hasVariable(binaryExpression.getRightExpression().toString())) {
                stringBuilder.append(SubstitutedSql);
            } else {
                expr.accept(getExpressionDeParser(stringBuilder));
            }
        } else {
            expr.accept(getExpressionDeParser(stringBuilder));
        }
        plainSelect.setWhere(CCJSqlParserUtil.parseCondExpression(stringBuilder.toString()));
        return handleWith(plainSelect, statementSelect, dsType);
    }

    private String handleWith(PlainSelect plainSelect, Select select, String dsType) throws Exception {
        StringBuilder builder = new StringBuilder();
        if (select != null && CollectionUtils.isNotEmpty(select.getWithItemsList())) {
            builder.append("WITH");
            builder.append(" ");
            for (Iterator<WithItem> iter = select.getWithItemsList().iterator(); iter.hasNext(); ) {
                WithItem withItem = iter.next();
                builder.append(withItem.getName()).append(" AS ( ").append(removeVariables(withItem.getSubSelect().toString(), dsType)).append(" ) ");
                if (iter.hasNext()) {
                    builder.append(",");
                }
            }
        }

        builder.append(" ").append(plainSelect);
        return builder.toString();
    }

    public Map<String, Object> getDBPreview(DataSetTableRequest dataSetTableRequest) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
        if (ds == null) {
            throw new Exception(Translator.get("i18n_invalid_ds"));
        }
        Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        DataTableInfoDTO dataTableInfo = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
        QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
        String sqlAsTable = qp.createSQLPreview(qp.sqlForPreview(dataTableInfo.getTable(), ds), null);
        datasourceRequest.setQuery(sqlAsTable);
        datasourceRequest.setTable(dataTableInfo.getTable());
        Map<String, List> result = datasourceProvider.fetchResultAndField(datasourceRequest);
        List<String[]> data = result.get("dataList");
        List<TableField> fields = result.get("fieldList");
        String[] fieldArray = fields.stream().map(TableField::getFieldName).toArray(String[]::new);
        if (checkIsRepeat(fieldArray)) {
            DataEaseException.throwException(Translator.get("i18n_excel_field_repeat"));
        }
        List<Map<String, Object>> jsonArray = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(data)) {
            jsonArray = data.stream().map(ele -> {
                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < ele.length; i++) {
                    map.put(fieldArray[i], ele[i]);
                }
                return map;
            }).collect(Collectors.toList());
        }

        Map<String, Object> map = new HashMap<>();
        map.put("fields", fields);
        map.put("data", jsonArray);

        return map;
    }

    public List<DatasetSqlLog> getSQLLog(DataSetTableRequest dataSetTableRequest) {
        if (StringUtils.isEmpty(dataSetTableRequest.getId())) {
            return new ArrayList<>();
        }
        DatasetSqlLogExample example = new DatasetSqlLogExample();
        example.createCriteria().andDatasetIdEqualTo(dataSetTableRequest.getId());
        example.setOrderByClause(" start_time desc ");
        return datasetSqlLogMapper.selectByExample(example);
    }

    public ResultHolder getSQLPreview(DataSetTableRequest dataSetTableRequest, boolean realData) throws Exception {
        DatasetSqlLog datasetSqlLog = new DatasetSqlLog();

        DataTableInfoDTO dataTableInfo = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
        String sql = dataTableInfo.isBase64Encryption() ? new String(java.util.Base64.getDecoder().decode(dataTableInfo.getSql())) : dataTableInfo.getSql();
        datasetSqlLog.setSql(sql);
        Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
        if (ds == null) {
            throw new Exception(Translator.get("i18n_invalid_ds"));
        }
        String tmpSql = removeVariables(sql, ds.getType());
        if (!realData) {
            tmpSql.replaceAll(SubstitutedSql, SubstitutedSqlVirtualData);
        }
        if (dataSetTableRequest.getMode() == 1 && (tmpSql.contains(SubstitutedParams) || tmpSql.contains(SubstitutedSql.trim()))) {
            throw new Exception(Translator.get("I18N_SQL_variable_direct_limit"));
        }
        if (tmpSql.contains(SubstitutedParams)) {
            throw new Exception(Translator.get("I18N_SQL_variable_limit"));
        }
        Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);

        sql = realData ? handleVariableDefaultValue(sql, dataSetTableRequest.getSqlVariableDetails(), ds.getType(), true) : removeVariables(sql, ds.getType()).replaceAll(SubstitutedSql.trim(), SubstitutedSqlVirtualData);
        if (StringUtils.isEmpty(sql)) {
            DataEaseException.throwException(Translator.get("i18n_sql_not_empty"));
        }
        checkVariable(sql, ds.getType());
        QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
        String sqlAsTable = qp.createSQLPreview(sql, null);
        datasourceRequest.setQuery(sqlAsTable);
        Map<String, List> result;
        try {
            datasetSqlLog.setStartTime(System.currentTimeMillis());
            result = datasourceProvider.fetchResultAndField(datasourceRequest);
            datasetSqlLog.setEndTime(System.currentTimeMillis());
            datasetSqlLog.setSpend(datasetSqlLog.getEndTime() - datasetSqlLog.getStartTime());
            datasetSqlLog.setStatus("Completed");
        } catch (Exception e) {
            datasetSqlLog.setStatus("Error");
            return ResultHolder.error(e.getMessage(), datasetSqlLog);
        } finally {
            if (StringUtils.isNotEmpty(dataSetTableRequest.getId())) {
                datasetSqlLog.setDatasetId(dataSetTableRequest.getId());
                datasetSqlLog.setId(UUID.randomUUID().toString());
                datasetSqlLogMapper.insert(datasetSqlLog);
            }
        }

        List<String[]> data = result.get("dataList");
        List<TableField> fields = result.get("fieldList");
        String[] fieldArray = fields.stream().map(TableField::getFieldName).toArray(String[]::new);
        if (checkIsRepeat(fieldArray)) {
            DataEaseException.throwException(Translator.get("i18n_excel_field_repeat"));
        }
        List<Map<String, Object>> jsonArray = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(data)) {
            jsonArray = data.stream().map(ele -> {
                Map<String, Object> map = new HashMap<>();
                for (int i = 0; i < ele.length; i++) {
                    map.put(fieldArray[i], ele[i]);
                }
                return map;
            }).collect(Collectors.toList());
        }

        Map<String, Object> map = new HashMap<>();
        map.put("fields", fields);
        map.put("data", jsonArray);
        map.put("log", datasetSqlLog);

        return ResultHolder.success(map);
    }

    public Map<String, Object> getUnionPreview(DataSetTableRequest dataSetTableRequest) throws Exception {
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
        Map<String, Object> sqlMap;
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        Datasource ds;
        if (dataSetTableRequest.getMode() == 0) {
            ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
            datasourceRequest.setDatasource(ds);
            sqlMap = getUnionSQLDatasource(dataTableInfoDTO, ds);
        } else {
            ds = engineService.getDeEngine();
            datasourceRequest.setDatasource(ds);
            sqlMap = getUnionSQLDoris(dataTableInfoDTO);
        }
        String sql = (String) sqlMap.get("sql");
        List<DatasetTableField> fieldList = (List<DatasetTableField>) sqlMap.get("field");

        Map<String, Object> res = new HashMap<>();
        // 处理结果
        try {
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            datasourceRequest.setQuery(qp.createSQLPreview(sql, null));
            Map<String, List> result = datasourceProvider.fetchResultAndField(datasourceRequest);
            List<String[]> data = result.get("dataList");
            List<TableField> fields = result.get("fieldList");
            String[] fieldArray = fields.stream().map(TableField::getFieldName).toArray(String[]::new);

            List<Map<String, Object>> jsonArray = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(data)) {
                jsonArray = data.stream().map(ele -> {
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 0; i < ele.length; i++) {
                        map.put(fieldArray[i], ele[i]);
                    }
                    return map;
                }).collect(Collectors.toList());
            }

            // 获取每个字段在当前de数据库中的name，作为sql查询后的remarks返回前端展示
            for (DatasetTableField datasetTableField : fieldList) {
                for (TableField tableField : fields) {
                    if (StringUtils.equalsIgnoreCase(tableField.getFieldName(),
                            TableUtils.fieldName(
                                    datasetTableField.getTableId() + "_" + datasetTableField.getDataeaseName()))
                            || StringUtils.equalsIgnoreCase(tableField.getFieldName(),
                            TableUtils.fieldNameShort(datasetTableField.getTableId() + "_"
                                    + datasetTableField.getOriginName()))) {
                        tableField.setRemarks(datasetTableField.getName());
                        break;
                    }
                }
            }

            res.put("fields", fields);
            res.put("data", jsonArray);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return res;
        }
    }

    public Map<String, Object> getCustomPreview(DataSetTableRequest dataSetTableRequest) throws Exception {
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
        List<DataSetTableUnionDTO> list = dataSetTableUnionService
                .listByTableId(dataTableInfoDTO.getList().get(0).getTableId());
        String sql;

        DatasourceRequest datasourceRequest = new DatasourceRequest();
        Datasource ds;
        if (dataSetTableRequest.getMode() == 0) {
            ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
            datasourceRequest.setDatasource(ds);
            sql = getCustomSQLDatasource(dataTableInfoDTO, list, ds);
        } else {
            ds = engineService.getDeEngine();
            datasourceRequest.setDatasource(ds);
            sql = getCustomViewSQL(dataTableInfoDTO, list);
        }
        Map<String, Object> res = new HashMap<>();
        try {
            Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            datasourceRequest.setQuery(qp.createSQLPreview(sql, null));
            Map<String, List> result = datasourceProvider.fetchResultAndField(datasourceRequest);
            List<String[]> data = result.get("dataList");
            List<TableField> fields = result.get("fieldList");
            String[] fieldArray = fields.stream().map(TableField::getFieldName).toArray(String[]::new);

            List<Map<String, Object>> jsonArray = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(data)) {
                jsonArray = data.stream().map(ele -> {
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 0; i < ele.length; i++) {
                        map.put(fieldArray[i], ele[i]);
                    }
                    return map;
                }).collect(Collectors.toList());
            }

            // 获取每个字段在当前de数据库中的name，作为sql查询后的remarks返回前端展示
            List<DatasetTableField> checkedFieldList = new ArrayList<>();
            dataTableInfoDTO.getList().forEach(
                    ele -> checkedFieldList.addAll(dataSetTableFieldsService.getListByIds(ele.getCheckedFields())));
            for (DatasetTableField datasetTableField : checkedFieldList) {
                for (TableField tableField : fields) {
                    if (StringUtils.equalsIgnoreCase(tableField.getFieldName(),
                            TableUtils.fieldName(
                                    datasetTableField.getTableId() + "_" + datasetTableField.getDataeaseName()))
                            || StringUtils.equalsIgnoreCase(tableField.getFieldName(), TableUtils.fieldName(
                            datasetTableField.getTableId() + "_" + datasetTableField.getOriginName()))) {
                        tableField.setRemarks(datasetTableField.getName());
                        break;
                    }
                }
            }

            res.put("fields", fields);
            res.put("data", jsonArray);
            return res;
        } catch (Exception e) {
            return res;
        }
    }

    // 自助数据集从数据引擎（dorsi/mysql/...）里预览数据
    private String getCustomViewSQL(DataTableInfoDTO dataTableInfoDTO, List<DataSetTableUnionDTO> list) {
        Map<String, String[]> customInfo = new TreeMap<>();
        dataTableInfoDTO.getList().forEach(ele -> {
            String table = TableUtils.tableName(ele.getTableId());
            DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(ele.getTableId());
            if (ObjectUtils.isEmpty(datasetTable)) {
                throw new RuntimeException(Translator.get("i18n_custom_ds_delete"));
            }
            List<DatasetTableField> fields = dataSetTableFieldsService.getListByIdsEach(ele.getCheckedFields());
            if (CollectionUtils.isEmpty(fields)) {
                throw new RuntimeException(Translator.get("i18n_cst_ds_tb_or_field_deleted"));
            }
            String[] array = fields.stream()
                    .map(f -> table + "." + f.getDataeaseName() + " AS "
                            + TableUtils.fieldName(ele.getTableId() + "_" + f.getDataeaseName()))
                    .toArray(String[]::new);
            customInfo.put(table, array);
        });
        DataTableInfoCustomUnion first = dataTableInfoDTO.getList().get(0);
        if (CollectionUtils.isNotEmpty(list)) {
            StringBuilder field = new StringBuilder();
            for (Map.Entry<String, String[]> next : customInfo.entrySet()) {
                field.append(StringUtils.join(next.getValue(), ",")).append(",");
            }
            String f = field.substring(0, field.length() - 1);

            StringBuilder join = new StringBuilder();
            for (DataTableInfoCustomUnion dataTableInfoCustomUnion : dataTableInfoDTO.getList()) {
                for (DataSetTableUnionDTO dto : list) {
                    // 被关联表和自助数据集的表相等
                    if (StringUtils.equals(dto.getTargetTableId(), dataTableInfoCustomUnion.getTableId())) {
                        DatasetTableField sourceField = dataSetTableFieldsService.get(dto.getSourceTableFieldId());
                        DatasetTableField targetField = dataSetTableFieldsService.get(dto.getTargetTableFieldId());
                        if (ObjectUtils.isEmpty(sourceField) || ObjectUtils.isEmpty(targetField)) {
                            DEException.throwException(Translator.get("i18n_dataset_field_delete"));
                        }

                        join.append(convertUnionTypeToSQL(dto.getSourceUnionRelation()))
                                .append(TableUtils.tableName(dto.getTargetTableId()))
                                .append(" ON ")
                                .append(TableUtils.tableName(dto.getSourceTableId())).append(".")
                                .append(sourceField.getDataeaseName())
                                .append(" = ")
                                .append(TableUtils.tableName(dto.getTargetTableId())).append(".")
                                .append(targetField.getDataeaseName());
                    }
                }
            }
            if (StringUtils.isEmpty(f)) {
                throw new RuntimeException(Translator.get("i18n_custom_ds_delete"));
            }
            return MessageFormat.format("SELECT {0} FROM {1}", f, TableUtils.tableName(first.getTableId()))
                    + join.toString();
        } else {
            if (StringUtils
                    .isEmpty(StringUtils.join(customInfo.get(TableUtils.tableName(first.getTableId())), ","))) {
                throw new RuntimeException(Translator.get("i18n_custom_ds_delete"));
            }
            return MessageFormat.format("SELECT {0} FROM {1}",
                    StringUtils.join(customInfo.get(TableUtils.tableName(first.getTableId())), ","),
                    TableUtils.tableName(first.getTableId()));
        }
    }

    public String getCustomSQLDatasource(DataTableInfoDTO dataTableInfoDTO, List<DataSetTableUnionDTO> list, Datasource ds) {
        DataSourceType dataSourceType = datasourceService.types().stream().filter(obj -> Objects.equals(obj.getType(), ds.getType())).collect(Collectors.toList()).get(0);
        String keyword = dataSourceType.getKeywordPrefix() + "%s" + dataSourceType.getKeywordSuffix();
        Map<String, String[]> customInfo = new TreeMap<>();
        for (DataTableInfoCustomUnion ele : dataTableInfoDTO.getList()) {
            DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(ele.getTableId());
            if (ObjectUtils.isEmpty(datasetTable)) {
                throw new RuntimeException(Translator.get("i18n_custom_ds_delete"));
            }
            String table = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getTable();
            List<DatasetTableField> fields = dataSetTableFieldsService.getListByIdsEach(ele.getCheckedFields());
            if (CollectionUtils.isEmpty(fields)) {
                throw new RuntimeException(Translator.get("i18n_cst_ds_tb_or_field_deleted"));
            }
            String[] array = fields.stream()
                    .map(f -> String.format(keyword, table) + "." + String.format(keyword, f.getOriginName()) + " AS "
                            + TableUtils.fieldName(ele.getTableId() + "_" + f.getOriginName()))
                    .toArray(String[]::new);
            customInfo.put(table, array);
        }
        DataTableInfoCustomUnion first = dataTableInfoDTO.getList().get(0);
        DatasetTable table = datasetTableMapper.selectByPrimaryKey(first.getTableId());
        String tableName = new Gson().fromJson(table.getInfo(), DataTableInfoDTO.class).getTable();
        if (CollectionUtils.isNotEmpty(list)) {
            StringBuilder field = new StringBuilder();
            for (Map.Entry<String, String[]> next : customInfo.entrySet()) {
                field.append(StringUtils.join(next.getValue(), ",")).append(",");
            }
            String f = field.substring(0, field.length() - 1);

            StringBuilder join = new StringBuilder();
            for (DataTableInfoCustomUnion dataTableInfoCustomUnion : dataTableInfoDTO.getList()) {
                for (DataSetTableUnionDTO dto : list) {
                    // 被关联表和自助数据集的表相等
                    if (StringUtils.equals(dto.getTargetTableId(), dataTableInfoCustomUnion.getTableId())) {
                        DatasetTableField sourceField = dataSetTableFieldsService.get(dto.getSourceTableFieldId());
                        DatasetTableField targetField = dataSetTableFieldsService.get(dto.getTargetTableFieldId());
                        if (ObjectUtils.isEmpty(sourceField) || ObjectUtils.isEmpty(targetField)) {
                            DEException.throwException(Translator.get("i18n_dataset_field_delete"));
                        }
                        DatasetTable sourceTable = datasetTableMapper.selectByPrimaryKey(dto.getSourceTableId());
                        String sourceTableName = new Gson().fromJson(sourceTable.getInfo(), DataTableInfoDTO.class)
                                .getTable();
                        DatasetTable targetTable = datasetTableMapper.selectByPrimaryKey(dto.getTargetTableId());
                        String targetTableName = new Gson().fromJson(targetTable.getInfo(), DataTableInfoDTO.class)
                                .getTable();
                        join.append(convertUnionTypeToSQL(dto.getSourceUnionRelation()))
                                .append(String.format(keyword, targetTableName))
                                .append(" ON ")
                                .append(String.format(keyword, sourceTableName)).append(".")
                                .append(String.format(keyword, sourceField.getOriginName()))
                                .append(" = ")
                                .append(String.format(keyword, targetTableName)).append(".")
                                .append(String.format(keyword, targetField.getOriginName()));
                    }
                }
            }
            if (StringUtils.isEmpty(f)) {
                throw new RuntimeException(Translator.get("i18n_custom_ds_delete"));
            }
            return MessageFormat.format("SELECT {0} FROM {1}", f, String.format(keyword, tableName)) + join.toString();
        } else {
            if (StringUtils.isEmpty(StringUtils.join(customInfo.get(tableName), ","))) {
                throw new RuntimeException(Translator.get("i18n_custom_ds_delete"));
            }
            return MessageFormat.format("SELECT {0} FROM {1}", StringUtils.join(customInfo.get(tableName), ","),
                    String.format(keyword, tableName));
        }
    }

    private String convertUnionTypeToSQL(String unionType) {
        switch (unionType) {
            case "1:1":
            case "inner":
                return " INNER JOIN ";
            case "1:N":
            case "left":
                return " LEFT JOIN ";
            case "N:1":
            case "right":
                return " RIGHT JOIN ";
            case "N:N":
            case "full":
                return " FULL JOIN ";
            default:
                return " INNER JOIN ";
        }
    }

    // 关联数据集从doris里预览数据
    private Map<String, Object> getUnionSQLDoris(DataTableInfoDTO dataTableInfoDTO) {
        List<UnionDTO> union = dataTableInfoDTO.getUnion();
        // 所有选中的字段，即select后的查询字段
        Map<String, String[]> checkedInfo = new LinkedHashMap<>();
        List<UnionParamDTO> unionList = new ArrayList<>();
        List<DatasetTableField> checkedFields = new ArrayList<>();
        String sql = "";
        for (UnionDTO unionDTO : union) {
            // doris 使用数据集id做表名，拼sql将用到该名称
            String tableId = unionDTO.getCurrentDs().getId();
            String table = TableUtils.tableName(tableId);
            DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(tableId);
            if (ObjectUtils.isEmpty(datasetTable)) {
                DEException.throwException(
                        Translator.get("i18n_custom_ds_delete") + String.format(":table id [%s]", tableId));
            }
            CurrentUserDto user = AuthUtils.getUser();
            if (user != null && !user.getIsAdmin()) {
                DataSetTableDTO withPermission = getWithPermission(datasetTable.getId(), user.getUserId());
                if (ObjectUtils.isEmpty(withPermission.getPrivileges()) || !withPermission.getPrivileges().contains("use")) {
                    DEException.throwException(
                            Translator.get("i18n_dataset_no_permission") + String.format(":table name [%s]", withPermission.getName()));
                }
            }
            List<DatasetTableField> fields = dataSetTableFieldsService.getListByIdsEach(unionDTO.getCurrentDsField());

            String[] array = fields.stream()
                    .map(f -> table + "." + f.getDataeaseName() + " AS "
                            + TableUtils.fieldName(tableId + "_" + f.getDataeaseName()))
                    .toArray(String[]::new);
            checkedInfo.put(table, array);
            checkedFields.addAll(fields);
            // 获取child的fields和union
            if (CollectionUtils.isNotEmpty(unionDTO.getChildrenDs())) {
                getUnionSQLDorisJoin(unionDTO.getChildrenDs(), checkedInfo, unionList, checkedFields);
            }
        }
        // build sql
        if (CollectionUtils.isNotEmpty(unionList)) {
            // field
            StringBuilder field = new StringBuilder();
            for (Map.Entry<String, String[]> next : checkedInfo.entrySet()) {
                if (next.getValue().length > 0) {
                    field.append(StringUtils.join(next.getValue(), ",")).append(",");
                }
            }
            String f = subPrefixSuffixChar(field.toString());
            // join
            StringBuilder join = new StringBuilder();
            for (UnionParamDTO unionParamDTO : unionList) {
                String joinType = convertUnionTypeToSQL(unionParamDTO.getUnionType());
                UnionItemDTO u = unionParamDTO.getUnionFields().get(0);
                DatasetTableField pField = dataSetTableFieldsService.get(u.getParentField().getId());
                DatasetTableField cField = dataSetTableFieldsService.get(u.getCurrentField().getId());
                if (ObjectUtils.isEmpty(pField) || ObjectUtils.isEmpty(cField)) {
                    DEException.throwException(Translator.get("i18n_dataset_field_delete"));
                }
                DatasetTable parentTable = datasetTableMapper.selectByPrimaryKey(pField.getTableId());
                DatasetTable currentTable = datasetTableMapper.selectByPrimaryKey(cField.getTableId());

                join.append(" ").append(joinType).append(" ").append(TableUtils.tableName(currentTable.getId()))
                        .append(" ON ");
                for (int i = 0; i < unionParamDTO.getUnionFields().size(); i++) {
                    UnionItemDTO unionItemDTO = unionParamDTO.getUnionFields().get(i);
                    // 通过field id取得field详情，并且以第一组为准，寻找dataset table
                    DatasetTableField parentField = dataSetTableFieldsService
                            .get(unionItemDTO.getParentField().getId());
                    DatasetTableField currentField = dataSetTableFieldsService
                            .get(unionItemDTO.getCurrentField().getId());

                    join.append(TableUtils.tableName(parentTable.getId())).append(".")
                            .append(parentField.getDataeaseName())
                            .append(" = ")
                            .append(TableUtils.tableName(currentTable.getId())).append(".")
                            .append(currentField.getDataeaseName());
                    if (i < unionParamDTO.getUnionFields().size() - 1) {
                        join.append(" AND ");
                    }
                }
            }
            if (StringUtils.isEmpty(f)) {
                DEException.throwException(Translator.get("i18n_union_ds_no_checked"));
            }
            sql = MessageFormat.format("SELECT {0} FROM {1}", f,
                    TableUtils.tableName(union.get(0).getCurrentDs().getId())) + join.toString();
        } else {
            String f = StringUtils.join(checkedInfo.get(TableUtils.tableName(union.get(0).getCurrentDs().getId())),
                    ",");
            if (StringUtils.isEmpty(f)) {
                throw new RuntimeException(Translator.get("i18n_union_ds_no_checked"));
            }
            sql = MessageFormat.format("SELECT {0} FROM {1}", f,
                    TableUtils.tableName(union.get(0).getCurrentDs().getId()));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("sql", sql);
        map.put("field", checkedFields);
        map.put("join", unionList);
        return map;
    }

    // 递归计算出所有子级的checkedFields和unionParam
    private void getUnionSQLDorisJoin(List<UnionDTO> childrenDs, Map<String, String[]> checkedInfo,
                                      List<UnionParamDTO> unionList, List<DatasetTableField> checkedFields) {
        for (UnionDTO unionDTO : childrenDs) {
            String tableId = unionDTO.getCurrentDs().getId();
            String table = TableUtils.tableName(tableId);
            DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(tableId);
            if (ObjectUtils.isEmpty(datasetTable)) {
                DEException.throwException(
                        Translator.get("i18n_custom_ds_delete") + String.format(":table id [%s]", tableId));
            }
            List<DatasetTableField> fields = dataSetTableFieldsService.getListByIdsEach(unionDTO.getCurrentDsField());

            String[] array = fields.stream()
                    .map(f -> table + "." + f.getDataeaseName() + " AS "
                            + TableUtils.fieldName(tableId + "_" + f.getDataeaseName()))
                    .toArray(String[]::new);
            checkedInfo.put(table, array);
            checkedFields.addAll(fields);

            unionList.add(unionDTO.getUnionToParent());
            if (CollectionUtils.isNotEmpty(unionDTO.getChildrenDs())) {
                getUnionSQLDorisJoin(unionDTO.getChildrenDs(), checkedInfo, unionList, checkedFields);
            }
        }
    }

    // 关联数据集 直连模式
    public Map<String, Object> getUnionSQLDatasource(DataTableInfoDTO dataTableInfoDTO, Datasource ds) {
        DataSourceType dataSourceType = datasourceService.types().stream().filter(obj -> Objects.equals(obj.getType(), ds.getType())).collect(Collectors.toList()).get(0);
        String keyword = dataSourceType.getKeywordPrefix() + "%s" + dataSourceType.getKeywordSuffix();

        String configuration = ds.getConfiguration();
        JsonObject jsonObject = JsonParser.parseString(configuration).getAsJsonObject();
        JsonElement schemaJson = jsonObject.get("schema");
        String schema = null;
        if (schemaJson != null) {
            schema = schemaJson.getAsString();
        }
        String joinPrefix = "";
        if (StringUtils.isNotEmpty(schema) && (StringUtils.equalsIgnoreCase(ds.getType(), DatasourceTypes.db2.getType()) ||
                StringUtils.equalsIgnoreCase(ds.getType(), DatasourceTypes.sqlServer.getType()) ||
                StringUtils.equalsIgnoreCase(ds.getType(), DatasourceTypes.oracle.getType()) ||
                StringUtils.equalsIgnoreCase(ds.getType(), DatasourceTypes.pg.getType()))) {
            joinPrefix = String.format(keyword, schema) + ".";
        }

        List<UnionDTO> union = dataTableInfoDTO.getUnion();
        // 所有选中的字段，即select后的查询字段
        Map<String, String[]> checkedInfo = new LinkedHashMap<>();
        List<UnionParamDTO> unionList = new ArrayList<>();
        List<DatasetTableField> checkedFields = new ArrayList<>();
        String sql = "";
        String tableName = new Gson()
                .fromJson(datasetTableMapper.selectByPrimaryKey(union.get(0).getCurrentDs().getId()).getInfo(),
                        DataTableInfoDTO.class)
                .getTable();
        for (UnionDTO unionDTO : union) {
            DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(unionDTO.getCurrentDs().getId());
            String table = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getTable();
            String tableId = unionDTO.getCurrentDs().getId();
            if (ObjectUtils.isEmpty(datasetTable)) {
                DEException.throwException(
                        Translator.get("i18n_custom_ds_delete") + String.format(":table id [%s]", tableId));
            }
            CurrentUserDto user = AuthUtils.getUser();
            if (user != null && !user.getIsAdmin()) {
                DataSetTableDTO withPermission = getWithPermission(datasetTable.getId(), user.getUserId());
                if (ObjectUtils.isEmpty(withPermission.getPrivileges()) || !withPermission.getPrivileges().contains("use")) {
                    DEException.throwException(
                            Translator.get("i18n_dataset_no_permission") + String.format(":table name [%s]", withPermission.getName()));
                }
            }
            List<DatasetTableField> fields = dataSetTableFieldsService.getListByIdsEach(unionDTO.getCurrentDsField());

            String[] array = fields.stream()
                    .map(f -> String.format(keyword, table) + "." + String.format(keyword, f.getOriginName()) + " AS "
                            + TableUtils.fieldNameShort(tableId + "_" + f.getOriginName()))
                    .toArray(String[]::new);
            checkedInfo.put(table, array);
            checkedFields.addAll(fields);
            // 获取child的fields和union
            if (CollectionUtils.isNotEmpty(unionDTO.getChildrenDs())) {
                getUnionSQLDatasourceJoin(unionDTO.getChildrenDs(), checkedInfo, unionList, keyword, checkedFields);
            }
        }
        // build sql
        if (CollectionUtils.isNotEmpty(unionList)) {
            // field
            StringBuilder field = new StringBuilder();
            for (Map.Entry<String, String[]> next : checkedInfo.entrySet()) {
                if (next.getValue().length > 0) {
                    field.append(StringUtils.join(next.getValue(), ",")).append(",");
                }
            }
            String f = subPrefixSuffixChar(field.toString());
            // join
            StringBuilder join = new StringBuilder();
            for (UnionParamDTO unionParamDTO : unionList) {
                String joinType = convertUnionTypeToSQL(unionParamDTO.getUnionType());
                UnionItemDTO u = unionParamDTO.getUnionFields().get(0);
                DatasetTableField pField = dataSetTableFieldsService.get(u.getParentField().getId());
                DatasetTableField cField = dataSetTableFieldsService.get(u.getCurrentField().getId());
                if (ObjectUtils.isEmpty(pField) || ObjectUtils.isEmpty(cField)) {
                    DEException.throwException(Translator.get("i18n_dataset_field_delete"));
                }
                DatasetTable parentTable = datasetTableMapper.selectByPrimaryKey(pField.getTableId());
                String parentTableName = new Gson().fromJson(parentTable.getInfo(), DataTableInfoDTO.class).getTable();
                DatasetTable currentTable = datasetTableMapper.selectByPrimaryKey(cField.getTableId());
                String currentTableName = new Gson().fromJson(currentTable.getInfo(), DataTableInfoDTO.class)
                        .getTable();

                join.append(" ").append(joinType).append(" ").append(joinPrefix).append(String.format(keyword, currentTableName))
                        .append(" ON ");
                for (int i = 0; i < unionParamDTO.getUnionFields().size(); i++) {
                    UnionItemDTO unionItemDTO = unionParamDTO.getUnionFields().get(i);
                    // 通过field id取得field详情，并且以第一组为准，寻找dataset table
                    DatasetTableField parentField = dataSetTableFieldsService
                            .get(unionItemDTO.getParentField().getId());
                    DatasetTableField currentField = dataSetTableFieldsService
                            .get(unionItemDTO.getCurrentField().getId());

                    join.append(String.format(keyword, parentTableName)).append(".")
                            .append(String.format(keyword, parentField.getOriginName()))
                            .append(" = ")
                            .append(String.format(keyword, currentTableName)).append(".")
                            .append(String.format(keyword, currentField.getOriginName()));
                    if (i < unionParamDTO.getUnionFields().size() - 1) {
                        join.append(" AND ");
                    }
                }
            }
            if (StringUtils.isEmpty(f)) {
                DEException.throwException(Translator.get("i18n_union_ds_no_checked"));
            }
            sql = MessageFormat.format("SELECT {0} FROM {1}", f, joinPrefix + String.format(keyword, tableName)) + join.toString();
        } else {
            String f = StringUtils.join(checkedInfo.get(tableName), ",");
            if (StringUtils.isEmpty(f)) {
                throw new RuntimeException(Translator.get("i18n_union_ds_no_checked"));
            }
            sql = MessageFormat.format("SELECT {0} FROM {1}", f, joinPrefix + String.format(keyword, tableName));
        }
        Map<String, Object> map = new HashMap<>();
        map.put("sql", sql);
        map.put("field", checkedFields);
        map.put("join", unionList);
        return map;
    }

    private String subPrefixSuffixChar(String str) {
        while (StringUtils.startsWith(str, ",")) {
            str = str.substring(1, str.length());
        }
        while (StringUtils.endsWith(str, ",")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    // 递归计算出所有子级的checkedFields和unionParam
    private void getUnionSQLDatasourceJoin(List<UnionDTO> childrenDs, Map<String, String[]> checkedInfo,
                                           List<UnionParamDTO> unionList, String keyword, List<DatasetTableField> checkedFields) {
        for (UnionDTO unionDTO : childrenDs) {
            DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(unionDTO.getCurrentDs().getId());
            String tableId = unionDTO.getCurrentDs().getId();
            if (ObjectUtils.isEmpty(datasetTable)) {
                DEException.throwException(
                        Translator.get("i18n_custom_ds_delete") + String.format(":table id [%s]", tableId));
            }
            String table = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class).getTable();

            List<DatasetTableField> fields = dataSetTableFieldsService.getListByIdsEach(unionDTO.getCurrentDsField());

            String[] array = fields.stream()
                    .map(f -> String.format(keyword, table) + "." + String.format(keyword, f.getOriginName()) + " AS "
                            + TableUtils.fieldNameShort(tableId + "_" + f.getOriginName()))
                    .toArray(String[]::new);
            checkedInfo.put(table, array);
            checkedFields.addAll(fields);

            unionList.add(unionDTO.getUnionToParent());
            if (CollectionUtils.isNotEmpty(unionDTO.getChildrenDs())) {
                getUnionSQLDatasourceJoin(unionDTO.getChildrenDs(), checkedInfo, unionList, keyword, checkedFields);
            }
        }
    }

    public List<DatasetTableField> saveExcelTableField(String datasetTableId, List<TableField> fields, boolean insert) {
        List<DatasetTableField> datasetTableFields = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(fields)) {
            for (int i = 0; i < fields.size(); i++) {
                TableField field = fields.get(i);
                DatasetTableField datasetTableField = DatasetTableField.builder().build();
                datasetTableField.setTableId(datasetTableId);
                datasetTableField.setOriginName(field.getFieldName());
                datasetTableField.setName(field.getRemarks());
                datasetTableField.setDataeaseName(TableUtils.columnName(field.getFieldName()));
                datasetTableField.setType(field.getFieldType());
                datasetTableField.setDeType(transFieldType(field.getFieldType()));
                datasetTableField.setDeExtractType(transFieldType(field.getFieldType()));
                datasetTableField.setSize(field.getFieldSize());
                datasetTableField.setChecked(true);
                datasetTableField.setColumnIndex(i);
                datasetTableField.setLastSyncTime(System.currentTimeMillis());
                datasetTableField.setExtField(0);
                datasetTableField.setGroupType(datasetTableField.getDeType() < 2 ? "d" : "q");
                if (insert) {
                    dataSetTableFieldsService.save(datasetTableField);
                }
                datasetTableFields.add(datasetTableField);
            }
        }
        return datasetTableFields;
    }

    public void saveTableField(DatasetTable datasetTable) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasetTable.getDataSourceId());
        if (ObjectUtils.isEmpty(ds) && !datasetTable.getType().equalsIgnoreCase(DatasetType.UNION.name())) {
            throw new RuntimeException(Translator.get("i18n_datasource_delete"));
        }
        DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
        BeanUtils.copyBean(dataSetTableRequest, datasetTable);

        List<TableField> fields = new ArrayList<>();
        long syncTime = System.currentTimeMillis();
        if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.DB.name()) || StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.API.name())) {
            fields = getFields(datasetTable);
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.SQL.name())) {
            Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            DataTableInfoDTO dataTableInfo = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
            String sql = dataTableInfo.isBase64Encryption() ? new String(java.util.Base64.getDecoder().decode(dataTableInfo.getSql())) : dataTableInfo.getSql();
            sql = removeVariables(sql, ds.getType()).replaceAll(SubstitutedSql.trim(), SubstitutedSqlVirtualData);
            String sqlAsTable = qp.createSQLPreview(sql, null);
            datasourceRequest.setQuery(sqlAsTable);
            fields = datasourceProvider.fetchResultField(datasourceRequest);
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.EXCEL.name())) {
            fields = dataSetTableRequest.getFields();
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.CUSTOM.name())) {
            if (datasetTable.getMode() == 1) {
                // save field
                DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(),
                        DataTableInfoDTO.class);
                List<DataTableInfoCustomUnion> list = dataTableInfoDTO.getList();
                List<DatasetTableField> fieldList = new ArrayList<>();
                list.forEach(ele -> {
                    List<DatasetTableField> listByIds = dataSetTableFieldsService
                            .getListByIdsEach(ele.getCheckedFields());
                    listByIds.forEach(f -> f.setDataeaseName(
                            TableUtils.fieldName(ele.getTableId() + "_" + f.getDataeaseName())));
                    fieldList.addAll(listByIds);
                });
                for (int i = 0; i < fieldList.size(); i++) {
                    DatasetTableField datasetTableField = fieldList.get(i);
                    datasetTableField.setId(null);
                    datasetTableField.setTableId(datasetTable.getId());
                    datasetTableField.setColumnIndex(i);
                }
                dataSetTableFieldsService.deleteByTableId(datasetTable.getId());
                dataSetTableFieldsService.batchEdit(fieldList);
                // custom 创建doris视图
                if (datasetTable.getMode() == 1) {
                    createDorisView(TableUtils.tableName(datasetTable.getId()), getCustomViewSQL(dataTableInfoDTO,
                            dataSetTableUnionService.listByTableId(dataTableInfoDTO.getList().get(0).getTableId())));
                }
                return;
            } else {
                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
                List<DataSetTableUnionDTO> list = dataSetTableUnionService
                        .listByTableId(dt.getList().get(0).getTableId());
                String sqlAsTable = getCustomSQLDatasource(dt, list, ds);
                datasourceRequest.setQuery(sqlAsTable);
                fields = datasourceProvider.fetchResultField(datasourceRequest);

                DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(),
                        DataTableInfoDTO.class);
                List<DataTableInfoCustomUnion> listField = dataTableInfoDTO.getList();
                List<DatasetTableField> fieldList = new ArrayList<>();
                listField.forEach(ele -> {
                    List<DatasetTableField> listByIds = dataSetTableFieldsService
                            .getListByIdsEach(ele.getCheckedFields());
                    fieldList.addAll(listByIds);
                });
                for (DatasetTableField field : fieldList) {
                    for (TableField tableField : fields) {
                        if (StringUtils.equalsIgnoreCase(
                                TableUtils.fieldName(field.getTableId() + "_" + field.getOriginName()),
                                tableField.getFieldName())) {
                            tableField.setRemarks(field.getName());
                            break;
                        }
                    }
                }
            }
        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "union")) {
            if (datasetTable.getMode() == 1) {
                ds = engineService.getDeEngine();
                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                // save field
                DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(),
                        DataTableInfoDTO.class);
                Map<String, Object> sqlMap = getUnionSQLDoris(dataTableInfoDTO);
                String sql = (String) sqlMap.get("sql");
                List<DatasetTableField> fieldList = (List<DatasetTableField>) sqlMap.get("field");


                // custom 创建doris视图
                createDorisView(TableUtils.tableName(datasetTable.getId()), sql);

                // getQuerySql to get field
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createSQLPreview(sql, null));
                fields = datasourceProvider.fetchResultField(datasourceRequest);
                for (DatasetTableField field : fieldList) {
                    for (TableField tableField : fields) {
                        if (StringUtils.equalsIgnoreCase(
                                TableUtils.fieldName(field.getTableId() + "_" + field.getDataeaseName()),
                                tableField.getFieldName())) {
                            tableField.setRemarks(field.getName());
                            tableField.setFieldType(field.getType()); //将原有的type赋值给新创建的数据列
                            break;
                        }
                    }
                }
            } else {
                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
                DatasourceRequest datasourceRequest = new DatasourceRequest();
                datasourceRequest.setDatasource(ds);
                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);

                Map<String, Object> sqlMap = getUnionSQLDatasource(dt, ds);
                String sql = (String) sqlMap.get("sql");
                List<DatasetTableField> fieldList = (List<DatasetTableField>) sqlMap.get("field");

                // getQuerySql to get field
                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
                datasourceRequest.setQuery(qp.createSQLPreview(sql, null));
                fields = datasourceProvider.fetchResultField(datasourceRequest);

                for (DatasetTableField field : fieldList) {
                    for (TableField tableField : fields) {
                        if (StringUtils.equalsIgnoreCase(
                                TableUtils.fieldNameShort(field.getTableId() + "_" + field.getOriginName()),
                                tableField.getFieldName())) {
                            tableField.setRemarks(field.getName());
                            break;
                        }
                    }
                }
            }
        }
        QueryProvider qp = null;
        if (!ObjectUtils.isEmpty(ds)) {
            qp = ProviderFactory.getQueryProvider(ds.getType());
        }
        if (CollectionUtils.isNotEmpty(fields)) {
            List<String> originNameList = new ArrayList<>();
            for (int i = 0; i < fields.size(); i++) {
                TableField field = fields.get(i);
                originNameList.add(field.getFieldName());
                DatasetTableField datasetTableField = DatasetTableField.builder().build();
                // 物理字段名设定为唯一，查询当前数据集下是否已存在该字段，存在则update，不存在则insert
                DatasetTableFieldExample datasetTableFieldExample = new DatasetTableFieldExample();
                // 字段名一致，认为字段没有改变
                datasetTableFieldExample.createCriteria().andTableIdEqualTo(datasetTable.getId()).andOriginNameEqualTo(field.getFieldName());
                List<DatasetTableField> datasetTableFields = datasetTableFieldMapper.selectByExample(datasetTableFieldExample);
                if (CollectionUtils.isNotEmpty(datasetTableFields)) {
                    datasetTableField.setId(datasetTableFields.get(0).getId());
                    datasetTableField.setOriginName(field.getFieldName());
                    datasetTableField.setType(field.getFieldType());
                    datasetTableField.setSize(field.getFieldSize());
                    datasetTableField.setAccuracy(field.getAccuracy());
                    if (ObjectUtils.isEmpty(ds)) {
                        datasetTableField.setDeExtractType(transFieldType(field.getFieldType()));
                    } else {
                        Integer fieldType = qp.transFieldType(field.getFieldType());
                        datasetTableField.setDeExtractType(fieldType);
                    }
                } else {
                    datasetTableField.setTableId(datasetTable.getId());
                    datasetTableField.setOriginName(field.getFieldName());
                    datasetTableField.setName(field.getRemarks());
                    if (datasetTable.getMode() == 1 && StringUtils.equalsIgnoreCase("union", datasetTable.getType())) {
                        datasetTableField.setDataeaseName(field.getFieldName());
                    } else {
                        datasetTableField.setDataeaseName(TableUtils.columnName(field.getFieldName()));
                    }
                    datasetTableField.setType(field.getFieldType());
                    if (ObjectUtils.isEmpty(ds)) {
                        datasetTableField.setDeType(transFieldType(field.getFieldType()));
                        datasetTableField.setDeExtractType(transFieldType(field.getFieldType()));
                    } else {
                        Integer fieldType = qp.transFieldType(field.getFieldType());
                        datasetTableField.setDeType(fieldType == 4 ? 2 : (fieldType == 6 ? 0 : fieldType));
                        datasetTableField.setDeExtractType(fieldType);
                    }
                    datasetTableField.setSize(field.getFieldSize());
                    datasetTableField.setAccuracy(field.getAccuracy());
                    datasetTableField.setChecked(true);
                    datasetTableField.setLastSyncTime(syncTime);
                    datasetTableField.setExtField(0);
                    datasetTableField.setGroupType((datasetTableField.getDeType() < 2 || datasetTableField.getDeType() == 6) ? "d" : "q");
                }
                datasetTableField.setColumnIndex(i);
                dataSetTableFieldsService.save(datasetTableField);
            }
            // delete 数据库中多余的字段
            DatasetTableFieldExample datasetTableFieldExample = new DatasetTableFieldExample();
            datasetTableFieldExample.createCriteria().andTableIdEqualTo(datasetTable.getId()).andExtFieldEqualTo(0).andOriginNameNotIn(originNameList);
            datasetTableFieldMapper.deleteByExample(datasetTableFieldExample);
        }
    }

    private void createDorisView(String dorisTableName, String customSql) throws Exception {
        Datasource engine = engineService.getDeEngine();
        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(engine);
        DDLProvider ddlProvider = ProviderFactory.getDDLProvider(engine.getType());
        // 先删除表
        datasourceRequest.setQuery(ddlProvider.dropView(dorisTableName));
        jdbcProvider.exec(datasourceRequest);
        datasourceRequest.setQuery(ddlProvider.createView(dorisTableName, customSql));
        jdbcProvider.exec(datasourceRequest);
    }

    public Integer transFieldType(String field) {
        switch (field) {
            case "TEXT":
                return 0;
            case "DATETIME":
                return 1;
            case "LONG":
            case "INT":
                return 2;
            case "DOUBLE":
                return 3;
            default:
                return 0;
        }
    }

    public DatasetTableIncrementalConfig incrementalConfig(
            DatasetTableIncrementalConfig datasetTableIncrementalConfig) {
        if (StringUtils.isEmpty(datasetTableIncrementalConfig.getTableId())) {
            return new DatasetTableIncrementalConfig();
        }
        DatasetTableIncrementalConfigExample example = new DatasetTableIncrementalConfigExample();
        example.createCriteria().andTableIdEqualTo(datasetTableIncrementalConfig.getTableId());
        List<DatasetTableIncrementalConfig> configs = datasetTableIncrementalConfigMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(configs)) {
            return configs.get(0);
        } else {
            return new DatasetTableIncrementalConfig();
        }

    }

    public DatasetTableIncrementalConfig incrementalConfig(String datasetTableId) {
        DatasetTableIncrementalConfig datasetTableIncrementalConfig = new DatasetTableIncrementalConfig();
        datasetTableIncrementalConfig.setTableId(datasetTableId);
        return incrementalConfig(datasetTableIncrementalConfig);
    }

    public void saveIncrementalConfig(DatasetTableIncrementalConfig datasetTableIncrementalConfig) throws Exception {
        if (datasetTableIncrementalConfig == null || StringUtils.isEmpty(datasetTableIncrementalConfig.getTableId())) {
            return;
        }
        if (StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalAdd())) {
            datasetTableIncrementalConfig.setIncrementalAdd(datasetTableIncrementalConfig.getIncrementalAdd().trim());
        }
        if (StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalDelete())) {
            datasetTableIncrementalConfig.setIncrementalDelete(datasetTableIncrementalConfig.getIncrementalDelete().trim());
        }
        if (StringUtils.isEmpty(datasetTableIncrementalConfig.getId())) {
            datasetTableIncrementalConfig.setId(UUID.randomUUID().toString());
            datasetTableIncrementalConfigMapper.insertSelective(datasetTableIncrementalConfig);
        } else {
            datasetTableIncrementalConfigMapper.updateByPrimaryKey(datasetTableIncrementalConfig);
        }
        DatasetTable datasetTable = get(datasetTableIncrementalConfig.getTableId());
        DeLogUtils.save(SysLogConstants.OPERATE_TYPE.MODIFY, SysLogConstants.SOURCE_TYPE.DATASET, datasetTable.getId(), datasetTable.getSceneId(), null, null);
        checkColumes(datasetTableIncrementalConfig);
    }

    private void checkColumes(DatasetTableIncrementalConfig datasetTableIncrementalConfig) throws Exception {
        DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(datasetTableIncrementalConfig.getTableId());
        List<DatasetTableField> datasetTableFields = dataSetTableFieldsService.getFieldsByTableId(datasetTable.getId())
                .stream().filter(datasetTableField -> datasetTableField.getExtField() == 0).sorted((o1, o2) -> {
                    if (o1.getColumnIndex() == null) {
                        return -1;
                    }
                    if (o2.getColumnIndex() == null) {
                        return 1;
                    }
                    return o1.getColumnIndex().compareTo(o2.getColumnIndex());
                }).collect(Collectors.toList());

        List<String> originNameFields = datasetTableFields.stream().map(DatasetTableField::getOriginName)
                .collect(Collectors.toList());
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasetTable.getDataSourceId());
        QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
        Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        if (StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalAdd())
                && StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalAdd().replace(" ", ""))) {// 增量添加
            String sql = datasetTableIncrementalConfig.getIncrementalAdd()
                    .replace(lastUpdateTime, Long.valueOf(System.currentTimeMillis()).toString())
                    .replace(currentUpdateTime, Long.valueOf(System.currentTimeMillis()).toString());
            datasourceRequest.setQuery(qp.wrapSql(sql));
            List<String> sqlFields = new ArrayList<>();
            try {
                datasourceProvider.fetchResultField(datasourceRequest).stream().map(TableField::getFieldName)
                        .forEach(sqlFields::add);
            } catch (Exception e) {
                DataEaseException.throwException(Translator.get("i18n_check_sql_error") + e.getMessage());
            }

            if (!originNameFields.equals(sqlFields)) {
                DataEaseException.throwException(Translator.get("i18n_sql_add_not_matching") + sqlFields.toString());
            }
        }
        if (StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalDelete())
                && StringUtils.isNotEmpty(datasetTableIncrementalConfig.getIncrementalDelete().replace(" ", ""))) {// 增量删除
            String sql = datasetTableIncrementalConfig.getIncrementalDelete()
                    .replace(lastUpdateTime, Long.valueOf(System.currentTimeMillis()).toString())
                    .replace(currentUpdateTime, Long.valueOf(System.currentTimeMillis()).toString());
            datasourceRequest.setQuery(qp.wrapSql(sql));
            List<String> sqlFields = new ArrayList<>();
            try {
                datasourceProvider.fetchResultField(datasourceRequest).stream().map(TableField::getFieldName)
                        .forEach(sqlFields::add);
            } catch (Exception e) {
                DataEaseException.throwException(Translator.get("i18n_check_sql_error") + e.getMessage());
            }

            if (!originNameFields.equals(sqlFields)) {
                DataEaseException.throwException(Translator.get("i18n_sql_delete_not_matching") + sqlFields.toString());
            }
        }
    }

    private void checkName(DatasetTable datasetTable) {
        DatasetTableExample datasetTableExample = new DatasetTableExample();
        DatasetTableExample.Criteria criteria = datasetTableExample.createCriteria();
        if (StringUtils.isNotEmpty(datasetTable.getId())) {
            criteria.andIdNotEqualTo(datasetTable.getId());
        }
        if (StringUtils.isNotEmpty(datasetTable.getSceneId())) {
            criteria.andSceneIdEqualTo(datasetTable.getSceneId());
        }
        if (StringUtils.isNotEmpty(datasetTable.getName())) {
            criteria.andNameEqualTo(datasetTable.getName());
        }
        List<DatasetTable> list = datasetTableMapper.selectByExample(datasetTableExample);
        if (!list.isEmpty()) {
            throw new RuntimeException(Translator.get("i18n_name_cant_repeat_same_group"));
        }
    }

    private void checkNames(List<DataSetTableRequest> datasetTable) {
        if (CollectionUtils.isEmpty(datasetTable)) {
            return;
        }
        Set<String> nameSet = new HashSet<>();
        for (DataSetTableRequest table : datasetTable) {
            if (StringUtils.isEmpty(table.getName())) {
                throw new RuntimeException(Translator.get("I18n_name_cant_empty"));
            }
            nameSet.add(table.getName());
        }
        if (nameSet.size() != datasetTable.size()) {
            throw new RuntimeException(Translator.get("i18n_name_cant_repeat_same_group"));
        }
        DatasetTableExample datasetTableExample = new DatasetTableExample();
        DatasetTableExample.Criteria criteria = datasetTableExample.createCriteria();
        if (StringUtils.isNotEmpty(datasetTable.get(0).getSceneId())) {
            criteria.andSceneIdEqualTo(datasetTable.get(0).getSceneId());
        }
        if (CollectionUtils.isNotEmpty(nameSet)) {
            criteria.andNameIn(new ArrayList<>(nameSet));
        }
        List<DatasetTable> list = datasetTableMapper.selectByExample(datasetTableExample);
        if (!list.isEmpty()) {
            throw new RuntimeException(Translator.get("i18n_name_cant_repeat_same_group"));
        }
    }

    public DataSetDetail getDatasetDetail(String id) {
        DataSetDetail dataSetDetail = new DataSetDetail();
        DataSetTableDTO table = extDataSetTableMapper.findOneDetails(id);
        dataSetDetail.setTable(table);
        if (ObjectUtils.isNotEmpty(table)) {
            Datasource datasource = datasourceMapper.selectByPrimaryKey(table.getDataSourceId());
            Optional.ofNullable(datasource).orElse(new Datasource()).setConfiguration(null);
            Collection<DataSourceType> types = datasourceService.types();
            for (DataSourceType type : types) {
                if (ObjectUtils.isNotEmpty(datasource) && StringUtils.equalsIgnoreCase(datasource.getType(), type.getType())) {
                    datasource.setType(type.getName());
                    break;
                }
            }
            dataSetDetail.setDatasource(datasource);
        }
        return dataSetDetail;
    }

    public ExcelFileData excelSaveAndParse(MultipartFile file, String tableId, Integer editType) throws Exception {
        String filename = file.getOriginalFilename();
        // parse file
        List<ExcelSheetData> excelSheetDataList = parseExcel(filename, file.getInputStream(), true);
        List<ExcelSheetData> returnSheetDataList = new ArrayList<>();

        if (StringUtils.isNotEmpty(tableId)) {
            List<DatasetTableField> fields = dataSetTableFieldsService.getFieldsByTableId(tableId);
            List<DatasetTableField> datasetTableFields = fields.stream().filter(datasetTableField -> datasetTableField.getExtField() == 0).sorted((o1, o2) -> {
                if (o1.getColumnIndex() == null) {
                    return -1;
                }
                if (o2.getColumnIndex() == null) {
                    return 1;
                }
                return o1.getColumnIndex().compareTo(o2.getColumnIndex());
            }).collect(Collectors.toList());

            List<String> oldFields = datasetTableFields.stream().map(DatasetTableField::getOriginName).collect(Collectors.toList());

            if (editType == 1) {
                for (ExcelSheetData excelSheetData : excelSheetDataList) {
                    List<TableField> tableFields = excelSheetData.getFields();
                    List<String> newFields = tableFields.stream().map(TableField::getRemarks).collect(Collectors.toList());
                    if (oldFields.equals(newFields)) {
                        returnSheetDataList.add(excelSheetData);
                    }
                }
                if (returnSheetDataList.isEmpty()) {
                    DataEaseException.throwException(Translator.get("i18n_excel_column_change"));
                }
            } else {
                List<DatasetTableField> extFields = fields.stream().filter(datasetTableField -> datasetTableField.getExtField() > 0).collect(Collectors.toList());
                List<String> extFieldsRefIds = new ArrayList<>();
                for (DatasetTableField extField : extFields) {
                    String originField = extField.getOriginName().replaceAll("[\\t\\n\\r]]", "");
                    String regex = "\\[(.*?)]";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(originField);
                    while (matcher.find()) {
                        String id = matcher.group(1);
                        if (!extFieldsRefIds.contains(id)) {
                            extFieldsRefIds.add(id);
                        }
                    }
                }
                List<String> extFieldsRefNames = datasetTableFields.stream().filter(datasetTableField -> extFieldsRefIds.contains(datasetTableField.getId())).map(DatasetTableField::getOriginName).collect(Collectors.toList());
                for (ExcelSheetData excelSheetData : excelSheetDataList) {
                    List<TableField> tableFields = excelSheetData.getFields();
                    List<String> newFields = tableFields.stream().map(TableField::getRemarks).collect(Collectors.toList());
                    excelSheetData.setChangeFiled(!oldFields.equals(newFields));
                    boolean effectExtField = false;
                    for (String extFieldsRefName : extFieldsRefNames) {
                        if (!newFields.contains(extFieldsRefName)) {
                            effectExtField = true;
                            break;
                        }
                    }
                    excelSheetData.setEffectExtField(effectExtField);

                    returnSheetDataList.add(excelSheetData);
                }
                if (returnSheetDataList.isEmpty()) {
                    DataEaseException.throwException(Translator.get("i18n_excel_column_change"));
                }
            }
        } else {
            returnSheetDataList = excelSheetDataList;
        }
        returnSheetDataList = returnSheetDataList.stream()
                .filter(excelSheetData -> CollectionUtils.isNotEmpty(excelSheetData.getFields()))
                .collect(Collectors.toList());
        // save file
        String excelId = UUID.randomUUID().toString();
        String filePath = saveFile(file, excelId);
        ExcelFileData excelFileData = new ExcelFileData();
        excelFileData.setExcelLabel(filename);
        excelFileData.setId(excelId);
        excelFileData.setPath(filePath);

        filename = filename.substring(0, filename.lastIndexOf('.'));
        if (returnSheetDataList.size() == 1) {
            returnSheetDataList.get(0).setDatasetName(filename);
            returnSheetDataList.get(0).setSheetExcelId(excelId);
            returnSheetDataList.get(0).setId(UUID.randomUUID().toString());
            returnSheetDataList.get(0).setPath(filePath);
        } else {
            for (ExcelSheetData excelSheetData : returnSheetDataList) {
                excelSheetData.setDatasetName(filename + "-" + excelSheetData.getExcelLabel());
                excelSheetData.setSheetExcelId(excelId);
                excelSheetData.setId(UUID.randomUUID().toString());
                excelSheetData.setPath(filePath);
            }
        }
        excelFileData.setSheets(returnSheetDataList);
        return excelFileData;
    }

    private List<ExcelSheetData> parseExcel(String filename, InputStream inputStream, boolean isPreview)
            throws Exception {
        List<ExcelSheetData> excelSheetDataList = new ArrayList<>();
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        if (StringUtils.equalsIgnoreCase(suffix, "xls")) {
            ExcelXlsReader excelXlsReader = new ExcelXlsReader();
            excelXlsReader.setObtainedNum(1000);
            excelXlsReader.process(inputStream);
            excelSheetDataList = excelXlsReader.totalSheets;
        }
        if (StringUtils.equalsIgnoreCase(suffix, "xlsx")) {
            ExcelXlsxReader excelXlsxReader = new ExcelXlsxReader();
            excelXlsxReader.setObtainedNum(1000);
            excelXlsxReader.process(inputStream);
            excelSheetDataList = excelXlsxReader.totalSheets;
        }

        if (StringUtils.equalsIgnoreCase(suffix, "csv")) {
            List<TableField> fields = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String s = reader.readLine();// first line
            String[] split = s.split(",");
            for (String s1 : split) {
                TableField tableFiled = new TableField();
                tableFiled.setFieldName(s1);
                tableFiled.setRemarks(s1);
                tableFiled.setFieldType("TEXT");
                fields.add(tableFiled);
            }
            List<List<String>> data = new ArrayList<>();
            int num = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                if (num > 1000) {
                    break;
                }
                String str;
                line += ",";
                Pattern pCells = Pattern.compile("(\"[^\"]*(\"{2})*[^\"]*\")*[^,]*,");
                Matcher mCells = pCells.matcher(line);
                List<String> cells = new ArrayList();//每行记录一个list
                //读取每个单元格
                while (mCells.find()) {
                    str = mCells.group();
                    str = str.replaceAll("(?sm)\"?([^\"]*(\"{2})*[^\"]*)\"?.*,", "$1");
                    str = str.replaceAll("(?sm)(\"(\"))", "$2");
                    cells.add(str);
                }
                data.add(cells);
                num++;
            }
            ExcelSheetData excelSheetData = new ExcelSheetData();
            String[] fieldArray = fields.stream().map(TableField::getFieldName).toArray(String[]::new);
            excelSheetData.setFields(fields);
            excelSheetData.setData(data);
            excelSheetData.setExcelLabel(filename);
            excelSheetData.setFieldsMd5(Md5Utils.md5(StringUtils.join(fieldArray, ",")));
            excelSheetDataList.add(excelSheetData);
        }

        inputStream.close();
        excelSheetDataList.forEach(excelSheetData -> {
            List<List<String>> data = excelSheetData.getData();
            String[] fieldArray = excelSheetData.getFields().stream().map(TableField::getFieldName).toArray(String[]::new);
            List<Map<String, Object>> jsonArray = new ArrayList<>();
            if (CollectionUtils.isNotEmpty(data)) {
                jsonArray = data.stream().map(ele -> {
                    Map<String, Object> map = new HashMap<>();
                    for (int i = 0; i < fieldArray.length; i++) {
                        map.put(fieldArray[i], i < ele.size() ? ele.get(i) : "");
                    }
                    return map;
                }).collect(Collectors.toList());
            }
            excelSheetData.setFieldsMd5(Md5Utils.md5(StringUtils.join(fieldArray, ",")));
            excelSheetData.setJsonArray(jsonArray);
        });

        return excelSheetDataList;
    }

    private String readCell(Cell cell, boolean cellType, TableField tableField) {
        if (cell == null) {
            return "";
        }
        CellType cellTypeEnum = cell.getCellTypeEnum();
        if (cellTypeEnum.equals(CellType.FORMULA)) {
            try {
                double d = cell.getNumericCellValue();
                try {
                    Double value = d;
                    double eps = 1e-10;
                    if (value - Math.floor(value) < eps) {
                        if (cellType) {
                            if (StringUtils.isEmpty(tableField.getFieldType())
                                    || tableField.getFieldType().equalsIgnoreCase("TEXT")) {
                                tableField.setFieldType("LONG");
                            }
                        }
                        return value.longValue() + "";
                    } else {
                        if (cellType) {
                            tableField.setFieldType("DOUBLE");
                        }
                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setGroupingUsed(false);
                        return nf.format(value);
                    }
                } catch (Exception e) {
                    BigDecimal b = BigDecimal.valueOf(d);
                    return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "";
                }
            } catch (IllegalStateException e) {
                String s = String.valueOf(cell.getRichStringCellValue());
                if (cellType) {
                    tableField.setFieldType("TEXT");
                    tableField.setFieldSize(65533);
                }
                return s;
            }
        }
        if (cellTypeEnum.equals(CellType.STRING)) {
            if (cellType) {
                tableField.setFieldType("TEXT");
                tableField.setFieldSize(65533);
            }
            return cell.getStringCellValue();
        }
        if (cellTypeEnum.equals(CellType.NUMERIC)) {
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
                if (cellType) {
                    tableField.setFieldType("DATETIME");
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    return sdf.format(cell.getDateCellValue());
                } catch (Exception e) {
                    return "";
                }
            } else {
                double d = cell.getNumericCellValue();
                try {
                    Double value = d;
                    double eps = 1e-10;
                    if (value - Math.floor(value) < eps) {
                        if (cellType) {
                            if (StringUtils.isEmpty(tableField.getFieldType())
                                    || tableField.getFieldType().equalsIgnoreCase("TEXT")) {
                                tableField.setFieldType("LONG");
                            }
                        }
                        return value.longValue() + "";
                    } else {
                        if (cellType) {
                            tableField.setFieldType("DOUBLE");
                        }
                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setGroupingUsed(false);
                        return nf.format(value);
                    }
                } catch (Exception e) {
                    BigDecimal b = BigDecimal.valueOf(d);
                    return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue() + "";
                }
            }
        }
        if (cellTypeEnum.equals(CellType.BOOLEAN)) {
            return cell.getBooleanCellValue() ? "1" : "0";
        }
        return "";
    }

    private String saveFile(MultipartFile file, String fileNameUUID) throws Exception {
        String filename = file.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        String dirPath = path + AuthUtils.getUser().getUsername() + "/";
        File p = new File(dirPath);
        if (!p.exists()) {
            p.mkdirs();
        }
        String filePath = dirPath + fileNameUUID + "." + suffix;
        File f = new File(filePath);
        FileOutputStream fileOutputStream = new FileOutputStream(f);
        fileOutputStream.write(file.getBytes());
        fileOutputStream.flush();
        fileOutputStream.close();
        return filePath;
    }

    public Boolean checkEngineTableIsExists(String id) throws Exception {
        Datasource engine = engineService.getDeEngine();
        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(engine);
        QueryProvider qp = ProviderFactory.getQueryProvider(engine.getType());
        datasourceRequest.setQuery(qp.searchTable(TableUtils.tableName(id)));
        List<String[]> data = jdbcProvider.getData(datasourceRequest);
        return CollectionUtils.isNotEmpty(data);
    }

    @Resource
    private UtilMapper utilMapper;

    public void updateDatasetTableStatus() {
        if (this.isUpdatingDatasetTableStatus) {
            return;
        } else {
            this.isUpdatingDatasetTableStatus = true;
        }

        try {
            doUpdate();
        } catch (Exception e) {
        } finally {
            this.isUpdatingDatasetTableStatus = false;
        }
    }

    private void doUpdate() {
        List<QrtzSchedulerState> qrtzSchedulerStates = qrtzSchedulerStateMapper.selectByExample(null);
        List<String> activeQrtzInstances = qrtzSchedulerStates.stream()
                .filter(qrtzSchedulerState -> qrtzSchedulerState.getLastCheckinTime()
                        + qrtzSchedulerState.getCheckinInterval() + 1000 > utilMapper.currentTimestamp())
                .map(QrtzSchedulerStateKey::getInstanceName).collect(Collectors.toList());

        DatasetTableExample example = new DatasetTableExample();
        example.createCriteria().andSyncStatusEqualTo(JobStatus.Underway.name());
        List<DatasetTable> jobStoppeddDatasetTables = new ArrayList<>();
        List<DatasetTable> syncDatasetTables = new ArrayList<>();

        List<DatasetTable> datasetTables = datasetTableMapper.selectByExample(example);
        datasetTables.forEach(datasetTable -> {
            if (StringUtils.isNotEmpty(datasetTable.getQrtzInstance()) && !activeQrtzInstances.contains(datasetTable.getQrtzInstance().substring(0, datasetTable.getQrtzInstance().length() - 13))) {
                jobStoppeddDatasetTables.add(datasetTable);
            } else {
                syncDatasetTables.add(datasetTable);
            }
        });

        datasetTables.forEach(datasetTable -> {
            DatasetTableTaskExample datasetTableTaskExample = new DatasetTableTaskExample();
            DatasetTableTaskExample.Criteria criteria = datasetTableTaskExample.createCriteria();
            criteria.andTableIdEqualTo(datasetTable.getId()).andLastExecStatusEqualTo(JobStatus.Underway.name());
            if (CollectionUtils.isEmpty(dataSetTableTaskService.list(datasetTableTaskExample))) {
                DatasetTable record = new DatasetTable();
                record.setSyncStatus(JobStatus.Error.name());
                example.clear();
                example.createCriteria().andIdEqualTo(datasetTable.getId());
                datasetTableMapper.updateByExampleSelective(record, example);
            }
        });
        if (CollectionUtils.isEmpty(jobStoppeddDatasetTables)) {
            return;
        }

        //Task
        DatasetTableTaskExample datasetTableTaskExample = new DatasetTableTaskExample();
        DatasetTableTaskExample.Criteria criteria = datasetTableTaskExample.createCriteria();
        criteria.andTableIdIn(jobStoppeddDatasetTables.stream().map(DatasetTable::getId).collect(Collectors.toList())).andLastExecStatusEqualTo(JobStatus.Underway.name());
        List<DatasetTableTask> datasetTableTasks = dataSetTableTaskService.list(datasetTableTaskExample);
        if (CollectionUtils.isEmpty(datasetTableTasks)) {
            return;
        }

        dataSetTableTaskService.updateTaskStatus(datasetTableTasks, JobStatus.Error);

        //DatasetTable
        DatasetTable record = new DatasetTable();
        record.setSyncStatus(JobStatus.Error.name());
        example.clear();
        example.createCriteria().andSyncStatusEqualTo(JobStatus.Underway.name())
                .andIdIn(jobStoppeddDatasetTables.stream().map(DatasetTable::getId).collect(Collectors.toList()));
        datasetTableMapper.updateByExampleSelective(record, example);

        //TaskLog
        DatasetTableTaskLog datasetTableTaskLog = new DatasetTableTaskLog();
        datasetTableTaskLog.setStatus(JobStatus.Error.name());
        datasetTableTaskLog.setInfo("Job stopped due to system error.");

        DatasetTableTaskLogExample datasetTableTaskLogExample = new DatasetTableTaskLogExample();
        datasetTableTaskLogExample.createCriteria().andStatusEqualTo(JobStatus.Underway.name())
                .andTableIdIn(datasetTableTasks.stream().map(DatasetTableTask::getTableId).collect(Collectors.toList()));
        datasetTableTaskLogMapper.updateByExampleSelective(datasetTableTaskLog, datasetTableTaskLogExample);

        for (DatasetTableTask datasetTableTask : datasetTableTasks) {
            extractDataService.deleteFile("all_scope", datasetTableTask.getTableId());
            extractDataService.deleteFile("incremental_add", datasetTableTask.getTableId());
            extractDataService.deleteFile("incremental_delete", datasetTableTask.getTableId());
        }
    }

    /*
     * 判断数组中是否有重复的值
     */
    public static boolean checkIsRepeat(String[] array) {
        HashSet<String> hashSet = new HashSet<>();
        for (String s : array) {
            if (StringUtils.isEmpty(s)) {
                throw new RuntimeException(Translator.get("i18n_excel_empty_column"));
            }
            hashSet.add(s);
        }
        return hashSet.size() != array.length;
    }

    public DatasetTable syncDatasetTableField(String id) throws Exception {
        DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(id);
        saveTableField(datasetTable);
        return datasetTable;
    }

    public int updateByExampleSelective(DatasetTable record, DatasetTableExample example) {
        return datasetTableMapper.updateByExampleSelective(record, example);
    }

    private ExpressionDeParser getExpressionDeParser(StringBuilder stringBuilder) {
        ExpressionDeParser expressionDeParser = new ExpressionDeParser(null, stringBuilder) {
            @Override
            public void visit(Parenthesis parenthesis) {
                getBuffer().append("(");
                parenthesis.getExpression().accept(this);
                getBuffer().append(")");
            }

            @Override
            public void visit(OrExpression orExpression) {
                visitBinaryExpr(orExpression, "OR");
            }

            @Override
            public void visit(AndExpression andExpression) {
                visitBinaryExpr(andExpression, "AND");
            }

            @Override
            public void visit(Between between) {
                if (hasVariable(between.getBetweenExpressionStart().toString()) || hasVariable(between.getBetweenExpressionEnd().toString())) {
                    getBuffer().append(SubstitutedSql);
                } else {
                    getBuffer().append(between.getLeftExpression()).append(" BETWEEN ").append(between.getBetweenExpressionStart()).append(" AND ").append(between.getBetweenExpressionEnd());
                }
            }

            @Override
            public void visit(MinorThan minorThan) {
                if (hasVariable(minorThan.getLeftExpression().toString()) || hasVariable(minorThan.getRightExpression().toString())) {
                    getBuffer().append(SubstitutedSql);
                    return;
                }
                getBuffer().append(minorThan.getLeftExpression());
                getBuffer().append(" < ");
                getBuffer().append(minorThan.getRightExpression());
            }

            @Override
            public void visit(MinorThanEquals minorThan) {
                if (hasVariable(minorThan.getLeftExpression().toString()) || hasVariable(minorThan.getRightExpression().toString())) {
                    getBuffer().append(SubstitutedSql);
                    return;
                }
                getBuffer().append(minorThan.getLeftExpression());
                getBuffer().append(" <= ");
                getBuffer().append(minorThan.getRightExpression());
            }

            @Override
            public void visit(GreaterThanEquals minorThan) {
                if (hasVariable(minorThan.getLeftExpression().toString()) || hasVariable(minorThan.getRightExpression().toString())) {
                    getBuffer().append(SubstitutedSql);
                    return;
                }
                getBuffer().append(minorThan.getLeftExpression());
                getBuffer().append(" >= ");
                getBuffer().append(minorThan.getRightExpression());
            }

            @Override
            public void visit(GreaterThan greaterThan) {
                if (hasVariable(greaterThan.getLeftExpression().toString()) || hasVariable(greaterThan.getRightExpression().toString())) {
                    getBuffer().append(SubstitutedSql);
                    return;
                }
                getBuffer().append(greaterThan.getLeftExpression());
                getBuffer().append(" > ");
                getBuffer().append(greaterThan.getRightExpression());
            }

            @Override
            public void visit(ExpressionList expressionList) {
                for (Iterator<Expression> iter = expressionList.getExpressions().iterator(); iter.hasNext(); ) {
                    Expression expression = iter.next();
                    expression.accept(this);
                    if (iter.hasNext()) {
                        buffer.append(", ");
                    }
                }
            }

            @Override
            public void visit(LikeExpression likeExpression) {
                if (hasVariable(likeExpression.toString())) {
                    getBuffer().append(SubstitutedSql);
                    return;
                }

                visitBinaryExpression(likeExpression,
                        (likeExpression.isNot() ? " NOT" : "") + (likeExpression.isCaseInsensitive() ? " ILIKE " : " LIKE "));
                String escape = likeExpression.getEscape();
                if (escape != null) {
                    buffer.append(" ESCAPE '").append(escape).append('\'');
                }
            }

            @Override
            public void visit(InExpression inExpression) {
                if (inExpression.getRightItemsList() != null && hasVariable(inExpression.getRightItemsList().toString())) {
                    stringBuilder.append(SubstitutedSql);
                    return;
                }
                if (inExpression.getRightExpression() != null && inExpression.getRightExpression().toString().equals(SubstitutedParams)) {
                    stringBuilder.append(SubstitutedSql);
                    return;
                }
                inExpression.getLeftExpression().accept(this);
                if (inExpression.isNot()) {
                    getBuffer().append(" " + " NOT IN " + " ");
                } else {
                    getBuffer().append(" IN ");
                }
                if (inExpression.getRightItemsList() != null) {
                    getBuffer().append(inExpression.getRightItemsList());
                }
                if (inExpression.getRightExpression() != null) {
                    getBuffer().append(" ( ");
                    inExpression.getRightExpression().accept(this);
                    getBuffer().append(" )");
                }
            }


            @Override
            public void visit(SubSelect subSelect) {
                StringBuilder stringBuilder = new StringBuilder();
                Expression in = ((PlainSelect) subSelect.getSelectBody()).getWhere();
                if (in instanceof BinaryExpression && hasVariable(in.toString())) {
                    stringBuilder.append(SubstitutedParams);
                } else {
                    in.accept(getExpressionDeParser(stringBuilder));
                }

                try {
                    Expression where = CCJSqlParserUtil.parseCondExpression(stringBuilder.toString());
                    ((PlainSelect) subSelect.getSelectBody()).setWhere(where);
                    getBuffer().append(subSelect.getSelectBody());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            private void visitBinaryExpr(BinaryExpression expr, String operator) {
                boolean hasSubBinaryExpression = false;
                try {
                    BinaryExpression leftBinaryExpression = (BinaryExpression) expr.getLeftExpression();
                    hasSubBinaryExpression = leftBinaryExpression instanceof AndExpression || leftBinaryExpression instanceof OrExpression;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (expr.getLeftExpression() instanceof BinaryExpression && !hasSubBinaryExpression && hasVariable(expr.getLeftExpression().toString())) {
                    getBuffer().append(SubstitutedSql);
                } else {
                    expr.getLeftExpression().accept(this);
                }

                getBuffer().append(" " + operator + " ");
                hasSubBinaryExpression = false;
                try {
                    BinaryExpression rightBinaryExpression = (BinaryExpression) expr.getRightExpression();
                    hasSubBinaryExpression = rightBinaryExpression instanceof AndExpression || rightBinaryExpression instanceof OrExpression;
                    ;
                } catch (Exception e) {
                }
                if (expr.getRightExpression() instanceof BinaryExpression && !hasSubBinaryExpression && hasVariable(expr.getRightExpression().toString())) {
                    getBuffer().append(SubstitutedSql);
                } else {
                    expr.getRightExpression().accept(this);
                }
            }
        };
        return expressionDeParser;
    }

    private static boolean hasVariable(String sql) {
        return sql.contains(SubstitutedParams);
    }

    public void createAppCustomDorisView(String datasetInfo, String tableId) throws Exception {
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(datasetInfo, DataTableInfoDTO.class);
        createDorisView(TableUtils.tableName(tableId), getCustomViewSQL(dataTableInfoDTO,
                dataSetTableUnionService.listByTableId(dataTableInfoDTO.getList().get(0).getTableId())));
    }

    public void createAppUnionDorisView(String datasetInfo, String tableId) throws Exception {
        // save field
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(datasetInfo, DataTableInfoDTO.class);
        Map<String, Object> sqlMap = getUnionSQLDoris(dataTableInfoDTO);
        String sql = (String) sqlMap.get("sql");
        // custom 创建doris视图
        createDorisView(TableUtils.tableName(tableId), sql);
    }

    public void updateDatasetInfo(DatasetTable datasetTable) {
        datasetTableMapper.updateByPrimaryKeySelective(datasetTable);
    }

    public void exportDataset(DataSetExportRequest request, HttpServletResponse response) throws Exception {
        try {
            DatasetRowPermissionsTreeObj tree = null;
            if (StringUtils.isNotEmpty(request.getExpressionTree())) {
                Gson gson = new Gson();
                tree = gson.fromJson(request.getExpressionTree(), DatasetRowPermissionsTreeObj.class);
                permissionsTreeService.getField(tree);
            }
            Datasource datasource = datasourceService.get(request.getDataSourceId());
            int pageSize = (datasource != null && StringUtils.equalsIgnoreCase(datasource.getType(), "es")) ? 10000 : 100000;
            request.setRow(String.valueOf(pageSize));
            Map<String, Object> previewData = getPreviewData(request, 1, pageSize, null, tree);
            List<DatasetTableField> fields = (List<DatasetTableField>) previewData.get("fields");
            List<Map<String, Object>> data = (List<Map<String, Object>>) previewData.get("data");
            // 构建Excel数据格式
            List<List<String>> details = new ArrayList<>();
            List<String> header = new ArrayList<>();
            for (DatasetTableField field : fields) {
                header.add(field.getName());
            }
            details.add(header);
            for (Map<String, Object> obj : data) {
                List<String> row = new ArrayList<>();
                for (DatasetTableField field : fields) {
                    String string = (String) obj.get(field.getDataeaseName());
                    row.add(string);
                }
                details.add(row);
            }
            // 操作Excel
            Workbook wb = new SXSSFWorkbook();
            // Sheet
            Sheet detailsSheet = wb.createSheet("数据");
            //给单元格设置样式
            CellStyle cellStyle = wb.createCellStyle();
            Font font = wb.createFont();
            //设置字体大小
            font.setFontHeightInPoints((short) 12);
            //设置字体加粗
            font.setBold(true);
            //给字体设置样式
            cellStyle.setFont(font);
            //设置单元格背景颜色
            cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            //设置单元格填充样式(使用纯色背景颜色填充)
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            if (CollectionUtils.isNotEmpty(details)) {
                for (int i = 0; i < details.size(); i++) {
                    Row row = detailsSheet.createRow(i);
                    List<String> rowData = details.get(i);
                    if (rowData != null) {
                        for (int j = 0; j < rowData.size(); j++) {
                            Cell cell = row.createCell(j);
                            if (i == 0) {// 头部
                                cell.setCellValue(rowData.get(j));
                                cell.setCellStyle(cellStyle);
                                //设置列的宽度
                                detailsSheet.setColumnWidth(j, 255 * 20);
                            } else {
                                if ((fields.get(j).getDeType().equals(DeTypeConstants.DE_INT) || fields.get(j).getDeType() == DeTypeConstants.DE_FLOAT) && StringUtils.isNotEmpty(rowData.get(j))) {
                                    try {
                                        cell.setCellValue(Double.valueOf(rowData.get(j)));
                                    } catch (Exception e) {
                                        LogUtil.warn("export excel data transform error");
                                    }
                                } else {
                                    cell.setCellValue(rowData.get(j));
                                }
                            }
                        }
                    }
                }
            }
            OutputStream outputStream = response.getOutputStream();
            response.setContentType("application/vnd.ms-excel");
            //文件名称
            response.setHeader("Content-disposition", "attachment;filename=" + request.getFilename() + ".xlsx");
            wb.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            DataEaseException.throwException(e);
        }
    }
}
