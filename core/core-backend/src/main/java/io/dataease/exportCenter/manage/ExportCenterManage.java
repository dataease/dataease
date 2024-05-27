package io.dataease.exportCenter.manage;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.chart.dto.ViewDetailField;
import io.dataease.api.chart.request.ChartExcelRequest;
import io.dataease.api.exportCenter.vo.ExportTaskDTO;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.chart.dao.auto.mapper.CoreChartViewMapper;
import io.dataease.chart.server.ChartDataServer;
import io.dataease.engine.constant.DeTypeConstants;
import io.dataease.exception.DEException;
import io.dataease.exportCenter.dao.auto.entity.CoreExportTask;
import io.dataease.exportCenter.dao.auto.mapper.CoreExportTaskMapper;
import io.dataease.utils.*;
import io.dataease.visualization.server.DataVisualizationServer;
import io.dataease.websocket.entity.WsMessage;
import io.dataease.websocket.service.WsService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Transactional(rollbackFor = Exception.class)
public class ExportCenterManage {
    @Resource
    private CoreExportTaskMapper exportTaskMapper;
    @Resource
    DataVisualizationServer dataVisualizationServer;
    @Resource
    private CoreChartViewMapper coreChartViewMapper;
    @Autowired
    private WsService wsService;

    @Value("${export.dataset.limit:100000}")
    private int limit;
    private final static String DATA_URL_TITLE = "data:image/jpeg;base64,";
    private static final String exportData_path = "/opt/dataease2.0/data/exportData/";
    @Value("${extract.page.size:50000}")
    private Integer extractPageSize;
    static private List<String> STATUS = Arrays.asList("SUCCESS", "FAILED", "PENDING", "IN_PROGRESS", "ALL");
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private int corePoolSize = 10;
    private int keepAliveSeconds = 600;
    private Map<String, Future> Running_Task = new HashMap<>();
    @Resource
    private ChartDataServer chartDataServer;

    @PostConstruct
    public void init() {
        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(corePoolSize);
        scheduledThreadPoolExecutor.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
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
        exportTask.setExportStatus("PENDING");
        exportTask.setExportProgress("0");
        exportTask.setExportMachineName(hostName());
        exportTask.setExportTime(System.currentTimeMillis());
        exportTaskMapper.updateById(exportTask);
        FileUtils.deleteDirectoryRecursively(exportData_path + id);
        if (exportTask.getExportFromType().equalsIgnoreCase("chart")) {
            ChartExcelRequest request = JsonUtil.parse(exportTask.getParams(), ChartExcelRequest.class);
            startViewTask(exportTask, request);
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
            if (status.equalsIgnoreCase("ALL")) {
                setExportFromAbsName(exportTaskDTO);
            }
            if (status.equalsIgnoreCase(exportTaskDTO.getExportStatus())) {
                setExportFromAbsName(exportTaskDTO);
            }
            result.add(exportTaskDTO);
        });

        return result;
    }

    private void setExportFromAbsName(ExportTaskDTO exportTaskDTO) {
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("chart")) {
            exportTaskDTO.setExportFromName(dataVisualizationServer.getAbsPath(exportTaskDTO.getExportFrom()));
        }
    }

    private void setExportFromName(ExportTaskDTO exportTaskDTO) {
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("chart")) {
            exportTaskDTO.setExportFromName(coreChartViewMapper.selectById(exportTaskDTO.getExportFrom()).getTitle());
        }
    }


//    public void exportTableDetails(PanelViewDetailsRequest request, Workbook wb, CellStyle cellStyle, Sheet detailsSheet) throws IOException {
//        List<Object[]> details = request.getDetails();
//        Integer[] excelTypes = request.getExcelTypes();
//        if (CollectionUtils.isNotEmpty(details)) {
//            for (int i = 0; i < details.size(); i++) {
//                Row row = detailsSheet.createRow(i);
//                Object[] rowData = details.get(i);
//                if (rowData != null) {
//                    for (int j = 0; j < rowData.length; j++) {
//                        Cell cell = row.createCell(j);
//                        if (i == 0) {// 头部
//                            cell.setCellValue(String.valueOf(rowData[j]));
//                            cell.setCellStyle(cellStyle);
//                            //设置列的宽度
//                            detailsSheet.setColumnWidth(j, 255 * 20);
//                        } else {
//                            try {
//                                // with DataType
//                                if ((excelTypes[j].equals(DeTypeConstants.DE_INT) || excelTypes[j].equals(DeTypeConstants.DE_FLOAT)) && rowData[j] != null) {
//                                    cell.setCellValue(Double.valueOf(rowData[j].toString()));
//                                } else if (rowData[j] != null) {
//                                    cell.setCellValue(String.valueOf(rowData[j]));
//                                }
//                            } catch (Exception e) {
//                                LogUtil.warn("export excel data transform error");
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }

