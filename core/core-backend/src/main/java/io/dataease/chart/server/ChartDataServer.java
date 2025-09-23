package io.dataease.chart.server;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.chart.ChartDataApi;
import io.dataease.api.chart.dto.ViewDetailField;
import io.dataease.api.chart.request.ChartExcelRequest;
import io.dataease.api.chart.request.ChartExcelRequestInner;
import io.dataease.auth.DeLinkPermit;
import io.dataease.chart.constant.ChartConstants;
import io.dataease.chart.manage.ChartDataManage;
import io.dataease.constant.AuthConstant;
import io.dataease.constant.CommonConstants;
import io.dataease.constant.LogOT;
import io.dataease.dataset.manage.PermissionManage;
import io.dataease.dataset.server.DatasetFieldServer;
import io.dataease.constant.DeTypeConstants;
import io.dataease.dataset.utils.DatasetUtils;
import io.dataease.exception.DEException;
import io.dataease.exportCenter.manage.ExportCenterManage;
import io.dataease.exportCenter.util.ExportCenterUtils;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.dto.*;
import io.dataease.i18n.Lang;
import io.dataease.license.manage.F2CLicLimitedManage;
import io.dataease.log.DeLog;
import io.dataease.result.ResultCode;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.LogUtil;
import io.dataease.visualization.manage.VisualizationTemplateExtendDataManage;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@RestController
@RequestMapping("/chartData")
public class ChartDataServer implements ChartDataApi {
    @Resource
    private ChartDataManage chartDataManage;
    @Resource
    private ExportCenterManage exportCenterManage;

    @Resource
    private VisualizationTemplateExtendDataManage extendDataManage;

    @Resource
    private PermissionManage permissionManage;
    @Resource
    private DatasetFieldServer datasetFieldServer;

    @Resource(name = "f2CLicLimitedManage")
    private F2CLicLimitedManage f2CLicLimitedManage;
    @Value("${dataease.export.page.size:50000}")
    private Integer extractPageSize;
    private final Long sheetLimit = 1000000L;


