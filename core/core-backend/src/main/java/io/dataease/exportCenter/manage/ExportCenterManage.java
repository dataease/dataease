package io.dataease.exportCenter.manage;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.Gson;
import io.dataease.api.chart.dto.ViewDetailField;
import io.dataease.api.chart.request.ChartExcelRequest;
import io.dataease.api.chart.request.ChartExcelRequestInner;
import io.dataease.api.dataset.dto.DataSetExportRequest;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.api.export.BaseExportApi;
import io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO;
import io.dataease.api.xpack.dataFilling.DataFillingApi;
import io.dataease.api.xpack.dataFilling.dto.DataFillFormTableDataRequest;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.chart.dao.auto.mapper.CoreChartViewMapper;
import io.dataease.chart.server.ChartDataServer;
import io.dataease.dataset.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupMapper;
import io.dataease.dataset.manage.*;
import io.dataease.datasource.utils.DatasourceUtils;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.Field2SQLObj;
import io.dataease.engine.trans.Order2SQLObj;
import io.dataease.engine.trans.Table2SQLObj;
import io.dataease.engine.trans.WhereTree2Str;
import io.dataease.engine.utils.Utils;
import io.dataease.exception.DEException;
import io.dataease.exportCenter.dao.auto.entity.CoreExportTask;
import io.dataease.exportCenter.dao.auto.mapper.CoreExportTaskMapper;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.ColumnPermissionItem;
import io.dataease.extensions.view.dto.DatasetRowPermissionsTreeObj;
import io.dataease.i18n.Translator;
import io.dataease.license.config.XpackInteract;
import io.dataease.license.manage.F2CLicLimitedManage;
import io.dataease.model.ExportTaskDTO;
import io.dataease.system.manage.CoreUserManage;
import io.dataease.system.manage.SysParameterManage;
import io.dataease.utils.*;
import io.dataease.visualization.server.DataVisualizationServer;
import io.dataease.websocket.WsMessage;
import io.dataease.websocket.WsService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Transactional(rollbackFor = Exception.class)
public class ExportCenterManage implements BaseExportApi {
    @Resource
    private CoreExportTaskMapper exportTaskMapper;
    @Resource
    private DatasetGroupManage datasetGroupManage;
    @Resource
    DataVisualizationServer dataVisualizationServer;
    @Resource
    private CoreChartViewMapper coreChartViewMapper;
    @Resource
    private PermissionManage permissionManage;
    @Autowired
    private WsService wsService;
    @Autowired(required = false)
    private PluginManageApi pluginManage;
    @Resource
    private SysParameterManage sysParameterManage;
    @Value("${dataease.export.core.size:10}")
    private int core;
    @Value("${dataease.export.max.size:10}")
    private int max;

    @Value("${dataease.export.dataset.limit:100000}")
    private int limit;
    private final static String DATA_URL_TITLE = "data:image/jpeg;base64,";
    private static final String exportData_path = "/opt/dataease2.0/data/exportData/";
    @Value("${dataease.export.page.size:50000}")
    private Integer extractPageSize;
    static private List<String> STATUS = Arrays.asList("SUCCESS", "FAILED", "PENDING", "IN_PROGRESS", "ALL");
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private int keepAliveSeconds = 600;
    private Map<String, Future> Running_Task = new HashMap<>();
    @Resource
    private ChartDataServer chartDataServer;
    @Resource
    private CoreDatasetGroupMapper coreDatasetGroupMapper;
    @Resource
    private CoreUserManage coreUserManage;
    @Resource
    private DatasetSQLManage datasetSQLManage;
    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;
    @Resource
    private DatasetDataManage datasetDataManage;

    @Autowired(required = false)
    private DataFillingApi dataFillingApi = null;

    private DataFillingApi getDataFillingApi() {
        return dataFillingApi;
    }

    @Resource(name = "f2CLicLimitedManage")
    private F2CLicLimitedManage f2CLicLimitedManage;

    @PostConstruct
    public void init() {
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(core);
        scheduledThreadPoolExecutor.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
        scheduledThreadPoolExecutor.setMaximumPoolSize(max);
    }