//    public void reInitExportTask() {
//        ExportTaskExample exportTaskExample = new ExportTaskExample();
//        ExportTaskExample.Criteria criteria = exportTaskExample.createCriteria();
//        criteria.andExportMachineNameEqualTo(hostName()).andExportStatusEqualTo("IN_PROGRESS");
//        ExportTask record = new ExportTask();
//        record.setExportStatus("FAILED");
//        exportTaskMapper.updateByExampleSelective(record, exportTaskExample);
//        exportTaskExample.clear();
//        criteria = exportTaskExample.createCriteria();
//        criteria.andExportMachineNameEqualTo(hostName()).andExportStatusEqualTo("PENDING");
//        exportTaskMapper.selectByExampleWithBLOBs(exportTaskExample).parallelStream().forEach(exportTask -> {
//            if (exportTask.getExportFromType().equalsIgnoreCase("dataset")) {
//                DataSetExportRequest request = new Gson().fromJson(exportTask.getParams(), DataSetExportRequest.class);
//                startDatasetTask(exportTask, request);
//            }
//            if (exportTask.getExportFromType().equalsIgnoreCase("chart")) {
//                PanelViewDetailsRequest request = new Gson().fromJson(exportTask.getParams(), PanelViewDetailsRequest.class);
//                startViewTask(exportTask, request);
//            }
//        });
//    }

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
                List<Object[]> details = request.getDetails();
                Integer[] excelTypes = request.getExcelTypes();
                details.add(0, request.getHeader());
                Workbook wb = new SXSSFWorkbook();
                //明细sheet
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


                Boolean mergeHead = false;
                ViewDetailField[] detailFields = request.getDetailFields();
                if (ArrayUtils.isNotEmpty(detailFields)) {
                    cellStyle.setBorderTop(BorderStyle.THIN);
                    cellStyle.setBorderRight(BorderStyle.THIN);
                    cellStyle.setBorderBottom(BorderStyle.THIN);
                    cellStyle.setBorderLeft(BorderStyle.THIN);
                    String[] detailField = Arrays.stream(detailFields).map(field -> field.getName()).collect(Collectors.toList()).toArray(new String[detailFields.length]);
                    Object[] header = request.getHeader();
                    Row row = detailsSheet.createRow(0);
                    int headLen = header.length;
                    int detailFieldLen = detailField.length;
                    for (int i = 0; i < headLen; i++) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue(header[i].toString());
                        if (i < headLen - 1) {
                            CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 1, i, i);
                            detailsSheet.addMergedRegion(cellRangeAddress);
                        } else {
                            for (int j = i + 1; j < detailFieldLen + i; j++) {
                                row.createCell(j).setCellStyle(cellStyle);
                            }
                            CellRangeAddress cellRangeAddress = new CellRangeAddress(0, 0, i, i + detailFieldLen - 1);
                            detailsSheet.addMergedRegion(cellRangeAddress);
                        }
                        cell.setCellStyle(cellStyle);
                        detailsSheet.setColumnWidth(i, 255 * 20);
                    }

                    Row detailRow = detailsSheet.createRow(1);
                    for (int i = 0; i < headLen - 1; i++) {
                        Cell cell = detailRow.createCell(i);
                        cell.setCellStyle(cellStyle);
                    }
                    for (int i = 0; i < detailFieldLen; i++) {
                        int colIndex = headLen - 1 + i;
                        Cell cell = detailRow.createCell(colIndex);
                        cell.setCellValue(detailField[i]);
                        cell.setCellStyle(cellStyle);
                        detailsSheet.setColumnWidth(colIndex, 255 * 20);
                    }
                    details.add(1, detailField);
                    mergeHead = true;
                }
                if (CollectionUtils.isNotEmpty(details) && (!mergeHead || details.size() > 2)) {
                    int realDetailRowIndex = 2;
                    for (int i = (mergeHead ? 2 : 0); i < details.size(); i++) {
                        Row row = detailsSheet.createRow(realDetailRowIndex > 2 ? realDetailRowIndex : i);
                        Object[] rowData = details.get(i);
                        if (rowData != null) {
                            for (int j = 0; j < rowData.length; j++) {
                                Object cellValObj = rowData[j];
                                if (mergeHead && j == rowData.length - 1 && (cellValObj.getClass().isArray() || cellValObj instanceof ArrayList)) {
                                    Object[] detailRowArray = ((List<Object>) cellValObj).toArray(new Object[((List<?>) cellValObj).size()]);
                                    int detailRowArrayLen = detailRowArray.length;
                                    int temlJ = j;
                                    while (detailRowArrayLen > 1 && temlJ-- > 0) {
                                        CellRangeAddress cellRangeAddress = new CellRangeAddress(realDetailRowIndex, realDetailRowIndex + detailRowArrayLen - 1, temlJ, temlJ);
                                        detailsSheet.addMergedRegion(cellRangeAddress);
                                    }

                                    for (int k = 0; k < detailRowArrayLen; k++) {
                                        List<Object> detailRows = (List<Object>) detailRowArray[k];
                                        Row curRow = row;
                                        if (k > 0) {
                                            curRow = detailsSheet.createRow(realDetailRowIndex + k);
                                        }

                                        for (int l = 0; l < detailRows.size(); l++) {
                                            Object col = detailRows.get(l);
                                            Cell cell = curRow.createCell(j + l);
                                            cell.setCellValue(col.toString());
                                        }
                                    }
                                    realDetailRowIndex += detailRowArrayLen;
                                    break;
                                }

                                Cell cell = row.createCell(j);
                                if (i == 0) {// 头部
                                    cell.setCellValue(cellValObj.toString());
                                    cell.setCellStyle(cellStyle);
                                    //设置列的宽度
                                    detailsSheet.setColumnWidth(j, 255 * 20);
                                } else if (cellValObj != null) {
                                    try {
                                        // with DataType
                                        if ((excelTypes[j].equals(DeTypeConstants.DE_INT) || excelTypes[j].equals(DeTypeConstants.DE_FLOAT)) && StringUtils.isNotEmpty(cellValObj.toString())) {
                                            cell.setCellValue(Double.valueOf(cellValObj.toString()));
                                        } else {
                                            cell.setCellValue(cellValObj.toString());
                                        }
                                    } catch (Exception e) {
                                        LogUtil.warn("export excel data transform error");
                                    }
                                }


                            }
                        }
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
                e.printStackTrace();
                LogUtil.error("Failed to export data", e);
                exportTask.setExportStatus("FAILED");
            } finally {
                exportTaskMapper.updateById(exportTask);
            }
        });
        Running_Task.put(exportTask.getId(), future);
    }