    @DeLinkPermit("#p0.sceneId")
    @Override
    public ChartViewDTO getData(ChartViewDTO chartViewDTO) throws Exception {
        try {
            // 从模板数据获取
            if (CommonConstants.VIEW_DATA_FROM.TEMPLATE.equalsIgnoreCase(chartViewDTO.getDataFrom())) {
                return extendDataManage.getChartDataInfo(chartViewDTO.getId(), chartViewDTO);
            } else {
                DatasetUtils.viewDecode(chartViewDTO);
                ChartViewDTO dto = chartDataManage.calcData(chartViewDTO);
                DatasetUtils.viewEncode(dto);
                chartDataManage.encodeData(dto);
                return dto;
            }
        } catch (Exception e) {
            DEException.throwException(ResultCode.DATA_IS_WRONG.code(), e.getMessage() + "\n\n" + ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    public ChartViewDTO findExcelData(ChartExcelRequest request) {
        ChartViewDTO chartViewInfo = new ChartViewDTO();
        try {
            ChartViewDTO viewDTO = request.getViewInfo();
            viewDTO.setIsExcelExport(true);
            String[] dsHeader = null;
            Integer[] dsTypes = null;
            //downloadType = dataset 为下载原始名字 这里做数据转换模拟 table-info类型图表导出
            if ("dataset".equals(request.getDownloadType())) {
                viewDTO.setResultMode(ChartConstants.VIEW_RESULT_MODE.ALL);
                viewDTO.setType("table-info");
                viewDTO.setRender("antv");
                List<DatasetTableFieldDTO> sourceFields = datasetFieldServer.listByDatasetGroup(viewDTO.getTableId());
                List<String> fileNames = permissionManage.filterColumnPermissions(sourceFields, new HashMap<>(), viewDTO.getTableId(), null).stream().map(DatasetTableFieldDTO::getDataeaseName).collect(Collectors.toList());
                sourceFields = sourceFields.stream().filter(datasetTableFieldDTO -> fileNames.contains(datasetTableFieldDTO.getDataeaseName())).collect(Collectors.toList());
                dsHeader = sourceFields.stream().map(DatasetTableFieldDTO::getName).toArray(String[]::new);
                dsTypes = sourceFields.stream().map(DatasetTableFieldDTO::getDeType).toArray(Integer[]::new);
                TypeReference<List<ChartViewFieldDTO>> listTypeReference = new TypeReference<List<ChartViewFieldDTO>>() {
                };
                viewDTO.setXAxis(JsonUtil.parseList(JsonUtil.toJSONString(sourceFields).toString(), listTypeReference));
            }
            int curLimit = Math.toIntExact(ExportCenterUtils.getExportLimit("view"));
            int curDsLimit = Math.toIntExact(ExportCenterUtils.getExportLimit("dataset"));
            int viewLimit = Math.min(curLimit, curDsLimit);
            if (ChartConstants.VIEW_RESULT_MODE.CUSTOM.equals(viewDTO.getResultMode())) {
                Integer limitCount = viewDTO.getResultCount();
                viewDTO.setResultCount(Math.min(viewLimit, limitCount));
            } else {
                viewDTO.setResultCount(viewLimit);
            }
            if (CommonConstants.VIEW_DATA_FROM.TEMPLATE.equalsIgnoreCase(viewDTO.getDataFrom())) {
                chartViewInfo = extendDataManage.getChartDataInfo(viewDTO.getId(), viewDTO);
            } else {
                chartViewInfo = chartDataManage.calcData(viewDTO);
            }
            List<Object[]> tableRow = (List) chartViewInfo.getData().get("sourceData");
            if ("dataset".equals(request.getDownloadType())) {
                request.setHeader(dsHeader);
                request.setExcelTypes(dsTypes);
            }
            request.setDetails(tableRow);
            request.setData(chartViewInfo.getData());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return chartViewInfo;
    }


    public static String valueFormatter(BigDecimal value, FormatterCfgDTO formatter) {
        if (value == null) {
            return null;
        }
        String result;
        if (formatter.getType().equals("auto")) {
            result = transSeparatorAndSuffix(String.valueOf(transUnit(value, formatter)), formatter);
        } else if (formatter.getType().equals("value")) {
            result = transSeparatorAndSuffix(transDecimal(transUnit(value, formatter), formatter), formatter);
        } else if (formatter.getType().equals("percent")) {
            value = value.multiply(BigDecimal.valueOf(100));
            result = transSeparatorAndSuffix(transDecimal(value, formatter), formatter);
        } else {
            result = value.toString();
        }
        return result;
    }

    private static BigDecimal transUnit(BigDecimal value, FormatterCfgDTO formatter) {
        return value.divide(BigDecimal.valueOf(formatter.getUnit()));
    }

    private static String transDecimal(BigDecimal value, FormatterCfgDTO formatter) {
        DecimalFormat df = new DecimalFormat("0." + new String(new char[formatter.getDecimalCount()]).replace('\0', '0'));
        return df.format(value);
    }

    private static String transSeparatorAndSuffix(String value, FormatterCfgDTO formatter) {
        StringBuilder sb = new StringBuilder(value);

        if (formatter.getThousandSeparator()) {
            Pattern thousandsPattern = Pattern.compile("(\\d)(?=(\\d{3})+$)");
            String[] numArr = value.split("\\.");
            numArr[0] = addThousandSeparator(numArr[0], thousandsPattern);
            sb = new StringBuilder(String.join(".", numArr));
        }
        if (formatter.getType().equals("percent")) {
            sb.append('%');
        } else {
            switch (formatter.getUnit()) {
                case 1000:
                    sb.append("千");
                    break;
                case 10000:
                    sb.append("万");
                    break;
                case 1000000:
                    sb.append("百万");
                    break;
                case 100000000:
                    sb.append('亿');
                    break;
                default:
                    break;
            }
        }
        String suffix = formatter.getSuffix().trim();
        if (!suffix.isEmpty()) {
            if (suffix.equals("%")) {
                sb.append("\"%\"");
            } else {
                sb.append(suffix);
            }
        }
        return sb.toString();
    }


    private static String addThousandSeparator(String numStr, Pattern pattern) {
        Matcher matcher = pattern.matcher(numStr);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1) + ",");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }


    @DeLinkPermit("#p0.dvId")
    @Override
    public void innerExportDetails(ChartExcelRequest request, HttpServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String linkToken = httpServletRequest.getHeader(AuthConstant.LINK_TOKEN_KEY);
        LogUtil.info(request.getViewInfo().getId() + " " + StringUtils.isNotEmpty(linkToken) + " " + request.isDataEaseBi());
        if ((StringUtils.isNotEmpty(linkToken) && !request.isDataEaseBi()) || (request.isDataEaseBi() && StringUtils.isEmpty(linkToken))) {
            OutputStream outputStream = response.getOutputStream();
            try {
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

                if ("dataset".equals(request.getDownloadType()) || request.getViewInfo().getType().equalsIgnoreCase("table-info") || request.getViewInfo().getType().equalsIgnoreCase("table-normal")) {
                    List<Object[]> details = new ArrayList<>();
                    Sheet detailsSheet;
                    Integer sheetIndex = 1;
                    request.getViewInfo().getChartExtRequest().setPageSize(Long.valueOf(extractPageSize));
                    ChartViewDTO chartViewDTO = findExcelData(request);
                    for (long i = 1; i < chartViewDTO.getTotalPage() + 1; i++) {
                        request.getViewInfo().getChartExtRequest().setGoPage(i);
                        request.getViewInfo().setXAxis(request.getViewInfo().getXAxis().stream().filter(ele -> !ele.isHide()).collect(Collectors.toList()));
                        request.getViewInfo().setYAxis(request.getViewInfo().getYAxis().stream().filter(ele -> !ele.isHide()).collect(Collectors.toList()));
                        request.getViewInfo().setXAxisExt(request.getViewInfo().getXAxisExt().stream().filter(ele -> !ele.isHide()).collect(Collectors.toList()));
                        request.getViewInfo().setYAxisExt(request.getViewInfo().getYAxisExt().stream().filter(ele -> !ele.isHide()).collect(Collectors.toList()));
                        request.getViewInfo().setExtStack(request.getViewInfo().getExtStack().stream().filter(ele -> !ele.isHide()).collect(Collectors.toList()));
                        findExcelData(request);
                        details.addAll(request.getDetails());
                        if ((details.size() + extractPageSize) > sheetLimit || i == chartViewDTO.getTotalPage()) {
                            detailsSheet = wb.createSheet("数据" + sheetIndex);
                            Integer[] excelTypes = request.getExcelTypes();
                            List<ChartViewFieldDTO> xAxis = new ArrayList<>();
                            xAxis.addAll(request.getViewInfo().getXAxis());
                            xAxis.addAll(request.getViewInfo().getYAxis());
                            xAxis.addAll(request.getViewInfo().getXAxisExt());
                            xAxis.addAll(request.getViewInfo().getYAxisExt());
                            xAxis.addAll(request.getViewInfo().getExtStack());
                            Object[] header = Arrays.stream(request.getHeader()).filter(item -> xAxis.stream().map(d -> StringUtils.isNotBlank(d.getChartShowName()) ? d.getChartShowName() : d.getName()).toList().contains(item)).collect(Collectors.toList()).toArray();
                            details.add(0, header);
                            ViewDetailField[] detailFields = request.getDetailFields();
                            ChartDataServer.setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes, request.getViewInfo(), wb);
                            sheetIndex++;
                            details.clear();
                        }
                    }
                } else {
                    findExcelData(request);
                    if (CollectionUtils.isEmpty(request.getMultiInfo())) {
                        List<Object[]> details = request.getDetails();
                        Integer[] excelTypes = request.getExcelTypes();
                        details.add(0, request.getHeader());
                        ViewDetailField[] detailFields = request.getDetailFields();
                        Object[] header = request.getHeader();
                        Sheet detailsSheet = wb.createSheet("数据");
                        if (request.getViewInfo().getType().equalsIgnoreCase("table-normal")) {
                            setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes, request.getViewInfo(), wb);
                        } else {
                            setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes, request.getViewInfo(), null);
                        }
                    } else {
                        for (int i = 0; i < request.getMultiInfo().size(); i++) {
                            ChartExcelRequestInner requestInner = request.getMultiInfo().get(i);
                            List<Object[]> details = requestInner.getDetails();
                            Integer[] excelTypes = requestInner.getExcelTypes();
                            details.add(0, requestInner.getHeader());
                            ViewDetailField[] detailFields = requestInner.getDetailFields();
                            Object[] header = requestInner.getHeader();
                            Sheet detailsSheet = wb.createSheet("数据 " + (i + 1));
                            setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes, request.getViewInfo(), null);
                        }
                    }
                }
                exportCenterManage.addWatermarkTools(wb);
                response.setContentType("application/vnd.ms-excel");
                //文件名称
                response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(request.getViewName(), StandardCharsets.UTF_8) + ".xlsx");
                wb.write(outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                DEException.throwException(e);
            }
        } else {
            exportCenterManage.addTask(request.getViewId(), "chart", request, request.getBusiFlag());
        }
    }

    @DeLinkPermit("#p0.dvId")
    @Override
    public void innerExportDataSetDetails(ChartExcelRequest request, HttpServletResponse response) throws Exception {
        this.innerExportDetails(request, response);
    }

    public static void setExcelData(Sheet detailsSheet, CellStyle cellStyle, Object[] header, List<Object[]> details, ViewDetailField[] detailFields, Integer[] excelTypes, ChartViewDTO viewInfo, Workbook wb) {
        setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes, null, viewInfo, wb);
    }


    public static void setExcelData(Sheet detailsSheet, CellStyle cellStyle, Object[] header, List<Object[]> details, ViewDetailField[] detailFields, Integer[] excelTypes, Comment comment, ChartViewDTO viewInfo, Workbook wb) {
        List<CellStyle> styles = new ArrayList<>();
        List<ChartViewFieldDTO> xAxis = new ArrayList<>();

        xAxis.addAll(viewInfo.getXAxis());
        xAxis.addAll(viewInfo.getYAxis());
        xAxis.addAll(viewInfo.getXAxisExt());
        xAxis.addAll(viewInfo.getYAxisExt());
        xAxis.addAll(viewInfo.getExtStack());
        xAxis.addAll(viewInfo.getDrillFields());
        TableHeader tableHeader = null;
        Integer totalDepth = 0;
        List<CellRangeAddress> mergeConfig = new ArrayList<>();
        if (StringUtils.equalsAnyIgnoreCase(viewInfo.getType(), "table-normal", "table-info")) {
            for (ChartViewFieldDTO xAxi : xAxis) {
                if (xAxi.getDeType().equals(DeTypeConstants.DE_INT) || xAxi.getDeType().equals(DeTypeConstants.DE_FLOAT)) {
                    CellStyle formatterCellStyle = createCellStyle(wb, xAxi.getFormatterCfg(), null);
                    styles.add(formatterCellStyle);
                } else {
                    styles.add(null);
                }
            }

            Map<String, Object> customAttr = viewInfo.getCustomAttr();
            Map<String, Object> tableHeaderMap = (Map<String, Object>) customAttr.get("tableHeader");
            if (tableHeaderMap.get("headerGroup") != null && Boolean.parseBoolean(tableHeaderMap.get("headerGroup").toString())) {
                var tmpHeader = JsonUtil.parseObject((String) JsonUtil.toJSONString(customAttr.get("tableHeader")), TableHeader.class);
                // 校验字段数量和顺序
                var allAxis = new ArrayList<>(viewInfo.getXAxis().stream().filter(x -> !x.isHide()).toList());
                if (StringUtils.equalsIgnoreCase(viewInfo.getType(), "table-normal")) {
                    allAxis.addAll(viewInfo.getYAxis().stream().filter(x -> !x.isHide()).toList());
                }
                if (validateHeaderGroup(tmpHeader, allAxis)) {
                    tableHeader = tmpHeader;
                    for (TableHeader.ColumnInfo column : tableHeader.getHeaderGroupConfig().getColumns()) {
                        totalDepth = Math.max(totalDepth, getDepth(column, 1));
                    }
                    for (TableHeader.ColumnInfo column : tableHeader.getHeaderGroupConfig().getColumns()) {
                        setWidth(column, 1);
                    }
                }
            }
            if ("table-info".equalsIgnoreCase(viewInfo.getType()) && !"dataset".equalsIgnoreCase(viewInfo.getDownloadType())) {
                Map<String, Object> tableCell = (Map<String, Object>) viewInfo.getCustomAttr().get("tableCell");
                Boolean mergeCells = (Boolean) tableCell.get("mergeCells");
                if (mergeCells != null && mergeCells) {
                    var mergeIndex = viewInfo.getXAxis().size();
                    for (int i = 0; i < viewInfo.getXAxis().size(); i++) {
                        if ("q".equalsIgnoreCase(viewInfo.getXAxis().get(i).getGroupType())) {
                            mergeIndex = i;
                            break;
                        }
                    }
                    if (mergeIndex >= 1 && details.size() > 1) {
                        mergeConfig = getMergeConfig(details.subList(1, details.size()), mergeIndex - 1, totalDepth == 0 ? 1 : totalDepth);
                    }
                }
            }
        }

        boolean mergeHead = false;
        if (ArrayUtils.isNotEmpty(detailFields)) {
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            String[] detailField = Arrays.stream(detailFields).map(ViewDetailField::getName).toList().toArray(new String[detailFields.length]);

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
            int realDetailRowIndex = tableHeader == null ? 2 : totalDepth;
            if (tableHeader != null) {
                cellStyle.setBorderTop(BorderStyle.THIN);
                cellStyle.setBorderRight(BorderStyle.THIN);
                cellStyle.setBorderBottom(BorderStyle.THIN);
                cellStyle.setBorderLeft(BorderStyle.THIN);
                Map<String, Row> rowMap = new HashMap<>();
                for (Integer i = 0; i < totalDepth; i++) {
                    rowMap.put("row" + i, detailsSheet.createRow(i));
                }
                int width = 0;
                Integer depth = 0;
                for (TableHeader.ColumnInfo column : tableHeader.getHeaderGroupConfig().getColumns()) {
                    createCell(tableHeader, column, width, depth, detailsSheet, cellStyle, totalDepth, rowMap, xAxis);
                    width = width + column.getWidth();
                }
            }
            for (int i = (mergeHead ? 2 : 0); i < details.size(); i++) {
                int rowIndex = i;
                if (tableHeader != null) {
                    rowIndex = realDetailRowIndex - 1 + i;
                } else {
                    rowIndex = realDetailRowIndex > 2 ? realDetailRowIndex : i;
                }
                Row row = detailsSheet.createRow(rowIndex);
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
                                if ((viewInfo.getType().equalsIgnoreCase("table-normal") || viewInfo.getType().equalsIgnoreCase("table-info")) && (xAxis.get(j).getDeType().equals(DeTypeConstants.DE_INT) || xAxis.get(j).getDeType().equals(DeTypeConstants.DE_FLOAT))) {
                                    try {
                                        FormatterCfgDTO formatterCfgDTO = xAxis.get(j).getFormatterCfg() == null ? new FormatterCfgDTO().setUnitLanguage(Lang.isChinese() ? "ch" : "en") : xAxis.get(j).getFormatterCfg();
                                        row.getCell(j).setCellStyle(styles.get(j));
                                        row.getCell(j).setCellValue(Double.valueOf(cellValue(formatterCfgDTO, new BigDecimal(cellValObj.toString()))));
                                    } catch (Exception e) {
                                        cell.setCellValue(cellValObj.toString());
                                    }
                                } else {
                                    if ((excelTypes[j].equals(DeTypeConstants.DE_INT) || excelTypes[j].equals(DeTypeConstants.DE_FLOAT)) && StringUtils.isNotEmpty(cellValObj.toString())) {
                                        cell.setCellValue(Double.valueOf(cellValObj.toString()));
                                    } else if (cellValObj != null) {
                                        cell.setCellValue(cellValObj.toString());
                                    }
                                }
                            } catch (Exception e) {
                                LogUtil.warn("export excel data transform error");
                            }
                        } else {
                            if (!viewInfo.getType().equalsIgnoreCase("circle-packing")) {
                                Map<String, Object> senior = viewInfo.getSenior();
                                viewInfo.getCustomAttr().get("");
                                ChartSeniorFunctionCfgDTO functionCfgDTO = JsonUtil.parseObject((String) JsonUtil.toJSONString(senior.get("functionCfg")), ChartSeniorFunctionCfgDTO.class);
                                if (functionCfgDTO != null && StringUtils.isNotEmpty(functionCfgDTO.getEmptyDataStrategy()) && functionCfgDTO.getEmptyDataStrategy().equalsIgnoreCase("setZero")) {
                                    if ((viewInfo.getType().equalsIgnoreCase("table-normal") || viewInfo.getType().equalsIgnoreCase("table-info"))) {
                                        if (functionCfgDTO.getEmptyDataFieldCtrl().contains(xAxis.get(j).getDataeaseName())) {
                                            cell.setCellValue(0);
                                        }
                                    } else {
                                        cell.setCellValue(0);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(mergeConfig)) {
                mergeConfig.forEach(detailsSheet::addMergedRegion);
            }
        }
    }

    private static List<CellRangeAddress> getMergeConfig(List<Object[]> data, int colIndex, int offsetHeight) {
        var result = new ArrayList<CellRangeAddress>();
        var preRange = new ArrayList<Integer[]>();
        var initRange = new Integer[]{0, data.size() - 1};
        preRange.add(initRange);
        for (int curColIndex = 0; curColIndex <= colIndex; curColIndex++) {
            var curRange = new ArrayList<Integer[]>();
            for (int preRangeIndex = 0; preRangeIndex < preRange.size(); preRangeIndex++) {
                var preRowRange = preRange.get(preRangeIndex);
                var start = preRowRange[0];
                var end = preRowRange[1];
                var lastColValue = data.get(start)[curColIndex];
                if (lastColValue != null) {
                    lastColValue = lastColValue.toString();
                } else {
                    lastColValue = "";
                }
                var lastRowIndex = start;
                for (Integer curRowIndex = start + 1; curRowIndex <= end; curRowIndex++) {
                    var curRow = data.get(curRowIndex);
                    var curColValue = curRow[curColIndex];
                    if (curColValue != null) {
                        curColValue = curColValue.toString();
                    } else {
                        curColValue = "";
                    }
                    if (!StringUtils.equals(lastColValue.toString(), curColValue.toString()) && (curRowIndex - lastRowIndex > 1)) {
                        curRange.add(new Integer[]{lastRowIndex, curRowIndex - 1});
                        result.add(new CellRangeAddress(lastRowIndex + offsetHeight, curRowIndex + offsetHeight - 1, curColIndex, curColIndex));
                    }
                    if (curRowIndex.equals(end) && curColValue.equals(lastColValue) && curRowIndex - lastRowIndex > 0) {
                        curRange.add(new Integer[]{lastRowIndex, curRowIndex});
                        result.add(new CellRangeAddress(lastRowIndex + offsetHeight, curRowIndex + offsetHeight, curColIndex, curColIndex));
                    }
                    if (!StringUtils.equals(lastColValue.toString(), curColValue.toString())) {
                        lastColValue = curColValue;
                        lastRowIndex = curRowIndex;
                    }
                }
            }
            preRange = curRange;
        }
        return result;
    }

    private static boolean validateHeaderGroup(TableHeader header, List<ChartViewFieldDTO> fields) {
        if (header == null) {
            return false;
        }
        var columns = header.getHeaderGroupConfig().getColumns();
        if (CollectionUtils.isEmpty(columns)) {
            return false;
        }
        var leafColumn = getHeaderLeafColumn(columns);
        if (CollectionUtils.isEmpty(leafColumn) || leafColumn.size() != fields.size()) {
            return false;
        }
        for (int i = 0; i < leafColumn.size(); i++) {
            var a = leafColumn.get(i);
            var b = fields.get(i).getDataeaseName();
            if (!StringUtils.equals(a, b)) {
                return false;
            }
        }
        return true;
    }

    private static List<String> getHeaderLeafColumn(List<TableHeader.ColumnInfo> columns) {
        var result = new ArrayList<String>();
        for (TableHeader.ColumnInfo column : columns) {
            if (CollectionUtils.isEmpty(column.getChildren())) {
                result.add(column.getKey());
            } else {
                result.addAll(getHeaderLeafColumn(column.getChildren()));
            }
        }
        return result;
    }

    private static Integer getDepth(TableHeader.ColumnInfo column, Integer parentDepth) {
        if (org.springframework.util.CollectionUtils.isEmpty(column.getChildren())) {
            return parentDepth;
        } else {
            Integer depth = 0;
            for (TableHeader.ColumnInfo child : column.getChildren()) {
                depth = Math.max(depth, getDepth(child, parentDepth + 1));
            }
            return depth;
        }
    }

    private static void createCell(TableHeader tableHeader, TableHeader.ColumnInfo column, Integer width, Integer depth, Sheet sheet, CellStyle cellStyle, Integer totaalDepth, Map<String, Row> rowMap, List<ChartViewFieldDTO> xAxis) {
        if (org.springframework.util.CollectionUtils.isEmpty(column.getChildren())) {
            Integer toDepth = totaalDepth - 1 > depth ? totaalDepth - 1 : depth;
            if (depth.equals(toDepth)) {
                Cell cell = rowMap.get("row" + depth).createCell(width);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(getDeFieldName(xAxis, column.getKey()));
            } else {
                for (int i = depth; i <= toDepth; i++) {
                    Cell cell1 = rowMap.get("row" + i).createCell(width);
                    cell1.setCellValue(getDeFieldName(xAxis, column.getKey()));
                    cell1.setCellStyle(cellStyle);
                }
                CellRangeAddress region = new CellRangeAddress(depth, toDepth, width, width);
                sheet.addMergedRegion(region);

                Cell mergedCell = rowMap.get("row" + depth).getCell(width);
                mergedCell.setCellStyle(cellStyle);

            }
        } else {
            Cell cell1 = rowMap.get("row" + depth).createCell(width);
            cell1.setCellValue(getGroupName(tableHeader, column.getKey()));
            cell1.setCellStyle(cellStyle);
            Cell cell2 = rowMap.get("row" + depth).createCell(width + column.getWidth() - 1);
            cell2.setCellValue(getGroupName(tableHeader, column.getKey()));
            cell2.setCellStyle(cellStyle);
            CellRangeAddress region = new CellRangeAddress(depth, depth, width, width + column.getWidth() - 1);
            sheet.addMergedRegion(region);
            Cell mergedCell = rowMap.get("row" + depth).getCell(width);
            mergedCell.setCellStyle(cellStyle);
            int subWith = width;
            for (TableHeader.ColumnInfo child : column.getChildren()) {
                createCell(tableHeader, child, subWith, depth + 1, sheet, cellStyle, totaalDepth, rowMap, xAxis);
                subWith = subWith + child.getWidth();
            }
        }
    }

    private static String getGroupName(TableHeader tableHeader, String key) {
        for (TableHeader.MetaInfo metaInfo : tableHeader.getHeaderGroupConfig().getMeta()) {
            if (metaInfo.getField().equals(key)) {
                return metaInfo.getName();
            }
        }
        return "";
    }

    private static String getDeFieldName(List<ChartViewFieldDTO> xAxis, String key) {
        for (ChartViewFieldDTO xAxi : xAxis) {
            if (xAxi.getDataeaseName().equals(key)) {
                return StringUtils.isNotBlank(xAxi.getChartShowName()) ? xAxi.getChartShowName() : xAxi.getName();
            }
        }
        return "";
    }

    private static Integer setWidth(TableHeader.ColumnInfo column, Integer parentWidth) {
        if (org.springframework.util.CollectionUtils.isEmpty(column.getChildren())) {
            column.setWidth(parentWidth);
            return parentWidth;
        } else {
            Integer depth = 0;
            for (TableHeader.ColumnInfo child : column.getChildren()) {
                depth = depth + setWidth(child, 1);
            }
            column.setWidth(depth);
            return depth;
        }
    }

    private static String cellValue(FormatterCfgDTO formatterCfgDTO, BigDecimal value) {
        if (formatterCfgDTO.getType().equalsIgnoreCase("percent")) {
            return value.toString();
        } else {
            return value.divide(BigDecimal.valueOf(formatterCfgDTO.getUnit())).toString();
        }
    }

    private static CellStyle createCellStyle(Workbook workbook, FormatterCfgDTO formatter, String value) {
        CellStyle cellStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();

        if (formatter == null) {
            cellStyle.setDataFormat(format.getFormat("General"));
            return cellStyle;
        }
        String formatStr = "";
        if (formatter.getType().equals("auto")) {
            String[] valueSplit = String.valueOf(value).split(".");
            if (StringUtils.isEmpty(value) || !value.contains(".")) {
                formatStr = "General";
            } else {
                formatStr = "0." + new String(new char[valueSplit.length]).replace('\0', '0');
            }
            switch (formatter.getUnit()) {
                case 1000:
                    formatStr = formatStr + "\"千\"";
                    break;
                case 10000:
                    formatStr = formatStr + "\"万\"";
                    break;
                case 1000000:
                    formatStr = formatStr + "\"百万\"";
                    break;
                case 100000000:
                    formatStr = formatStr + "\"亿\"";
                    break;
                default:
                    break;
            }
            if (formatter.getThousandSeparator()) {
                formatStr = "#,##" + formatStr;
            }
            if (StringUtils.isNotEmpty(formatter.getSuffix())) {
                if (formatter.getSuffix().equals("%")) {
                    formatStr = formatStr + "\"%\"";
                } else {
                    formatStr = formatStr + "\"" + formatter.getSuffix() + "\"";
                }
            }
        }
        if (formatter.getType().equals("value")) {
            if (formatter.getDecimalCount() > 0) {
                formatStr = "0." + new String(new char[formatter.getDecimalCount()]).replace('\0', '0');
            } else {
                formatStr = "0";
            }
            switch (formatter.getUnit()) {
                case 1000:
                    formatStr = formatStr + "\"千\"";
                    break;
                case 10000:
                    formatStr = formatStr + "\"万\"";
                    break;
                case 1000000:
                    formatStr = formatStr + "\"百万\"";
                    break;
                case 100000000:
                    formatStr = formatStr + "\"亿\"";
                    break;
                default:
                    break;
            }
            if (formatter.getThousandSeparator()) {
                formatStr = "#,##" + formatStr;
            }
            if (StringUtils.isNotEmpty(formatter.getSuffix())) {
                if (formatter.getSuffix().equals("%")) {
                    formatStr = formatStr + "\"%\"";
                } else {
                    formatStr = formatStr + "\"" + formatter.getSuffix() + "\"";
                }
            }
        } else if (formatter.getType().equals("percent")) {
            if (formatter.getDecimalCount() > 0) {
                formatStr = "0." + new String(new char[formatter.getDecimalCount()]).replace('\0', '0');
            } else {
                formatStr = "0";
            }
            formatStr = formatStr + "%";
        }
        if (StringUtils.isNotEmpty(formatStr)) {
            cellStyle.setDataFormat(format.getFormat(formatStr));
        } else {
            return null;
        }
        return cellStyle;
    }


    @Override
    public List<String> getFieldData(ChartViewDTO view, Long fieldId, String fieldType) throws Exception {
        return chartDataManage.getFieldData(view, fieldId, fieldType);
    }

    @Override
    public List<String> getDrillFieldData(ChartViewDTO view, Long fieldId) throws Exception {
        return chartDataManage.getDrillFieldData(view, fieldId);
    }
}