    @Scheduled(fixedRate = 5000)
    public void checkRunningTask() {
        Iterator<Map.Entry<String, Future>> iterator = Running_Task.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Future> entry = iterator.next();
            if (entry.getValue().isDone()) {
                iterator.remove();
                try {
                    CoreExportTask exportTask = exportTaskMapper.selectById(entry.getKey());
                    ExportTaskDTO exportTaskDTO = new ExportTaskDTO();
                    BeanUtils.copyBean(exportTaskDTO, exportTask);
                    setExportFromName(exportTaskDTO);
                    WsMessage message = new WsMessage(exportTask.getUserId(), "/task-export-topic", exportTaskDTO);
                    wsService.releaseMessage(message);
                } catch (Exception e) {

                }
            }
        }
    }

    public String exportLimit() {
        return String.valueOf(getExportLimit());
    }

    private Long getExportLimit() {
        return Math.min(f2CLicLimitedManage.checkDatasetLimit(), limit);
    }

    public void download(String id, HttpServletResponse response) throws Exception {
        CoreExportTask exportTask = exportTaskMapper.selectById(id);
        OutputStream outputStream = response.getOutputStream();
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-disposition", "attachment;filename=" + exportTask.getFileName());
        InputStream fileInputStream = new FileInputStream(exportData_path + id + "/" + exportTask.getFileName());
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.flush();
        outputStream.close();
        fileInputStream.close();
        response.flushBuffer();
    }

    public void delete(String id) {
        Iterator<Map.Entry<String, Future>> iterator = Running_Task.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Future> entry = iterator.next();
            if (entry.getKey().equalsIgnoreCase(id)) {
                entry.getValue().cancel(true);
                iterator.remove();
            }
        }
        FileUtils.deleteDirectoryRecursively(exportData_path + id);
        exportTaskMapper.deleteById(id);
    }

    public void deleteAll(String type) {
        if (!STATUS.contains(type)) {
            DEException.throwException("无效的状态");
        }
        QueryWrapper<CoreExportTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", AuthUtils.getUser().getUserId());
        if (!type.equalsIgnoreCase("ALL")) {
            queryWrapper.eq("export_status", type);
        }
        List<CoreExportTask> exportTasks = exportTaskMapper.selectList(queryWrapper);
        exportTasks.parallelStream().forEach(exportTask -> {
            Iterator<Map.Entry<String, Future>> iterator = Running_Task.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Future> entry = iterator.next();
                if (entry.getKey().equalsIgnoreCase(exportTask.getId())) {
                    entry.getValue().cancel(true);
                    iterator.remove();
                }
            }
            FileUtils.deleteDirectoryRecursively(exportData_path + exportTask.getId());
            exportTaskMapper.deleteById(exportTask.getId());
        });

    }

    public void delete(List<String> ids) {
        ids.forEach(this::delete);
    }

    public void retry(String id) {
        CoreExportTask exportTask = exportTaskMapper.selectById(id);
        if (!exportTask.getExportStatus().equalsIgnoreCase("FAILED")) {
            DEException.throwException("正在导出中!");
        }
        exportTask.setExportStatus("PENDING");
        exportTask.setExportProgress("0");
        exportTask.setExportMachineName(hostName());
        exportTask.setExportTime(System.currentTimeMillis());
        exportTaskMapper.updateById(exportTask);
        FileUtils.deleteDirectoryRecursively(exportData_path + id);
        if (exportTask.getExportFromType().equalsIgnoreCase("chart")) {
            ChartExcelRequest request = JsonUtil.parseObject(exportTask.getParams(), ChartExcelRequest.class);
            startViewTask(exportTask, request);
        }
        if (exportTask.getExportFromType().equalsIgnoreCase("dataset")) {
            DataSetExportRequest request = JsonUtil.parseObject(exportTask.getParams(), DataSetExportRequest.class);
            startDatasetTask(exportTask, request);
        }
        if (exportTask.getExportFromType().equalsIgnoreCase("data_filling")) {
            HashMap request = JsonUtil.parseObject(exportTask.getParams(), HashMap.class);
            startDataFillingTask(exportTask, request);
        }
    }

    public List<ExportTaskDTO> exportTasks(String status) {
        if (!STATUS.contains(status)) {
            DEException.throwException("Invalid status: " + status);
        }
        QueryWrapper<CoreExportTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", AuthUtils.getUser().getUserId());
        queryWrapper.orderByDesc("export_time");
        List<CoreExportTask> exportTasks = exportTaskMapper.selectList(queryWrapper);
        List<ExportTaskDTO> result = new ArrayList<>();
        exportTasks.forEach(exportTask -> {
            ExportTaskDTO exportTaskDTO = new ExportTaskDTO();
            BeanUtils.copyBean(exportTaskDTO, exportTask);
            if (status.equalsIgnoreCase("ALL") || status.equalsIgnoreCase(exportTaskDTO.getExportStatus())) {
                setExportFromAbsName(exportTaskDTO);
            }
            if (status.equalsIgnoreCase("ALL") || status.equalsIgnoreCase(exportTaskDTO.getExportStatus())) {
                proxy().setOrg(exportTaskDTO);
            }
            result.add(exportTaskDTO);
        });

        return result;
    }

    @XpackInteract(value = "exportCenter", before = false)
    public void setOrg(ExportTaskDTO exportTaskDTO) {
    }

    private ExportCenterManage proxy() {
        return CommonBeanFactory.getBean(ExportCenterManage.class);
    }

    private void setExportFromAbsName(ExportTaskDTO exportTaskDTO) {
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("chart")) {
            exportTaskDTO.setExportFromName(dataVisualizationServer.getAbsPath(exportTaskDTO.getExportFrom()));
        }
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("dataset")) {
            List<String> fullName = new ArrayList<>();
            datasetGroupManage.geFullName(Long.valueOf(exportTaskDTO.getExportFrom()), fullName);
            Collections.reverse(fullName);
            List<String> finalFullName = fullName;
            exportTaskDTO.setExportFromName(String.join("/", finalFullName));
        }
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("data_filling")) {
            List<String> fullName = new ArrayList<>();
            getDataFillingApi().geFullName(Long.valueOf(exportTaskDTO.getExportFrom()), fullName);
            Collections.reverse(fullName);
            List<String> finalFullName = fullName;
            exportTaskDTO.setExportFromName(String.join("/", finalFullName));
        }
    }

    private void setExportFromName(ExportTaskDTO exportTaskDTO) {
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("chart")) {
            exportTaskDTO.setExportFromName(coreChartViewMapper.selectById(exportTaskDTO.getExportFrom()).getTitle());
        }
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("dataset")) {
            exportTaskDTO.setExportFromName(coreDatasetGroupMapper.selectById(exportTaskDTO.getExportFrom()).getName());
        }
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("data_filling")) {
            exportTaskDTO.setExportFromName(getDataFillingApi().get(Long.parseLong(exportTaskDTO.getExportFrom())).getName());
        }
    }

    private String hostName() {
        String hostname = null;
        try {
            InetAddress localMachine = InetAddress.getLocalHost();
            hostname = localMachine.getHostName();
        } catch (Exception e) {
            DEException.throwException("请设置主机名！");
        }
        return hostname;
    }

    public void addTask(String exportFrom, String exportFromType, ChartExcelRequest request) {
        CoreExportTask exportTask = new CoreExportTask();
        exportTask.setId(UUID.randomUUID().toString());
        exportTask.setUserId(AuthUtils.getUser().getUserId());
        exportTask.setExportFrom(exportFrom);
        exportTask.setExportFromType(exportFromType);
        exportTask.setExportStatus("PENDING");
        exportTask.setFileName(request.getViewName() + ".xlsx");
        exportTask.setExportProgress("0");
        exportTask.setExportTime(System.currentTimeMillis());
        exportTask.setParams(JsonUtil.toJSONString(request).toString());
        exportTask.setExportMachineName(hostName());
        exportTaskMapper.insert(exportTask);
        startViewTask(exportTask, request);
    }

    public void addTask(Long exportFrom, String exportFromType, DataSetExportRequest request) {
        CoreExportTask exportTask = new CoreExportTask();
        exportTask.setId(UUID.randomUUID().toString());
        exportTask.setUserId(AuthUtils.getUser().getUserId());
        exportTask.setExportFrom(String.valueOf(exportFrom));
        exportTask.setExportFromType(exportFromType);
        exportTask.setExportStatus("PENDING");
        exportTask.setFileName(request.getFilename() + ".xlsx");
        exportTask.setExportProgress("0");
        exportTask.setExportTime(System.currentTimeMillis());
        exportTask.setParams(JsonUtil.toJSONString(request).toString());
        exportTask.setExportMachineName(hostName());
        exportTaskMapper.insert(exportTask);
        startDatasetTask(exportTask, request);
    }

    @Override
    public void addTask(String exportFromId, String exportFromType, HashMap<String, Object> request, Long userId, Long org) {
        CoreExportTask exportTask = new CoreExportTask();
        request.put("org", org);
        exportTask.setId(UUID.randomUUID().toString());
        exportTask.setUserId(userId);
        exportTask.setExportFrom(exportFromId);
        exportTask.setExportFromType(exportFromType);
        exportTask.setExportStatus("PENDING");
        exportTask.setFileName(request.get("name") + ".xlsx");
        exportTask.setExportProgress("0");
        exportTask.setExportTime(System.currentTimeMillis());
        exportTask.setParams(JsonUtil.toJSONString(request).toString());
        exportTask.setExportMachineName(hostName());
        exportTaskMapper.insert(exportTask);
        if (StringUtils.equals(exportFromType, "data_filling")) {
            startDataFillingTask(exportTask, request);
        }
    }

    private void startDataFillingTask(CoreExportTask exportTask, HashMap<String, Object> request) {

        if (ObjectUtils.isEmpty(getDataFillingApi())) {
            return;
        }

        String dataPath = exportData_path + exportTask.getId();
        File directory = new File(dataPath);
        boolean isCreated = directory.mkdir();
        TokenUserBO tokenUserBO = AuthUtils.getUser();
        Future future = scheduledThreadPoolExecutor.submit(() -> {
            AuthUtils.setUser(tokenUserBO);
            try {
                exportTask.setExportStatus("IN_PROGRESS");
                exportTaskMapper.updateById(exportTask);

                getDataFillingApi().writeExcel(dataPath + "/" + exportTask.getFileName(),
                        new DataFillFormTableDataRequest()
                                .setId(Long.parseLong(exportTask.getExportFrom()))
                                .setWithoutLogs(true)
                        , exportTask.getUserId(), Long.parseLong(request.get("org").toString()));


                exportTask.setExportProgress("100");
                exportTask.setExportStatus("SUCCESS");

                setFileSize(dataPath + "/" + exportTask.getFileName(), exportTask);
            } catch (Exception e) {
                exportTask.setMsg(e.getMessage());
                LogUtil.error("Failed to export data", e);
                exportTask.setExportStatus("FAILED");
            } finally {
                exportTaskMapper.updateById(exportTask);
            }
        });
        Running_Task.put(exportTask.getId(), future);
    }


    private void startDatasetTask(CoreExportTask exportTask, DataSetExportRequest request) {
        String dataPath = exportData_path + exportTask.getId();
        File directory = new File(dataPath);
        boolean isCreated = directory.mkdir();

        TokenUserBO tokenUserBO = AuthUtils.getUser();
        Future future = scheduledThreadPoolExecutor.submit(() -> {
            AuthUtils.setUser(tokenUserBO);
            try {
                exportTask.setExportStatus("IN_PROGRESS");
                exportTaskMapper.updateById(exportTask);
                CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(exportTask.getExportFrom());
                if (coreDatasetGroup == null) {
                    throw new Exception("Not found dataset group: " + exportTask.getExportFrom());
                }
                DatasetGroupInfoDTO dto = new DatasetGroupInfoDTO();
                BeanUtils.copyBean(dto, coreDatasetGroup);
                dto.setUnionSql(null);
                List<UnionDTO> unionDTOList = JsonUtil.parseList(coreDatasetGroup.getInfo(), new TypeReference<>() {
                });
                dto.setUnion(unionDTOList);
                List<DatasetTableFieldDTO> dsFields = datasetTableFieldManage.selectByDatasetGroupId(Long.valueOf(exportTask.getExportFrom()));
                List<DatasetTableFieldDTO> allFields = dsFields.stream().map(ele -> {
                    DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
                    BeanUtils.copyBean(datasetTableFieldDTO, ele);
                    datasetTableFieldDTO.setFieldShortName(ele.getDataeaseName());
                    return datasetTableFieldDTO;
                }).collect(Collectors.toList());

                Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(dto, null);
                String sql = (String) sqlMap.get("sql");
                if (ObjectUtils.isEmpty(allFields)) {
                    DEException.throwException(Translator.get("i18n_no_fields"));
                }
                Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
                allFields = permissionManage.filterColumnPermissions(allFields, desensitizationList, dto.getId(), null);
                if (ObjectUtils.isEmpty(allFields)) {
                    DEException.throwException(Translator.get("i18n_no_column_permission"));
                }
                dto.setAllFields(allFields);
                datasetDataManage.buildFieldName(sqlMap, allFields);
                Map<Long, DatasourceSchemaDTO> dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
                DatasourceUtils.checkDsStatus(dsMap);
                List<String> dsList = new ArrayList<>();
                for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
                    dsList.add(next.getValue().getType());
                }
                boolean needOrder = Utils.isNeedOrder(dsList);
                boolean crossDs = Utils.isCrossDs(dsMap);
                if (!crossDs) {
                    if (datasetDataManage.notFullDs.contains(dsMap.entrySet().iterator().next().getValue().getType()) && (boolean) sqlMap.get("isFullJoin")) {
                        DEException.throwException(Translator.get("i18n_not_full"));
                    }
                    sql = Utils.replaceSchemaAlias(sql, dsMap);
                }
                List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = new ArrayList<>();
                TokenUserBO user = AuthUtils.getUser();
                if (user != null) {
                    rowPermissionsTree = permissionManage.getRowPermissionsTree(dto.getId(), user.getUserId());
                }
                if (StringUtils.isNotEmpty(request.getExpressionTree())) {
                    Gson gson = new Gson();
                    DatasetRowPermissionsTreeObj datasetRowPermissionsTreeObj = JsonUtil.parseObject(request.getExpressionTree(), DatasetRowPermissionsTreeObj.class);
                    permissionManage.getField(datasetRowPermissionsTreeObj);
                    DataSetRowPermissionsTreeDTO dataSetRowPermissionsTreeDTO = new DataSetRowPermissionsTreeDTO();
                    dataSetRowPermissionsTreeDTO.setTree(datasetRowPermissionsTreeObj);
                    dataSetRowPermissionsTreeDTO.setExportData(true);
                    rowPermissionsTree.add(dataSetRowPermissionsTreeDTO);
                }

                Provider provider;
                if (crossDs) {
                    provider = ProviderFactory.getDefaultProvider();
                } else {
                    provider = ProviderFactory.getProvider(dsList.getFirst());
                }
                SQLMeta sqlMeta = new SQLMeta();
                Table2SQLObj.table2sqlobj(sqlMeta, null, "(" + sql + ")", crossDs);
                Field2SQLObj.field2sqlObj(sqlMeta, allFields, allFields, crossDs, dsMap, Utils.getParams(allFields), null, pluginManage);
                WhereTree2Str.transFilterTrees(sqlMeta, rowPermissionsTree, allFields, crossDs, dsMap, Utils.getParams(allFields), null, pluginManage);
                Order2SQLObj.getOrders(sqlMeta, dto.getSortFields(), allFields, crossDs, dsMap, Utils.getParams(allFields), null, pluginManage);

                String replaceSql = provider.rebuildSQL(SQLProvider.createQuerySQL(sqlMeta, false, false, false), sqlMeta, crossDs, dsMap);
                Long totalCount = datasetDataManage.getDatasetTotal(dto, replaceSql, null);
                Long curLimit = getExportLimit();
                totalCount = totalCount > curLimit ? curLimit : totalCount;
                Long totalPage = (totalCount / extractPageSize) + (totalCount % extractPageSize > 0 ? 1 : 0);

                Workbook wb = new SXSSFWorkbook();
                FileOutputStream fileOutputStream = new FileOutputStream(dataPath + "/" + request.getFilename() + ".xlsx");
                Sheet detailsSheet = wb.createSheet("数据");
                List<List<String>> details = new ArrayList<>();

                for (Integer p = 0; p < totalPage; p++) {
                    String querySQL = SQLProvider.createQuerySQLWithLimit(sqlMeta, false, needOrder, false, p * extractPageSize + extractPageSize, extractPageSize);
                    if (totalPage == 1) {
                        querySQL = SQLProvider.createQuerySQLWithLimit(sqlMeta, false, needOrder, false, 0, totalCount.intValue());
                    }
                    querySQL = provider.rebuildSQL(querySQL, sqlMeta, crossDs, dsMap);
                    DatasourceRequest datasourceRequest = new DatasourceRequest();
                    datasourceRequest.setQuery(querySQL);
                    datasourceRequest.setDsList(dsMap);
                    Map<String, Object> previewData = datasetDataManage.buildPreviewData(provider.fetchResultField(datasourceRequest), allFields, desensitizationList);
                    List<Map<String, Object>> data = (List<Map<String, Object>>) previewData.get("data");
                    if (p == 0L) {
                        CellStyle cellStyle = wb.createCellStyle();
                        Font font = wb.createFont();
                        font.setFontHeightInPoints((short) 12);
                        font.setBold(true);
                        cellStyle.setFont(font);
                        cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                        List<String> header = new ArrayList<>();
                        for (DatasetTableFieldDTO field : allFields) {
                            header.add(field.getName());
                        }

                        details.add(header);
                        for (Map<String, Object> obj : data) {
                            List<String> row = new ArrayList<>();
                            for (DatasetTableFieldDTO field : allFields) {
                                String string = (String) obj.get(field.getDataeaseName());
                                row.add(string);
                            }
                            details.add(row);
                        }
                        if (CollectionUtils.isNotEmpty(details)) {
                            for (int i = 0; i < details.size(); i++) {
                                Row row = detailsSheet.createRow(i);
                                List<String> rowData = details.get(i);
                                if (rowData != null) {
                                    for (int j = 0; j < rowData.size(); j++) {
                                        Cell cell = row.createCell(j);
                                        if (i == 0) {
                                            cell.setCellValue(rowData.get(j));
                                            cell.setCellStyle(cellStyle);
                                            detailsSheet.setColumnWidth(j, 255 * 20);
                                        } else {
                                            cell.setCellValue(rowData.get(j));
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        details.clear();
                        for (Map<String, Object> obj : data) {
                            List<String> row = new ArrayList<>();
                            for (DatasetTableFieldDTO field : allFields) {
                                String string = (String) obj.get(field.getDataeaseName());
                                row.add(string);
                            }
                            details.add(row);
                        }
                        int lastNum = detailsSheet.getLastRowNum();
                        for (int i = 0; i < details.size(); i++) {
                            Row row = detailsSheet.createRow(i + lastNum + 1);
                            List<String> rowData = details.get(i);
                            if (rowData != null) {
                                for (int j = 0; j < rowData.size(); j++) {
                                    Cell cell = row.createCell(j);
                                    cell.setCellValue(rowData.get(j));
                                }
                            }
                        }
                    }
                    exportTask.setExportStatus("IN_PROGRESS");
                    double exportRogress = (double) ((double) p / (double) totalPage);
                    DecimalFormat df = new DecimalFormat("#.##");
                    String formattedResult = df.format(exportRogress * 100);
                    exportTask.setExportProgress(formattedResult);
                    exportTaskMapper.updateById(exportTask);
                }
                wb.write(fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                wb.close();
                exportTask.setExportProgress("100");
                exportTask.setExportStatus("SUCCESS");
                setFileSize(dataPath + "/" + request.getFilename() + ".xlsx", exportTask);

            } catch (Exception e) {
                LogUtil.error("Failed to export data", e);
                exportTask.setMsg(e.getMessage());
                exportTask.setExportStatus("FAILED");
            } finally {
                exportTaskMapper.updateById(exportTask);
            }
        });
        Running_Task.put(exportTask.getId(), future);
    }

    private void startViewTask(CoreExportTask exportTask, ChartExcelRequest request) {
        String dataPath = exportData_path + exportTask.getId();
        File directory = new File(dataPath);
        boolean isCreated = directory.mkdir();
        TokenUserBO tokenUserBO = AuthUtils.getUser();
        Future future = scheduledThreadPoolExecutor.submit(() -> {
            AuthUtils.setUser(tokenUserBO);
            try {
                exportTask.setExportStatus("IN_PROGRESS");
                exportTaskMapper.updateById(exportTask);
                chartDataServer.findExcelData(request);

                Workbook wb = new SXSSFWorkbook();

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

                if (CollectionUtils.isEmpty(request.getMultiInfo())) {
                    List<Object[]> details = request.getDetails();
                    Integer[] excelTypes = request.getExcelTypes();
                    details.add(0, request.getHeader());
                    ViewDetailField[] detailFields = request.getDetailFields();
                    Object[] header = request.getHeader();

                    //明细sheet
                    Sheet detailsSheet = wb.createSheet("数据");

                    ChartDataServer.setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes);
                } else {
                    //多个sheet
                    for (int i = 0; i < request.getMultiInfo().size(); i++) {
                        ChartExcelRequestInner requestInner = request.getMultiInfo().get(i);

                        List<Object[]> details = requestInner.getDetails();
                        Integer[] excelTypes = requestInner.getExcelTypes();
                        details.add(0, requestInner.getHeader());
                        ViewDetailField[] detailFields = requestInner.getDetailFields();
                        Object[] header = requestInner.getHeader();

                        //明细sheet
                        Sheet detailsSheet = wb.createSheet("数据 " + (i + 1));

                        ChartDataServer.setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes);
                    }
                }
                try (FileOutputStream outputStream = new FileOutputStream(dataPath + "/" + request.getViewName() + ".xlsx")) {
                    wb.write(outputStream);
                    outputStream.flush();
                }
                wb.close();
                exportTask.setExportProgress("100");
                exportTask.setExportStatus("SUCCESS");
                setFileSize(dataPath + "/" + request.getViewName() + ".xlsx", exportTask);
            } catch (Exception e) {
                exportTask.setMsg(e.getMessage());
                LogUtil.error("Failed to export data", e);
                exportTask.setExportStatus("FAILED");
            } finally {
                exportTaskMapper.updateById(exportTask);
            }
        });
        Running_Task.put(exportTask.getId(), future);
    }


    private void setFileSize(String filePath, CoreExportTask exportTask) {
        File file = new File(filePath);
        long length = file.length();
        String unit = "Mb";
        Double size = 0.0;
        if ((double) length / 1024 / 1024 > 1) {
            if ((double) length / 1024 / 1024 / 1024 > 1) {
                unit = "Gb";
                size = Double.valueOf(String.format("%.2f", (double) length / 1024 / 1024 / 1024));
            } else {
                size = Double.valueOf(String.format("%.2f", (double) length / 1024 / 1024));
            }

        } else {
            unit = "Kb";
            size = Double.valueOf(String.format("%.2f", (double) length / 1024));
        }
        exportTask.setFileSize(size);
        exportTask.setFileSizeUnit(unit);
    }


    private static final String LOG_RETENTION = "30";

    public void cleanLog() {
        String key = "basic.exportFileLiveTime";
        String val = sysParameterManage.singleVal(key);
        if (StringUtils.isBlank(val)) {
            DEException.throwException("未获取到文件保留时间");
        }
        QueryWrapper<CoreExportTask> queryWrapper = new QueryWrapper<>();
        long expTime = Long.parseLong(val) * 24L * 3600L * 1000L;
        long threshold = System.currentTimeMillis() - expTime;
        queryWrapper.lt("export_time", threshold);
        exportTaskMapper.selectList(queryWrapper).forEach(coreExportTask -> {
            delete(coreExportTask.getId());
        });

    }

}

