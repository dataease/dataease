package io.dataease.exportCenter.manage;


import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.chart.dto.DeSortField;
import io.dataease.api.chart.dto.ViewDetailField;
import io.dataease.api.chart.request.ChartExcelRequest;
import io.dataease.api.chart.request.ChartExcelRequestInner;
import io.dataease.api.dataset.dto.DataSetExportRequest;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO;
import io.dataease.api.permissions.user.api.UserApi;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.api.xpack.dataFilling.DataFillingApi;
import io.dataease.api.xpack.dataFilling.dto.DataFillFormTableDataRequest;
import io.dataease.chart.dao.auto.mapper.CoreChartViewRepository;
import io.dataease.chart.server.ChartDataServer;
import io.dataease.commons.utils.ExcelWatermarkUtils;
import io.dataease.constant.DeTypeConstants;
import io.dataease.constant.LogOT;
import io.dataease.constant.LogST;
import io.dataease.dao.auto.entity.CoreChartView;
import io.dataease.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dao.auto.entity.CoreExportTask;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupRepository;
import io.dataease.dataset.manage.*;
import io.dataease.datasource.utils.DatasourceUtils;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.*;
import io.dataease.engine.utils.Utils;
import io.dataease.exception.DEException;
import io.dataease.exportCenter.dao.auto.mapper.CoreExportTaskRepository;
import io.dataease.exportCenter.util.ExportCenterUtils;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.dto.TableFieldWithValue;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import io.dataease.extensions.view.dto.ColumnPermissionItem;
import io.dataease.extensions.view.dto.DatasetRowPermissionsTreeObj;
import io.dataease.i18n.Translator;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.log.DeLog;
import io.dataease.model.ExportTaskDTO;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.LogUtil;
import io.dataease.visualization.dao.auto.entity.VisualizationWatermark;
import io.dataease.visualization.dao.auto.mapper.VisualizationWatermarkRepository;
import io.dataease.visualization.dto.WatermarkContentDTO;
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
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Transactional(rollbackFor = Exception.class)
public class ExportCenterDownLoadManage {
    @Resource
    private PermissionManage permissionManage;
    @Autowired
    private WsService wsService;
    @Autowired(required = false)
    private PluginManageApi pluginManage;
    @Value("${dataease.export.core.size:10}")
    private int core;
    @Value("${dataease.export.max.size:10}")
    private int max;

    @Value("${dataease.path.exportData:/opt/dataease3.0/data/exportData/}")
    private String exportData_path;
    @Value("${dataease.export.page.size:50000}")
    private Integer extractPageSize;
    static private List<String> STATUS = Arrays.asList("SUCCESS", "FAILED", "PENDING", "IN_PROGRESS", "ALL");
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private int keepAliveSeconds = 600;
    private Map<String, Future> Running_Task = new HashMap<>();
    @Resource
    private ChartDataServer chartDataServer;
    @Resource
    private VisualizationWatermarkRepository watermarkRepository;
    @Resource
    private DatasetSQLManage datasetSQLManage;
    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;
    @Resource
    private DatasetDataManage datasetDataManage;
    @Resource
    private CoreExportTaskRepository coreExportTaskRepository;
    private final Long sheetLimit = 1000000L;
    @Autowired(required = false)
    private DataFillingApi dataFillingApi = null;
    @Resource
    private CoreChartViewRepository coreChartViewRepository;
    @Resource
    private CoreDatasetGroupRepository coreDatasetGroupRepository;
    @Resource
    private DatasetGroupManage datasetGroupManage;

    private void applyPreparedParams(DatasourceRequest datasourceRequest, Map<String, Object> sqlMap) {
        if (sqlMap == null) {
            return;
        }
        List<TableFieldWithValue> tableFieldWithValues = (List<TableFieldWithValue>) sqlMap.get("tableFieldWithValues");
        if (CollectionUtils.isEmpty(tableFieldWithValues)) {
            return;
        }
        datasourceRequest.setTableFieldWithValues(tableFieldWithValues.stream().map(TableFieldWithValue::copy).toList());
    }

    private DataFillingApi getDataFillingApi() {
        return dataFillingApi;
    }

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
                    CoreExportTask exportTask = coreExportTaskRepository.findById(entry.getKey()).orElse(null);
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

