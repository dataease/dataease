package io.dataease.exportCenter.manage;


import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.chart.dto.ViewDetailField;
import io.dataease.api.chart.request.ChartExcelRequest;
import io.dataease.api.chart.request.ChartExcelRequestInner;
import io.dataease.api.dataset.dto.DataSetExportRequest;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.api.permissions.dataset.dto.DataSetRowPermissionsTreeDTO;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.api.xpack.dataFilling.DataFillingApi;
import io.dataease.api.xpack.dataFilling.dto.DataFillFormTableDataRequest;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.chart.dao.auto.mapper.CoreChartViewMapper;
import io.dataease.chart.server.ChartDataServer;
import io.dataease.commons.utils.ExcelWatermarkUtils;
import io.dataease.constant.DeTypeConstants;
import io.dataease.constant.LogOT;
import io.dataease.constant.LogST;
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
import io.dataease.exportCenter.util.ExportCenterUtils;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
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
import io.dataease.utils.*;
import io.dataease.visualization.dao.auto.entity.VisualizationWatermark;
import io.dataease.visualization.dao.auto.mapper.VisualizationWatermarkMapper;
import io.dataease.visualization.dao.ext.mapper.ExtDataVisualizationMapper;
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
    private CoreExportTaskMapper exportTaskMapper;
    @Resource
    private CoreChartViewMapper coreChartViewMapper;
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

    @Value("${dataease.path.exportData:/opt/dataease2.0/data/exportData/}")
    private String exportData_path;
    @Resource
    private VisualizationWatermarkMapper watermarkMapper;
    @Resource
    private ExtDataVisualizationMapper visualizationMapper;
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
    private DatasetSQLManage datasetSQLManage;
    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;
    @Resource
    private DatasetDataManage datasetDataManage;
    private final Long sheetLimit = 1000000L;
    @Autowired(required = false)
    private DataFillingApi dataFillingApi = null;

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

    private void setExportFromName(ExportTaskDTO exportTaskDTO) {
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("chart")) {
            exportTaskDTO.setExportFromName(coreChartViewMapper.selectById(exportTaskDTO.getExportFrom()).getTitle());
        }
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("dataset")) {
            exportTaskDTO.setExportFromName(coreDatasetGroupMapper.selectById(exportTaskDTO.getExportFrom()).getName());
        }
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("data_filling")) {
            exportTaskDTO.setExportFromName(getDataFillingApi().get(exportTaskDTO.getExportFrom()).getName());
        }
    }

    @DeLog(id = "#p0.exportFrom", ot = LogOT.EXPORT, st = LogST.DATA_FILLING)
    public void startDataFillingTask(CoreExportTask exportTask, HashMap<String, Object> request) {
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
                getDataFillingApi().writeExcel(dataPath + "/" + exportTask.getId() + ".xlsx", new DataFillFormTableDataRequest().setId(exportTask.getExportFrom()).setWithoutLogs(true), exportTask.getUserId(), Long.parseLong(request.get("org").toString()));
                exportTask.setExportProgress("100");
                exportTask.setExportStatus("SUCCESS");

                setFileSize(dataPath + "/" + exportTask.getId() + ".xlsx", exportTask);
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

    @DeLog(id = "#p0.exportFrom", ot = LogOT.EXPORT, st = LogST.DATASET)
    public void startDatasetTask(CoreExportTask exportTask, DataSetExportRequest request) {
        String dataPath = exportData_path + exportTask.getId();
        File directory = new File(dataPath);
        // 如果父目录不存在，则递归创建
        if (!directory.exists()) {
            boolean isCreated = directory.mkdirs(); // 创建所有必要的父目录
        }

        TokenUserBO tokenUserBO = AuthUtils.getUser();
        Future future = scheduledThreadPoolExecutor.submit(() -> {
            LicenseUtil.validate();
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
                boolean crossDs = dto.getIsCross();
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
                        exportTask.setExportStatus("IN_PROGRESS");
                        double exportRogress2 = (double) ((double) s - 1) / ((double) sheetCount);
                        double exportRogress = (double) ((double) (p + 1) / (double) pageSize) * ((double) 1 / sheetCount);
                        DecimalFormat df = new DecimalFormat("#.##");
                        String formattedResult = df.format((exportRogress + exportRogress2) * 100);
                        exportTask.setExportProgress(formattedResult);
                        exportTaskMapper.updateById(exportTask);
                    }
                }
                this.addWatermarkTools(wb);
                FileOutputStream fileOutputStream = new FileOutputStream(dataPath + "/" + exportTask.getId() + ".xlsx");
                wb.write(fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
                wb.close();
                exportTask.setExportProgress("100");
                exportTask.setExportStatus("SUCCESS");
                setFileSize(dataPath + "/" + exportTask.getId() + ".xlsx", exportTask);

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

    @DeLog(id = "#p0.exportFrom", ot = LogOT.EXPORT, st = LogST.PANEL)
    public void startPanelViewTask(CoreExportTask exportTask, ChartExcelRequest request) {
        startViewTask(exportTask, request);
    }

    @DeLog(id = "#p0.exportFrom", ot = LogOT.EXPORT, st = LogST.SCREEN)
    public void startDataVViewTask(CoreExportTask exportTask, ChartExcelRequest request) {
        startViewTask(exportTask, request);
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

    public void startViewTask(CoreExportTask exportTask, ChartExcelRequest request) {
        String dataPath = exportData_path + exportTask.getId();
        File directory = new File(dataPath);
        boolean isCreated = directory.mkdir();
        TokenUserBO tokenUserBO = AuthUtils.getUser();
        Future future = scheduledThreadPoolExecutor.submit(() -> {
            LicenseUtil.validate();
            AuthUtils.setUser(tokenUserBO);
            try {
                exportTask.setExportStatus("IN_PROGRESS");
                exportTaskMapper.updateById(exportTask);
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
                    request.getViewInfo().getChartExtRequest().setPageSize(Long.valueOf(extractPageSize));
                    ChartViewDTO chartViewDTO = chartDataServer.findExcelData(request);
                    for (long i = 1; i < chartViewDTO.getTotalPage() + 1; i++) {
                        request.getViewInfo().getChartExtRequest().setGoPage(i);
                        request.getViewInfo().setExtStack(request.getViewInfo().getExtStack().stream().filter(ele -> !ele.isHide()).collect(Collectors.toList()));
                        chartDataServer.findExcelData(request);
                        details.addAll(request.getDetails());
                        if (((details.size() + extractPageSize) > sheetLimit) || i == chartViewDTO.getTotalPage()) {
                            detailsSheet = wb.createSheet("数据" + sheetIndex);
                            Integer[] excelTypes = request.getExcelTypes();
                            ViewDetailField[] detailFields = request.getDetailFields();
                            Object[] header = request.getHeader();
                            List<ChartViewFieldDTO> xAxis = new ArrayList<>();
                            xAxis.addAll(request.getViewInfo().getXAxis());
                            xAxis.addAll(request.getViewInfo().getYAxis());
                            xAxis.addAll(request.getViewInfo().getXAxisExt());
                            xAxis.addAll(request.getViewInfo().getYAxisExt());
                            xAxis.addAll(request.getViewInfo().getExtStack());
                            xAxis.addAll(request.getViewInfo().getDrillFields());
                            header = Arrays.stream(request.getHeader()).filter(item -> xAxis.stream().map(d -> StringUtils.isNotBlank(d.getChartShowName()) ? d.getChartShowName() : d.getName()).toList().contains(item)).toArray();
                            details.add(0, header);
                            List<Integer> columnIndexs = new ArrayList<>();
                            for (int i1 = 0; i1 < xAxis.size(); i1++) {
                                ChartViewFieldDTO xAxi = xAxis.get(i1);
                                if (xAxi.isHide()) {
                                    columnIndexs.add(i1);
                                }
                            }
                            removeColumn(details, columnIndexs);
                            ChartDataServer.setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes, request.getViewInfo(), wb);
                            sheetIndex++;
                            details.clear();
                            exportTask.setExportStatus("IN_PROGRESS");
                            double exportProgress = (double) (i / (chartViewDTO.getTotalPage() + 1));
                            DecimalFormat df = new DecimalFormat("#.##");
                            String formattedResult = df.format((exportProgress) * 100);
                            exportTask.setExportProgress(formattedResult);
                            exportTaskMapper.updateById(exportTask);
                        }
                    }
                } else {
                    downloadNotTableInfoData(request, wb);
                }
                this.addWatermarkTools(wb);

                try (FileOutputStream outputStream = new FileOutputStream(dataPath + "/" + exportTask.getId() + ".xlsx")) {
                    wb.write(outputStream);
                    outputStream.flush();
                }
                wb.close();
                exportTask.setExportProgress("100");
                exportTask.setExportStatus("SUCCESS");
                setFileSize(dataPath + "/" + exportTask.getId() + ".xlsx", exportTask);
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

    public void addWatermarkTools(Workbook wb) {
        VisualizationWatermark watermark = watermarkMapper.selectById("system_default");
        WatermarkContentDTO watermarkContent = JsonUtil.parseObject(watermark.getSettingContent(), WatermarkContentDTO.class);
        if (watermarkContent.getEnable() && watermarkContent.getExcelEnable()) {
            UserFormVO userInfo = visualizationMapper.queryInnerUserInfo(AuthUtils.getUser().getUserId());
            // 在主逻辑中添加水印
            int watermarkPictureIdx = ExcelWatermarkUtils.addWatermarkImage(wb, watermarkContent, userInfo); // 生成水印图片并获取 ID
            for (Sheet sheet : wb) {
                ExcelWatermarkUtils.addWatermarkToSheet(sheet, watermarkPictureIdx); // 为每个 Sheet 添加水印
            }
        }
    }

    public void download(CoreExportTask exportTask, HttpServletResponse response) throws Exception {
        response.setContentType("application/octet-stream");
        String encodedFileName = URLEncoder.encode(exportTask.getFileName(), StandardCharsets.UTF_8);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=utf-8''" + encodedFileName);
        String filePath;

        if (exportTask.getExportTime() < 1730277243491L) {
            filePath = exportData_path + exportTask.getId() + "/" + exportTask.getFileName();
        } else {
            filePath = exportData_path + exportTask.getId() + "/" + exportTask.getId() + ".xlsx";
        }

        try (FileInputStream fileInputStream = new FileInputStream(filePath); OutputStream outputStream = response.getOutputStream()) {
            FileChannel fileChannel = fileInputStream.getChannel();
            WritableByteChannel outputChannel = Channels.newChannel(outputStream);
            fileChannel.transferTo(0, fileChannel.size(), outputChannel);
            response.flushBuffer();
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public void downloadDataset(DataSetExportRequest request, HttpServletResponse response) throws Exception {
        OutputStream outputStream = response.getOutputStream();
        try {
            CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(request.getId());
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
            boolean crossDs = dto.getIsCross();
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
}