//    public void addTask(String exportFrom, String exportFromType, DataSetExportRequest request) {
//        ExportTask exportTask = new ExportTask();
//        exportTask.setId(UUID.randomUUID().toString());
//        exportTask.setUserId(AuthUtils.getUser().getUserId());
//        exportTask.setExportFrom(exportFrom);
//        exportTask.setExportFromType(exportFromType);
//        exportTask.setExportStatus("PENDING");
//        exportTask.setFileName(request.getFilename() + ".xlsx");
//        exportTask.setExportPogress("0");
//        exportTask.setExportTime(System.currentTimeMillis());
//        exportTask.setParams(new Gson().toJson(request));
//        exportTask.setExportMachineName(hostName());
//        exportTaskMapper.insert(exportTask);
//        startDatasetTask(exportTask, request);
//
//    }

//    private void startDatasetTask(ExportTask exportTask, DataSetExportRequest request) {
//        String dataPath = exportData_path + exportTask.getId();
//        File directory = new File(dataPath);
//        boolean isCreated = directory.mkdir();
//        Future future = scheduledThreadPoolExecutor.submit(() -> {
//            try {
//                exportTask.setExportStatus("IN_PROGRESS");
//                exportTaskMapper.updateByPrimaryKey(exportTask);
//                DatasetRowPermissionsTreeObj tree = null;
//                if (StringUtils.isNotEmpty(request.getExpressionTree())) {
//                    Gson gson = new Gson();
//                    tree = gson.fromJson(request.getExpressionTree(), DatasetRowPermissionsTreeObj.class);
//                    permissionsTreeService.getField(tree);
//                }
//                Datasource datasource = datasourceService.get(request.getDataSourceId());
//                Integer totalCount = getTotal(request, limit, tree);
//                if (totalCount == null) {
//                    Workbook wb = new SXSSFWorkbook();
//                    // Sheet
//                    Sheet detailsSheet = wb.createSheet("数据");
//                    //给单元格设置样式
//                    CellStyle cellStyle = wb.createCellStyle();
//                    Font font = wb.createFont();
//                    //设置字体大小
//                    font.setFontHeightInPoints((short) 12);
//                    //设置字体加粗
//                    font.setBold(true);
//                    //给字体设置样式
//                    cellStyle.setFont(font);
//                    //设置单元格背景颜色
//                    cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
//                    //设置单元格填充样式(使用纯色背景颜色填充)
//                    cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//                    int pageSize = (datasource != null && StringUtils.equalsIgnoreCase(datasource.getType(), "es")) ? 10000 : limit;
//                    request.setRow(String.valueOf(pageSize));
//                    Map<String, Object> previewData = dataSetTableService.getPreviewData(request, 1, pageSize, null, tree);
//                    List<DatasetTableField> fields = (List<DatasetTableField>) previewData.get("fields");
//                    List<Map<String, Object>> data = (List<Map<String, Object>>) previewData.get("data");
//                    List<String> header = new ArrayList<>();
//                    for (DatasetTableField field : fields) {
//                        header.add(field.getName());
//                    }
//                    List<List<String>> details = new ArrayList<>();
//                    details.add(header);
//                    for (Map<String, Object> obj : data) {
//                        List<String> row = new ArrayList<>();
//                        for (DatasetTableField field : fields) {
//                            String string = (String) obj.get(field.getDataeaseName());
//                            row.add(string);
//                        }
//                        details.add(row);
//                    }
//                    if (CollectionUtils.isNotEmpty(details)) {
//                        for (int i = 0; i < details.size(); i++) {
//                            Row row = detailsSheet.createRow(i);
//                            List<String> rowData = details.get(i);
//                            if (rowData != null) {
//                                for (int j = 0; j < rowData.size(); j++) {
//                                    Cell cell = row.createCell(j);
//                                    if (i == 0) {// 头部
//                                        cell.setCellValue(rowData.get(j));
//                                        cell.setCellStyle(cellStyle);
//                                        //设置列的宽度
//                                        detailsSheet.setColumnWidth(j, 255 * 20);
//                                    } else {
//                                        if ((fields.get(j).getDeType().equals(DeTypeConstants.DE_INT) || fields.get(j).getDeType() == DeTypeConstants.DE_FLOAT) && StringUtils.isNotEmpty(rowData.get(j))) {
//                                            try {
//                                                cell.setCellValue(Double.valueOf(rowData.get(j)));
//                                            } catch (Exception e) {
//                                                LogUtil.warn("export excel data transform error");
//                                            }
//                                        } else {
//                                            cell.setCellValue(rowData.get(j));
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    try (FileOutputStream outputStream = new FileOutputStream(dataPath + "/" + request.getFilename() + ".xlsx")) {
//                        wb.write(outputStream);
//                    }
//                    wb.close();
//                } else {
//                    Integer totalPage = (totalCount / extractPageSize) + (totalCount % extractPageSize > 0 ? 1 : 0);
//                    List<DatasetTableField> fields = new ArrayList<>();
//                    Workbook wb = new SXSSFWorkbook();
//                    FileOutputStream fileOutputStream = new FileOutputStream(dataPath + "/" + request.getFilename() + ".xlsx");
//                    Sheet detailsSheet = wb.createSheet("数据");
//                    for (Integer p = 1; p < totalPage + 1; p++) {
//                        Map<String, Object> previewData = getPreviewData(request, p, extractPageSize, extractPageSize, null, tree);
//                        List<Map<String, Object>> data = (List<Map<String, Object>>) previewData.get("data");
//                        if (p == 1L) {
//                            CellStyle cellStyle = wb.createCellStyle();
//                            Font font = wb.createFont();
//                            font.setFontHeightInPoints((short) 12);
//                            font.setBold(true);
//                            cellStyle.setFont(font);
//                            cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
//                            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//                            fields = (List<DatasetTableField>) previewData.get("fields");
//                            List<String> header = new ArrayList<>();
//                            for (DatasetTableField field : fields) {
//                                header.add(field.getName());
//                            }
//                            List<List<String>> details = new ArrayList<>();
//                            details.add(header);
//                            for (Map<String, Object> obj : data) {
//                                List<String> row = new ArrayList<>();
//                                for (DatasetTableField field : fields) {
//                                    String string = (String) obj.get(field.getDataeaseName());
//                                    row.add(string);
//                                }
//                                details.add(row);
//                            }
//                            if (CollectionUtils.isNotEmpty(details)) {
//                                for (int i = 0; i < details.size(); i++) {
//                                    Row row = detailsSheet.createRow(i);
//                                    List<String> rowData = details.get(i);
//                                    if (rowData != null) {
//                                        for (int j = 0; j < rowData.size(); j++) {
//                                            Cell cell = row.createCell(j);
//                                            if (i == 0) {
//                                                cell.setCellValue(rowData.get(j));
//                                                cell.setCellStyle(cellStyle);
//                                                detailsSheet.setColumnWidth(j, 255 * 20);
//                                            } else {
//                                                if ((fields.get(j).getDeType().equals(DeTypeConstants.DE_INT) || fields.get(j).getDeType() == DeTypeConstants.DE_FLOAT) && StringUtils.isNotEmpty(rowData.get(j))) {
//                                                    try {
//                                                        cell.setCellValue(Double.valueOf(rowData.get(j)));
//                                                    } catch (Exception e) {
//                                                        LogUtil.warn("export excel data transform error");
//                                                    }
//                                                } else {
//                                                    cell.setCellValue(rowData.get(j));
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        } else {
//                            List<List<String>> details = new ArrayList<>();
//                            for (Map<String, Object> obj : data) {
//                                List<String> row = new ArrayList<>();
//                                for (DatasetTableField field : fields) {
//                                    String string = (String) obj.get(field.getDataeaseName());
//                                    row.add(string);
//                                }
//                                details.add(row);
//                            }
//                            int lastNum = detailsSheet.getLastRowNum();
//                            for (int i = 0; i < details.size(); i++) {
//                                Row row = detailsSheet.createRow(i + lastNum + 1);
//                                List<String> rowData = details.get(i);
//                                if (rowData != null) {
//                                    for (int j = 0; j < rowData.size(); j++) {
//                                        Cell cell = row.createCell(j);
//                                        if ((fields.get(j).getDeType().equals(DeTypeConstants.DE_INT) || fields.get(j).getDeType() == DeTypeConstants.DE_FLOAT) && StringUtils.isNotEmpty(rowData.get(j))) {
//                                            try {
//                                                cell.setCellValue(Double.valueOf(rowData.get(j)));
//                                            } catch (Exception e) {
//                                                LogUtil.warn("export excel data transform error");
//                                            }
//                                        } else {
//                                            cell.setCellValue(rowData.get(j));
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                        exportTask.setExportStatus("IN_PROGRESS");
//                        double exportRogress = (double) ((double) p / (double) totalPage);
//                        DecimalFormat df = new DecimalFormat("#.##");
//                        String formattedResult = df.format(exportRogress * 100);
//                        exportTask.setExportPogress(formattedResult);
//                        exportTaskMapper.updateByPrimaryKey(exportTask);
//                    }
//                    wb.write(fileOutputStream);
//                    fileOutputStream.flush();
//                    fileOutputStream.close();
//                    wb.close();
//                }
//                exportTask.setExportPogress("100");
//                exportTask.setExportStatus("SUCCESS");
//                setFileSize(dataPath + "/" + request.getFilename() + ".xlsx", exportTask);
//            } catch (Exception e) {
//                LogUtil.error("Failed to export data", e);
//                exportTask.setExportStatus("FAILED");
//            } finally {
//                exportTaskMapper.updateByPrimaryKey(exportTask);
//            }
//        });
//        Running_Task.put(exportTask.getId(), future);
//    }

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

    public Boolean checkEngineTableIsExists(String id) throws Exception {
//        Datasource engine = engineService.getDeEngine();
//        JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
//        DatasourceRequest datasourceRequest = new DatasourceRequest();
//        datasourceRequest.setDatasource(engine);
//        QueryProvider qp = ProviderFactory.getQueryProvider(engine.getType());
//        datasourceRequest.setQuery(qp.searchTable(TableUtils.tableName(id)));
//        List<String[]> data = jdbcProvider.getData(datasourceRequest);
//        return CollectionUtils.isNotEmpty(data);
        return true;
    }

//    private Integer getTotal(DataSetTableRequest dataSetTableRequest, Integer limit, DatasetRowPermissionsTreeObj extTree) throws Exception {
//        DatasetTableField datasetTableField = DatasetTableField.builder().tableId(dataSetTableRequest.getId()).checked(Boolean.TRUE).build();
//        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
//        if (CollectionUtils.isEmpty(fields)) {
//            return null;
//        }
//        DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(dataSetTableRequest.getId());
//        // 行权限
//        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = permissionsTreeService.getRowPermissionsTree(fields, datasetTable, null);
//        // ext filter
//        if (extTree != null) {
//            DataSetRowPermissionsTreeDTO dto = new DataSetRowPermissionsTreeDTO();
//            dto.setTree(extTree);
//            rowPermissionsTree.add(dto);
//        }
//        // 列权限
//        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
//        fields = permissionService.filterColumnPermissions(fields, desensitizationList, datasetTable.getId(), null);
//        if (CollectionUtils.isEmpty(fields)) {
//            return null;
//        }
//        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
//        if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.DB.name()) || StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.API.name())) {
//            if (datasetTable.getMode() == 0) {
//                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
//                if (ObjectUtils.isEmpty(ds)) {
//                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
//                }
//                if (StringUtils.isNotEmpty(ds.getStatus()) && ds.getStatus().equalsIgnoreCase("Error")) {
//                    throw new Exception(Translator.get("i18n_invalid_ds"));
//                }
//                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//                String table = dataTableInfoDTO.getTable();
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//                try {
//                    String totalSql = qp.getTotalCount(false, qp.createQueryTableWithLimit(table, fields, limit, false, ds, null, rowPermissionsTree), ds);
//                    if (totalSql == null) {
//                        return null;
//                    }
//                    datasourceRequest.setQuery(totalSql);
//                    List<String[]> tmpData = datasourceProvider.getData(datasourceRequest);
//                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
//                } catch (Exception e) {
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//            } else {
//                // check doris table
//                if (!checkEngineTableIsExists(dataSetTableRequest.getId())) {
//                    if (dataSetTableRequest.isPreviewForTask()) {
//                        return null;
//                    } else {
//                        throw new RuntimeException(Translator.get("i18n_data_not_sync"));
//                    }
//                }
//                Datasource ds = engineService.getDeEngine();
//                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//                String table = TableUtils.tableName(dataSetTableRequest.getId());
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//
//                try {
//                    String totalSql = qp.getTotalCount(false, qp.createQueryTableWithLimit(table, fields, limit, false, ds, null, rowPermissionsTree), ds);
//                    if (totalSql == null) {
//                        return null;
//                    }
//                    datasourceRequest.setQuery(totalSql);
//                    List<String[]> tmpData = jdbcProvider.getData(datasourceRequest);
//                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
//                } catch (Exception e) {
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//            }
//
//        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.SQL.name())) {
//            if (datasetTable.getMode() == 0) {
//                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
//                if (ObjectUtils.isEmpty(ds)) {
//                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
//                }
//                if (StringUtils.isNotEmpty(ds.getStatus()) && ds.getStatus().equalsIgnoreCase("Error")) {
//                    throw new Exception(Translator.get("i18n_invalid_ds"));
//                }
//                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//                DataTableInfoDTO dataTableInfo = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
//                String sql = dataTableInfo.isBase64Encryption() ? new String(Base64.getDecoder().decode(dataTableInfo.getSql())) : dataTableInfo.getSql();
//                sql = dataSetTableService.handleVariableDefaultValue(sql, datasetTable.getSqlVariableDetails(), ds.getType(), false);
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//                try {
//                    String totalSql = qp.getTotalCount(false, qp.createQuerySqlWithLimit(sql, fields, limit, false, null, rowPermissionsTree), ds);
//                    if (totalSql == null) {
//                        return null;
//                    }
//                    datasourceRequest.setQuery(totalSql);
//                    List<String[]> tmpData = datasourceProvider.getData(datasourceRequest);
//                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
//                } catch (Exception e) {
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//            } else {
//                // check doris table
//                if (!checkEngineTableIsExists(dataSetTableRequest.getId())) {
//                    throw new RuntimeException(Translator.get("i18n_data_not_sync"));
//                }
//                Datasource ds = engineService.getDeEngine();
//                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//                String table = TableUtils.tableName(dataSetTableRequest.getId());
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//                try {
//                    String totalSql = qp.getTotalCount(false, qp.createQueryTableWithLimit(table, fields, limit, false, ds, null, rowPermissionsTree), ds);
//                    if (totalSql == null) {
//                        return null;
//                    }
//                    datasourceRequest.setQuery(totalSql);
//                    List<String[]> tmpData = jdbcProvider.getData(datasourceRequest);
//                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
//                } catch (Exception e) {
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//            }
//        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "excel")) {
//            if (!checkEngineTableIsExists(dataSetTableRequest.getId())) {
//                throw new RuntimeException(Translator.get("i18n_data_not_sync"));
//            }
//
//            Datasource ds = engineService.getDeEngine();
//            JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
//            DatasourceRequest datasourceRequest = new DatasourceRequest();
//            datasourceRequest.setDatasource(ds);
//            String table = TableUtils.tableName(dataSetTableRequest.getId());
//            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//            try {
//                String totalSql = qp.getTotalCount(false, qp.createQueryTableWithLimit(table, fields, limit, false, ds, null, rowPermissionsTree), ds);
//                if (totalSql == null) {
//                    return null;
//                }
//                datasourceRequest.setQuery(totalSql);
//                List<String[]> tmpData = jdbcProvider.getData(datasourceRequest);
//                return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
//            } catch (Exception e) {
//                DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//            }
//
//        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "custom")) {
//            if (datasetTable.getMode() == 0) {
//                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
//                if (ObjectUtils.isEmpty(ds)) {
//                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
//                }
//                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//
//                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
//                List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dt.getList().get(0).getTableId());
//
//                String sql = "";
//                try {
//                    sql = dataSetTableService.getCustomSQLDatasource(dt, list, ds);
//                } catch (Exception e) {
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//                try {
//                    String totalSql = qp.getTotalCount(false, qp.createQuerySQLAsTmp(sql, fields, false, null, rowPermissionsTree), ds);
//                    if (totalSql == null) {
//                        return null;
//                    }
//                    datasourceRequest.setQuery(totalSql);
//                    List<String[]> tmpData = datasourceProvider.getData(datasourceRequest);
//                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
//                } catch (Exception e) {
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//
//            } else {
//                Datasource ds = engineService.getDeEngine();
//                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//                String table = TableUtils.tableName(dataSetTableRequest.getId());
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//                try {
//                    String totalSql = qp.getTotalCount(true, qp.createQuerySQL(table, fields, false, ds, null, rowPermissionsTree), ds);
//                    if (totalSql == null) {
//                        return null;
//                    }
//                    datasourceRequest.setQuery(totalSql);
//                    List<String[]> tmpData = jdbcProvider.getData(datasourceRequest);
//                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
//                } catch (Exception e) {
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//            }
//        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "union")) {
//            if (datasetTable.getMode() == 0) {
//                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
//                if (ObjectUtils.isEmpty(ds)) {
//                    DataEaseException.throwException(Translator.get("i18n_datasource_delete"));
//                }
//                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//
//                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
//
//                String sql = "";
//                try {
//                    sql = (String) dataSetTableService.getUnionSQLDatasource(dt, ds).get("sql");
//                } catch (Exception e) {
//
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//                try {
//                    String totalSql = qp.getTotalCount(false, qp.createQuerySqlWithLimit(sql, fields, limit, false, null, rowPermissionsTree), ds);
//                    if (totalSql == null) {
//                        return null;
//                    }
//                    datasourceRequest.setQuery(totalSql);
//                    List<String[]> tmpData = datasourceProvider.getData(datasourceRequest);
//                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
//                } catch (Exception e) {
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//            } else {
//                Datasource ds = engineService.getDeEngine();
//                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//                String table = TableUtils.tableName(dataSetTableRequest.getId());
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//                try {
//                    String totalSql = qp.getTotalCount(false, qp.createQueryTableWithLimit(table, fields, limit, false, ds, null, rowPermissionsTree), ds);
//                    if (totalSql == null) {
//                        return null;
//                    }
//                    datasourceRequest.setQuery(totalSql);
//                    List<String[]> tmpData = jdbcProvider.getData(datasourceRequest);
//                    return CollectionUtils.isEmpty(tmpData) ? 0 : Integer.valueOf(tmpData.get(0)[0]);
//                } catch (Exception e) {
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//            }
//        }
//        return null;
//    }


//    public Map<String, Object> getPreviewData(DataSetTableRequest dataSetTableRequest, Integer page, Integer pageSize, Integer realSize, List<DatasetTableField> extFields, DatasetRowPermissionsTreeObj extTree) throws Exception {
//        Map<String, Object> map = new HashMap<>();
//        String syncStatus = "";
//        DatasetTableField datasetTableField = DatasetTableField.builder().tableId(dataSetTableRequest.getId()).checked(Boolean.TRUE).build();
//        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
//        if (CollectionUtils.isNotEmpty(extFields)) {
//            fields = extFields;
//        }
//        if (CollectionUtils.isEmpty(fields)) {
//            map.put("fields", fields);
//            map.put("data", new ArrayList<>());
//            return map;
//        }
//        DatasetTable datasetTable = datasetTableMapper.selectByPrimaryKey(dataSetTableRequest.getId());
//        // 行权限
//        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = permissionsTreeService.getRowPermissionsTree(fields, datasetTable, null);
//        // ext filter
//        if (extTree != null) {
//            DataSetRowPermissionsTreeDTO dto = new DataSetRowPermissionsTreeDTO();
//            dto.setTree(extTree);
//            rowPermissionsTree.add(dto);
//        }
//        // 列权限
//        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
//        fields = permissionService.filterColumnPermissions(fields, desensitizationList, datasetTable.getId(), null);
//        if (CollectionUtils.isEmpty(fields)) {
//            map.put("fields", fields);
//            map.put("data", new ArrayList<>());
//            return map;
//        }
//
//        String[] fieldArray = fields.stream().map(DatasetTableField::getDataeaseName).toArray(String[]::new);
//
//        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(dataSetTableRequest.getInfo(), DataTableInfoDTO.class);
//
//        List<String[]> data = new ArrayList<>();
//        if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.DB.name()) || StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.API.name())) {
//            if (datasetTable.getMode() == 0) {
//                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
//                if (ObjectUtils.isEmpty(ds)) {
//                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
//                }
//                if (StringUtils.isNotEmpty(ds.getStatus()) && ds.getStatus().equalsIgnoreCase("Error")) {
//                    throw new Exception(Translator.get("i18n_invalid_ds"));
//                }
//                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//                String table = dataTableInfoDTO.getTable();
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//                datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
//                try {
//
//                    data.addAll(datasourceProvider.getData(datasourceRequest));
//                } catch (Exception e) {
//
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//
//            } else {
//                // check doris table
//                if (!checkEngineTableIsExists(dataSetTableRequest.getId())) {
//                    if (dataSetTableRequest.isPreviewForTask()) {
//                        map.put("fields", fields);
//                        map.put("data", new ArrayList<>());
//                        map.put("page", new DataSetPreviewPage());
//                        return map;
//                    } else {
//                        throw new RuntimeException(Translator.get("i18n_data_not_sync"));
//                    }
//                }
//                Datasource ds = engineService.getDeEngine();
//                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//                String table = TableUtils.tableName(dataSetTableRequest.getId());
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//                datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
//                map.put("sql", Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
//                try {
//                    data.addAll(jdbcProvider.getData(datasourceRequest));
//                } catch (Exception e) {
//
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//
//            }
//
//        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), DatasetType.SQL.name())) {
//            if (datasetTable.getMode() == 0) {
//                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
//                if (ObjectUtils.isEmpty(ds)) {
//                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
//                }
//                if (StringUtils.isNotEmpty(ds.getStatus()) && ds.getStatus().equalsIgnoreCase("Error")) {
//                    throw new Exception(Translator.get("i18n_invalid_ds"));
//                }
//                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//                DataTableInfoDTO dataTableInfo = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
//                String sql = dataTableInfo.isBase64Encryption() ? new String(Base64.getDecoder().decode(dataTableInfo.getSql())) : dataTableInfo.getSql();
//                sql = dataSetTableService.handleVariableDefaultValue(sql, datasetTable.getSqlVariableDetails(), ds.getType(), false);
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//                datasourceRequest.setQuery(qp.createQuerySQLWithPage(sql, fields, page, pageSize, realSize, false, null, rowPermissionsTree));
//                map.put("sql", Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
//                datasourceRequest.setPage(page);
//                datasourceRequest.setFetchSize(limit);
//                datasourceRequest.setPageSize(pageSize);
//                datasourceRequest.setRealSize(realSize);
//                datasourceRequest.setPreviewData(true);
//                try {
//                    datasourceRequest.setPageable(true);
//                    datasourceRequest.setPermissionFields(fields);
//                    data.addAll(datasourceProvider.getData(datasourceRequest));
//                } catch (Exception e) {
//
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//
//            } else {
//                // check doris table
//                if (!checkEngineTableIsExists(dataSetTableRequest.getId())) {
//                    throw new RuntimeException(Translator.get("i18n_data_not_sync"));
//                }
//                Datasource ds = engineService.getDeEngine();
//                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//                String table = TableUtils.tableName(dataSetTableRequest.getId());
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//                datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
//                map.put("sql", Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
//                try {
//                    data.addAll(jdbcProvider.getData(datasourceRequest));
//                } catch (Exception e) {
//
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//
//            }
//        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "excel")) {
//            if (!checkEngineTableIsExists(dataSetTableRequest.getId())) {
//                throw new RuntimeException(Translator.get("i18n_data_not_sync"));
//            }
//
//            Datasource ds = engineService.getDeEngine();
//            JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
//            DatasourceRequest datasourceRequest = new DatasourceRequest();
//            datasourceRequest.setDatasource(ds);
//            String table = TableUtils.tableName(dataSetTableRequest.getId());
//            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//            datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
//            map.put("sql", Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
//            try {
//                data.addAll(jdbcProvider.getData(datasourceRequest));
//            } catch (Exception e) {
//
//                DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//            }
//
//
//        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "custom")) {
//            if (datasetTable.getMode() == 0) {
//                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
//                if (ObjectUtils.isEmpty(ds)) {
//                    throw new RuntimeException(Translator.get("i18n_datasource_delete"));
//                }
//                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//
//                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
//                List<DataSetTableUnionDTO> list = dataSetTableUnionService.listByTableId(dt.getList().get(0).getTableId());
//
//                String sql = "";
//                try {
//                    sql = dataSetTableService.getCustomSQLDatasource(dt, list, ds);
//                } catch (Exception e) {
//
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//                datasourceRequest.setQuery(qp.createQuerySQLWithPage(sql, fields, page, pageSize, realSize, false, null, rowPermissionsTree));
//                map.put("sql", Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
//
//                try {
//
//                    data.addAll(datasourceProvider.getData(datasourceRequest));
//                } catch (Exception e) {
//
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//
//            } else {
//                Datasource ds = engineService.getDeEngine();
//                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//                String table = TableUtils.tableName(dataSetTableRequest.getId());
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//                datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
//                map.put("sql", Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
//                try {
//                    data.addAll(jdbcProvider.getData(datasourceRequest));
//                } catch (Exception e) {
//
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//
//            }
//        } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "union")) {
//            if (datasetTable.getMode() == 0) {
//                Datasource ds = datasourceMapper.selectByPrimaryKey(dataSetTableRequest.getDataSourceId());
//                if (ObjectUtils.isEmpty(ds)) {
//                    DataEaseException.throwException(Translator.get("i18n_datasource_delete"));
//                }
//                Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//
//                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
//
//                String sql = "";
//                try {
//                    sql = (String) dataSetTableService.getUnionSQLDatasource(dt, ds).get("sql");
//                } catch (Exception e) {
//
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//                datasourceRequest.setQuery(qp.createQuerySQLWithPage(sql, fields, page, pageSize, realSize, false, null, rowPermissionsTree));
//                map.put("sql", Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
//
//                try {
//
//                    data.addAll(datasourceProvider.getData(datasourceRequest));
//                } catch (Exception e) {
//
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//
//            } else {
//                Datasource ds = engineService.getDeEngine();
//                JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
//                DatasourceRequest datasourceRequest = new DatasourceRequest();
//                datasourceRequest.setDatasource(ds);
//                String table = TableUtils.tableName(dataSetTableRequest.getId());
//                QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
//                datasourceRequest.setQuery(qp.createQueryTableWithPage(table, fields, page, pageSize, realSize, false, ds, null, rowPermissionsTree));
//                map.put("sql", Base64.getEncoder().encodeToString(datasourceRequest.getQuery().getBytes()));
//                try {
//                    data.addAll(jdbcProvider.getData(datasourceRequest));
//                } catch (Exception e) {
//
//                    DataEaseException.throwException(Translator.get("i18n_ds_error") + "->" + e.getMessage());
//                }
//            }
//        }
//
//        List<Map<String, Object>> jsonArray = new ArrayList<>();
//        if (CollectionUtils.isNotEmpty(data)) {
//            jsonArray = data.stream().map(ele -> {
//                Map<String, Object> tmpMap = new HashMap<>();
//                for (int i = 0; i < ele.length; i++) {
//                    if (desensitizationList.keySet().contains(fieldArray[i])) {
//                        tmpMap.put(fieldArray[i], ChartDataBuild.desensitizationValue(desensitizationList.get(fieldArray[i]), String.valueOf(ele[i])));
//                    } else {
//                        tmpMap.put(fieldArray[i], ele[i]);
//                    }
//                }
//                return tmpMap;
//            }).collect(Collectors.toList());
//        }
//        map.put("fields", fields);
//        map.put("data", jsonArray);
//        return map;
//    }

    private static final String LOG_RETENTION = "30";

//    public void cleanLog() {
//        String value = systemParameterService.getValue(ParamConstants.BASIC.EXPORT_FILE_TIME_OUT.getValue());
//        value = StringUtils.isBlank(value) ? LOG_RETENTION : value;
//        int logRetention = Integer.parseInt(value);
//        Calendar instance = Calendar.getInstance();
//        Calendar startInstance = (Calendar) instance.clone();
//        startInstance.add(Calendar.DATE, -logRetention);
//        startInstance.set(Calendar.HOUR_OF_DAY, 0);
//        startInstance.set(Calendar.MINUTE, 0);
//        startInstance.set(Calendar.SECOND, 0);
//        startInstance.set(Calendar.MILLISECOND, -1);
//        long timeInMillis = startInstance.getTimeInMillis();
//        ExportTaskExample exportTaskExample = new ExportTaskExample();
//        ExportTaskExample.Criteria criteria = exportTaskExample.createCriteria();
//        criteria.andExportTimeLessThan(timeInMillis);
//        exportTaskMapper.selectByExample(exportTaskExample).forEach(exportTask -> {
//            delete(exportTask.getId());
//        });
//    }

}