    private void setExportFromName(ExportTaskDTO exportTaskDTO) {
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("chart")) {
            exportTaskDTO.setExportFromName(coreChartViewRepository.findById(exportTaskDTO.getExportFrom()).orElse(new CoreChartView()).getTitle());
            if (StringUtils.isNotEmpty(exportTaskDTO.getExportFromName())) {
                ChartExcelRequest request = JsonUtil.parseObject(exportTaskDTO.getParams(), ChartExcelRequest.class);
                if (request.getViewInfo() != null && request.getViewInfo().getId() != null && request.getViewInfo().getId().equals(exportTaskDTO.getExportFrom())) {
                    exportTaskDTO.setExportFromName(request.getViewInfo().getTitle());
                }
            }
        }
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("dataset")) {
            exportTaskDTO.setExportFromName(coreDatasetGroupRepository.findById(exportTaskDTO.getExportFrom()).orElse(new CoreDatasetGroup()).getName());
        }
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("data_filling")) {
            exportTaskDTO.setExportFromName(getDataFillingApi().get(exportTaskDTO.getExportFrom()).getName());
        }
    }

    @DeLog(id = "#p1", ot = LogOT.EXPORT, st = LogST.DATA_FILLING)
    public void startDataFillingTask(ExportTaskFileTarget exportTarget, Long exportFrom, Long userId, HashMap<String, Object> request) {
        if (ObjectUtils.isEmpty(getDataFillingApi())) {
            return;
        }
        exportTarget.createParentDirectory();
        Future future = scheduledThreadPoolExecutor.submit(() -> {
            try {
                updateExportTask(exportTarget.taskId(), "IN_PROGRESS", null, null, null, null);
                getDataFillingApi().writeExcel(exportTarget.filePath(), new DataFillFormTableDataRequest().setId(exportFrom).setWithoutLogs(true), userId, Long.parseLong(request.get("org").toString()));
                updateExportTaskSuccess(exportTarget, "100");
            } catch (Exception e) {
                LogUtil.error("Failed to export data", e);
                updateExportTask(exportTarget.taskId(), "FAILED", null, e.getMessage(), null, null);
            }
        });
        Running_Task.put(exportTarget.taskId(), future);
    }

    @DeLog(id = "#p1", ot = LogOT.EXPORT, st = LogST.DATASET)
    public void startDatasetTask(ExportTaskFileTarget exportTarget, Long exportFrom, DataSetExportRequest request) {
        exportTarget.createParentDirectory();
        Long uid = V3UserUtil.getUid();
        Future future = scheduledThreadPoolExecutor.submit(() -> {
            V3UserUtil.setUid(uid);
            LicenseUtil.validate();
            try {
                updateExportTask(exportTarget.taskId(), "IN_PROGRESS", null, null, null, null);
                CoreDatasetGroup coreDatasetGroup = coreDatasetGroupRepository.findById(exportFrom).orElse(null);
                if (coreDatasetGroup == null) {
                    throw new Exception("Not found dataset group: " + exportFrom);
                }
                DatasetGroupInfoDTO dto = new DatasetGroupInfoDTO();
                BeanUtils.copyBean(dto, coreDatasetGroup);
                dto.setUnionSql(null);
                List<UnionDTO> unionDTOList = JsonUtil.parseList(coreDatasetGroup.getInfo(), new TypeReference<>() {
                });
                dto.setUnion(unionDTOList);
                List<DatasetTableFieldDTO> dsFields = datasetTableFieldManage.selectByDatasetGroupId(exportFrom);
                List<DatasetTableFieldDTO> allFields = dsFields.stream().map(ele -> {
                    DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
                    BeanUtils.copyBean(datasetTableFieldDTO, ele);
                    datasetTableFieldDTO.setFieldShortName(ele.getDataeaseName());
                    return datasetTableFieldDTO;
                }).collect(Collectors.toList());
                if (ObjectUtils.isEmpty(allFields)) {
                    DEException.throwException(Translator.get("i18n_no_fields"));
                }
                dto.setAllFields(allFields);

                Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(dto, null);
                String sql = (String) sqlMap.get("sql");
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
                boolean crossDs = dto.getIsCross();
                if (!crossDs) {
                    if (datasetDataManage.notFullDs.contains(dsMap.entrySet().iterator().next().getValue().getType()) && (boolean) sqlMap.get("isFullJoin")) {
                        DEException.throwException(Translator.get("i18n_not_full"));
                    }
                    sql = Utils.replaceSchemaAlias(sql, dsMap);
                }
                List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = new ArrayList<>();
                if (uid != null) {
                    rowPermissionsTree = permissionManage.getRowPermissionsTree(dto.getId(), uid);
                }
                if (StringUtils.isNotEmpty(request.getExpressionTree())) {
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
                List<DeSortField> sortFields = new ArrayList<>();
                for (DatasetTableFieldDTO field : allFields) {
                    if (field.getOrderChecked()) {
                        DeSortField sortField = new DeSortField();
                        BeanUtils.copyBean(sortField, field);
                        sortField.setOrderDirection("asc");
                        sortFields.add(sortField);
                    }
                }
                dto.setSortFields(sortFields);
                DatasetOrder2SQLObj.getOrders(sqlMeta, dto.getSortFields(), allFields);
                String replaceSql = provider.rebuildSQL(SQLProvider.createQuerySQL(sqlMeta, false, false, false), sqlMeta, crossDs, dsMap);
                Long totalCount = datasetDataManage.getDatasetTotal(dto, replaceSql, null);
                Long curLimit = ExportCenterUtils.getExportLimit("dataset");
                totalCount = totalCount > curLimit ? curLimit : totalCount;

                Long sheetCount = (totalCount / sheetLimit) + (totalCount % sheetLimit > 0 ? 1 : 0);
                Workbook wb = new SXSSFWorkbook();
                CellStyle cellStyle = wb.createCellStyle();
                Font font = wb.createFont();
                font.setFontHeightInPoints((short) 12);
                font.setBold(true);
                cellStyle.setFont(font);
                cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                for (Long s = 1L; s < sheetCount + 1; s++) {
                    Long sheetSize;
                    if (s.equals(sheetCount)) {
                        sheetSize = totalCount - (s - 1) * sheetLimit;
                    } else {
                        sheetSize = sheetLimit;
                    }
                    Long pageSize = (sheetSize / extractPageSize) + (sheetSize % extractPageSize > 0 ? 1 : 0);
                    Sheet detailsSheet = null;
                    List<List<String>> details = new ArrayList<>();
                    for (Long p = 0L; p < pageSize; p++) {
                        int beforeCount = (int) ((s - 1) * sheetLimit);
                        String querySQL = SQLProvider.createQuerySQLWithLimit(sqlMeta, false, needOrder, false, beforeCount + p.intValue() * extractPageSize, extractPageSize);
                        if (pageSize == 1) {
                            querySQL = SQLProvider.createQuerySQLWithLimit(sqlMeta, false, needOrder, false, 0, sheetSize.intValue());
                        }
                        querySQL = provider.rebuildSQL(querySQL, sqlMeta, crossDs, dsMap);
                        DatasourceRequest datasourceRequest = new DatasourceRequest();
                        datasourceRequest.setQuery(querySQL);
                        datasourceRequest.setDsList(dsMap);
                        datasourceRequest.setIsCross(coreDatasetGroup.getIsCross());
                        applyPreparedParams(datasourceRequest, sqlMap);
                        Map<String, Object> previewData = datasetDataManage.buildPreviewData(provider.fetchResultField(datasourceRequest), allFields, desensitizationList, false);
                        List<Map<String, Object>> data = (List<Map<String, Object>>) previewData.get("data");
                        if (p.equals(0L)) {
                            detailsSheet = wb.createSheet("数据" + s);
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
                                                if ((allFields.get(j).getDeType().equals(DeTypeConstants.DE_INT) || allFields.get(j).getDeType() == DeTypeConstants.DE_FLOAT) && StringUtils.isNotEmpty(rowData.get(j))) {
                                                    try {
                                                        cell.setCellValue(Double.valueOf(rowData.get(j)));
                                                    } catch (Exception e) {
                                                        cell.setCellValue(rowData.get(j));
                                                    }
                                                } else {
                                                    cell.setCellValue(rowData.get(j));
                                                }
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
                                        if ((allFields.get(j).getDeType().equals(DeTypeConstants.DE_INT) || allFields.get(j).getDeType() == DeTypeConstants.DE_FLOAT) && StringUtils.isNotEmpty(rowData.get(j))) {
                                            try {
                                                cell.setCellValue(Double.valueOf(rowData.get(j)));
                                            } catch (Exception e) {
                                                cell.setCellValue(rowData.get(j));
                                            }
                                        } else {
                                            cell.setCellValue(rowData.get(j));
                                        }
                                    }
                                }
                            }
                        }
                        double exportRogress2 = (double) ((double) s - 1) / ((double) sheetCount);
                        double exportRogress = (double) ((double) (p + 1) / (double) pageSize) * ((double) 1 / sheetCount);
                        DecimalFormat df = new DecimalFormat("#.##");
                        String formattedResult = df.format((exportRogress + exportRogress2) * 100);
                        updateExportTask(exportTarget.taskId(), "IN_PROGRESS", formattedResult, null, null, null);
                    }
                }
                this.addWatermarkTools(wb);
                try (OutputStream fileOutputStream = exportTarget.newOutputStream()) {
                    wb.write(fileOutputStream);
                    fileOutputStream.flush();
                }
                wb.close();
                updateExportTaskSuccess(exportTarget, "100");

            } catch (Exception e) {
                LogUtil.error("Failed to export data", e);
                updateExportTask(exportTarget.taskId(), "FAILED", null, e.getMessage(), null, null);
            }
        });
        Running_Task.put(exportTarget.taskId(), future);
    }

    @DeLog(id = "#p1.viewId", ot = LogOT.EXPORT, st = LogST.PANEL)
    public void startPanelViewTask(ExportTaskFileTarget exportTarget, ChartExcelRequest request) {
        startViewTask(exportTarget, request);
    }

    @DeLog(id = "#p1.viewId", ot = LogOT.EXPORT, st = LogST.SCREEN)
    public void startDataVViewTask(ExportTaskFileTarget exportTarget, ChartExcelRequest request) {
        startViewTask(exportTarget, request);
    }

    public static void removeColumn(List<Object[]> list, List<Integer> columnIndexs) {
        if (list == null || list.isEmpty() || columnIndexs == null || columnIndexs.isEmpty()) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            Object[] originalRow = list.get(i);
            Object[] newRow = new Object[originalRow.length - columnIndexs.size()];
            int newIndex = 0;
            for (int j = 0; j < originalRow.length; j++) {
                if (!columnIndexs.contains(j)) {
                    newRow[newIndex++] = originalRow[j];
                }
            }
            list.set(i, newRow);
        }
    }

    public void startViewTask(ExportTaskFileTarget exportTarget, ChartExcelRequest request) {
        exportTarget.createParentDirectory();
        Future future = scheduledThreadPoolExecutor.submit(() -> {
            LicenseUtil.validate();
            try {
                updateExportTask(exportTarget.taskId(), "IN_PROGRESS", null, null, null, null);
                Workbook wb = new SXSSFWorkbook();
                CellStyle cellStyle = wb.createCellStyle();
                Font font = wb.createFont();
                font.setFontHeightInPoints((short) 12);
                font.setBold(true);
                cellStyle.setFont(font);
                cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                List<Object[]> details = new ArrayList<>();
                Sheet detailsSheet;
                Integer sheetIndex = 1;
                if ("dataset".equals(request.getDownloadType()) || request.getViewInfo().getType().equalsIgnoreCase("table-info") || request.getViewInfo().getType().equalsIgnoreCase("table-normal")) {
                    boolean summaryEnabled = !"dataset".equals(request.getDownloadType()) && ChartDataServer.isSummaryEnabled(request.getViewInfo());
                    ChartDataServer.SummaryConfig summaryConfig = null;
                    ChartDataServer.SummaryAccumulator summaryAcc = null;
                    List<ChartViewFieldDTO> allExportColumns = null;
                    Map<String, java.math.BigDecimal> customSumResult = null;
                    if (summaryEnabled) {
                        summaryConfig = ChartDataServer.parseSummaryConfig(request.getViewInfo());
                        summaryAcc = new ChartDataServer.SummaryAccumulator();
                        allExportColumns = ChartDataServer.getAllExportColumns(request.getViewInfo());
                    }

                    request.getViewInfo().getChartExtRequest().setPageSize(Long.valueOf(extractPageSize));
                    ChartViewDTO chartViewDTO = chartDataServer.findExcelData(request);
                    for (long i = 1; i < chartViewDTO.getTotalPage() + 1; i++) {
                        request.getViewInfo().getChartExtRequest().setGoPage(i);
                        request.getViewInfo().setExtStack(request.getViewInfo().getExtStack().stream().filter(ele -> !ele.isHide()).collect(Collectors.toList()));
                        ChartViewDTO pageDto = chartDataServer.findExcelData(request);
                        details.addAll(request.getDetails());

                        if (summaryEnabled) {
                            ChartDataServer.accumulatePageStats(summaryAcc, request.getDetails(), allExportColumns, summaryConfig);
                            if (i == chartViewDTO.getTotalPage() && pageDto.getData() != null && pageDto.getData().get("customSumResult") != null) {
                                customSumResult = (Map<String, java.math.BigDecimal>) pageDto.getData().get("customSumResult");
                            }
                        }

                        if (((details.size() + extractPageSize) > sheetLimit) || i == chartViewDTO.getTotalPage()) {
                            if (i == chartViewDTO.getTotalPage() && summaryEnabled && summaryAcc.totalCount > 0) {
                                Object[] totalRow = ChartDataServer.buildSummaryRow(allExportColumns, summaryConfig, summaryAcc, customSumResult);
                                details.add(totalRow);
                            }

                            detailsSheet = wb.createSheet("数据" + sheetIndex);
                            Integer[] excelTypes = request.getExcelTypes();
                            ViewDetailField[] detailFields = request.getDetailFields();
                            Object[] header = ChartDataServer.filterExportHeader(request.getHeader(), request.getViewInfo());
                            details.add(0, header);
                            List<Integer> columnIndexs = ChartDataServer.getHiddenExportColumnIndexes(header, request.getViewInfo());
                            removeColumn(details, columnIndexs);
                            ChartDataServer.setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes, request.getViewInfo(), wb);
                            sheetIndex++;
                            details.clear();
                        }
                        double exportProgress = (double) ((double) i / (chartViewDTO.getTotalPage()));
                        DecimalFormat df = new DecimalFormat("#.##");
                        String formattedResult = df.format((exportProgress) * 100);
                        updateExportTask(exportTarget.taskId(), "IN_PROGRESS", formattedResult, null, null, null);
                    }
                } else {
                    downloadNotTableInfoData(request, wb);
                }
                this.addWatermarkTools(wb);

                try (OutputStream outputStream = exportTarget.newOutputStream()) {
                    wb.write(outputStream);
                    outputStream.flush();
                }
                wb.close();
                updateExportTaskSuccess(exportTarget, "100");
            } catch (Exception e) {
                LogUtil.error("Failed to export data", e);
                updateExportTask(exportTarget.taskId(), "FAILED", null, e.getMessage(), null, null);
            }
        });
        Running_Task.put(exportTarget.taskId(), future);
    }

    private void updateExportTask(String taskId, String exportStatus, String exportProgress, String msg, Double fileSize, String fileSizeUnit) {
        CoreExportTask exportTask = coreExportTaskRepository.findById(taskId).orElse(null);
        if (exportTask == null) return;
        exportTask.setExportStatus(exportStatus);
        if (exportProgress != null) exportTask.setExportProgress(exportProgress);
        if (msg != null) exportTask.setMsg(msg);
        if (fileSize != null) exportTask.setFileSize(fileSize);
        if (fileSizeUnit != null) exportTask.setFileSizeUnit(fileSizeUnit);
        coreExportTaskRepository.saveAndFlush(exportTask);
    }

    private void downloadNotTableInfoData(ChartExcelRequest request, Workbook wb) {
        chartDataServer.findExcelData(request);
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
            if (request.getViewInfo().getType().equalsIgnoreCase("chart-mix-dual-line")) {
            } else {
                List<Object[]> details = request.getDetails();
                Integer[] excelTypes = request.getExcelTypes();
                details.add(0, request.getHeader());
                ViewDetailField[] detailFields = request.getDetailFields();
                Object[] header = request.getHeader();
                Sheet detailsSheet = wb.createSheet("数据");
                if (request.getViewInfo().getType().equalsIgnoreCase("table-normal")) {
                    ChartDataServer.setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes, request.getViewInfo(), wb);
                } else {
                    ChartDataServer.setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes, request.getViewInfo(), null);
                }
            }
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
                ChartDataServer.setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes, request.getViewInfo(), null);
            }
        }
    }

    private void updateExportTaskSuccess(ExportTaskFileTarget exportTarget, String exportProgress) {
        try {
            ExportTaskFileTarget.FileSize fileSize = exportTarget.readFileSize();
            updateExportTask(exportTarget.taskId(), "SUCCESS", exportProgress, null, fileSize.size(), fileSize.unit());
        } catch (IOException e) {
            DEException.throwException(e);
        }
    }

    public void addWatermarkTools(Workbook wb) {
        VisualizationWatermark watermark = watermarkRepository.findById("system_default").orElse(null);
        WatermarkContentDTO watermarkContent = JsonUtil.parseObject(watermark.getSettingContent(), WatermarkContentDTO.class);
        if (watermarkContent.getEnable() && watermarkContent.getExcelEnable()) {
            UserFormVO userInfo = CommonBeanFactory.getBean(UserApi.class).queryById(V3UserUtil.getUid());
            // 在主逻辑中添加水印
            int watermarkPictureIdx = ExcelWatermarkUtils.addWatermarkImage(wb, watermarkContent, userInfo); // 生成水印图片并获取 ID
            for (Sheet sheet : wb) {
                ExcelWatermarkUtils.addWatermarkToSheet(sheet, watermarkPictureIdx); // 为每个 Sheet 添加水印
            }
        }
    }

    public void download(ExportTaskFileTarget exportTarget, String downloadFileName, HttpServletResponse response) throws Exception {
        response.setContentType("application/octet-stream");
        String encodedFileName = URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=utf-8''" + encodedFileName);
        try (InputStream fileInputStream = exportTarget.newInputStream(); OutputStream outputStream = response.getOutputStream()) {
            fileInputStream.transferTo(outputStream);
            outputStream.flush();
            response.flushBuffer();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public void downloadDataset(DataSetExportRequest request, HttpServletResponse response) throws Exception {
        OutputStream outputStream = response.getOutputStream();
        try {
            CoreDatasetGroup coreDatasetGroup = coreDatasetGroupRepository.findById(request.getId()).orElse(null);
            if (coreDatasetGroup == null) {
                throw new Exception("Not found dataset group: " + request.getFilename());
            }
            DatasetGroupInfoDTO dto = new DatasetGroupInfoDTO();
            BeanUtils.copyBean(dto, coreDatasetGroup);
            dto.setUnionSql(null);
            List<UnionDTO> unionDTOList = JsonUtil.parseList(coreDatasetGroup.getInfo(), new TypeReference<>() {
            });
            dto.setUnion(unionDTOList);
            List<DatasetTableFieldDTO> dsFields = datasetTableFieldManage.selectByDatasetGroupId(request.getId());
            List<DatasetTableFieldDTO> allFields = dsFields.stream().map(ele -> {
                DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
                BeanUtils.copyBean(datasetTableFieldDTO, ele);
                datasetTableFieldDTO.setFieldShortName(ele.getDataeaseName());
                return datasetTableFieldDTO;
            }).collect(Collectors.toList());
            if (ObjectUtils.isEmpty(allFields)) {
                DEException.throwException(Translator.get("i18n_no_fields"));
            }
            dto.setAllFields(allFields);

            Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(dto, null);
            String sql = (String) sqlMap.get("sql");
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
            boolean crossDs = dto.getIsCross();
            if (!crossDs) {
                if (datasetDataManage.notFullDs.contains(dsMap.entrySet().iterator().next().getValue().getType()) && (boolean) sqlMap.get("isFullJoin")) {
                    DEException.throwException(Translator.get("i18n_not_full"));
                }
                sql = Utils.replaceSchemaAlias(sql, dsMap);
            }
            List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = new ArrayList<>();
            Long uid = V3UserUtil.getUid();
            if (uid != null) {
                rowPermissionsTree = permissionManage.getRowPermissionsTree(dto.getId(), uid);
            }
            if (StringUtils.isNotEmpty(request.getExpressionTree())) {
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
            Long curLimit = ExportCenterUtils.getExportLimit("dataset");
            totalCount = totalCount > curLimit ? curLimit : totalCount;

            Long sheetCount = (totalCount / sheetLimit) + (totalCount % sheetLimit > 0 ? 1 : 0);
            Workbook wb = new SXSSFWorkbook();
            CellStyle cellStyle = wb.createCellStyle();
            Font font = wb.createFont();
            font.setFontHeightInPoints((short) 12);
            font.setBold(true);
            cellStyle.setFont(font);
            cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (Long s = 1L; s < sheetCount + 1; s++) {
                Long sheetSize;
                if (s.equals(sheetCount)) {
                    sheetSize = totalCount - (s - 1) * sheetLimit;
                } else {
                    sheetSize = sheetLimit;
                }
                Long pageSize = (sheetSize / extractPageSize) + (sheetSize % extractPageSize > 0 ? 1 : 0);
                Sheet detailsSheet = null;
                List<List<String>> details = new ArrayList<>();
                for (Long p = 0L; p < pageSize; p++) {
                    int beforeCount = (int) ((s - 1) * sheetLimit);
                    String querySQL = SQLProvider.createQuerySQLWithLimit(sqlMeta, false, needOrder, false, beforeCount + p.intValue() * extractPageSize, extractPageSize);
                    if (pageSize == 1) {
                        querySQL = SQLProvider.createQuerySQLWithLimit(sqlMeta, false, needOrder, false, 0, sheetSize.intValue());
                    }
                    querySQL = provider.rebuildSQL(querySQL, sqlMeta, crossDs, dsMap);
                    DatasourceRequest datasourceRequest = new DatasourceRequest();
                    datasourceRequest.setQuery(querySQL);
                    datasourceRequest.setDsList(dsMap);
                    datasourceRequest.setIsCross(coreDatasetGroup.getIsCross());
                    applyPreparedParams(datasourceRequest, sqlMap);
                    Map<String, Object> previewData = datasetDataManage.buildPreviewData(provider.fetchResultField(datasourceRequest), allFields, desensitizationList, false);
                    List<Map<String, Object>> data = (List<Map<String, Object>>) previewData.get("data");
                    if (p.equals(0L)) {
                        detailsSheet = wb.createSheet("数据" + s);
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
                                            if ((allFields.get(j).getDeType().equals(DeTypeConstants.DE_INT) || allFields.get(j).getDeType() == DeTypeConstants.DE_FLOAT) && StringUtils.isNotEmpty(rowData.get(j))) {
                                                try {
                                                    cell.setCellValue(Double.valueOf(rowData.get(j)));
                                                } catch (Exception e) {
                                                    cell.setCellValue(rowData.get(j));
                                                }
                                            } else {
                                                cell.setCellValue(rowData.get(j));
                                            }
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
                                    if (i == 0) {
                                        cell.setCellValue(rowData.get(j));
                                        cell.setCellStyle(cellStyle);
                                        detailsSheet.setColumnWidth(j, 255 * 20);
                                    } else {
                                        if ((allFields.get(j).getDeType().equals(DeTypeConstants.DE_INT) || allFields.get(j).getDeType() == DeTypeConstants.DE_FLOAT) && StringUtils.isNotEmpty(rowData.get(j))) {
                                            try {
                                                cell.setCellValue(Double.valueOf(rowData.get(j)));
                                            } catch (Exception e) {
                                                cell.setCellValue(rowData.get(j));
                                            }
                                        } else {
                                            cell.setCellValue(rowData.get(j));
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
            this.addWatermarkTools(wb);
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(request.getFilename(), StandardCharsets.UTF_8) + ".xlsx");
            wb.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }

    private void validateDownloadPath(String filePath) {
        try {
            File resolved = new File(filePath).getCanonicalFile();
            File base = new File(exportData_path).getCanonicalFile();
            if (!resolved.getAbsolutePath().startsWith(base.getAbsolutePath())) {
                throw new RuntimeException("非法的下载路径");
            }
        } catch (IOException e) {
            throw new RuntimeException("路径解析失败");
        }
    }
}
