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
import io.dataease.dataset.manage.PermissionManage;
import io.dataease.dataset.server.DatasetFieldServer;
import io.dataease.engine.constant.DeTypeConstants;
import io.dataease.exception.DEException;
import io.dataease.exportCenter.manage.ExportCenterManage;
import io.dataease.exportCenter.util.ExportCenterUtils;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import io.dataease.extensions.view.dto.FormatterCfgDTO;
import io.dataease.license.manage.F2CLicLimitedManage;
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
import org.apache.poi.xssf.usermodel.*;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
                return chartDataManage.calcData(chartViewDTO);
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
            chartViewInfo = getData(viewDTO);
            List<Object[]> tableRow = (List) chartViewInfo.getData().get("sourceData");
            if ("dataset".equals(request.getDownloadType())) {
                request.setHeader(dsHeader);
                request.setExcelTypes(dsTypes);
            }
            if (CollectionUtils.isNotEmpty(tableRow)) {
                FormatterCfgDTO formatterCfgDTO = new FormatterCfgDTO();
                for (Object[] objects : tableRow) {
                    for (int i = 0; i < viewDTO.getXAxis().size(); i++) {
                        if (viewDTO.getXAxis().get(i).getDeType().equals(DeTypeConstants.DE_INT) || viewDTO.getXAxis().get(i).getDeType().equals(DeTypeConstants.DE_FLOAT)) {
                            try {
                                objects[i] = valueFormatter(new BigDecimal(String.valueOf(objects[i])), viewDTO.getXAxis().get(i).getFormatterCfg() == null ? formatterCfgDTO : viewDTO.getXAxis().get(i).getFormatterCfg());
                            } catch (Exception ignore) {
                                ignore.printStackTrace();
                            }
                        }
                    }
                }
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
            sb.append(suffix);
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

                if ("dataset".equals(request.getDownloadType()) || request.getViewInfo().getType().equalsIgnoreCase("table-info")) {
                    List<Object[]> details = new ArrayList<>();
                    Sheet detailsSheet;
                    Integer sheetIndex = 1;
                    request.getViewInfo().getChartExtRequest().setPageSize(Long.valueOf(extractPageSize));
                    ChartViewDTO chartViewDTO = findExcelData(request);
                    for (long i = 1; i < chartViewDTO.getTotalPage() + 1; i++) {
                        request.getViewInfo().getChartExtRequest().setGoPage(i);
                        findExcelData(request);
                        details.addAll(request.getDetails());
                        if ((details.size() + extractPageSize) > sheetLimit || i == chartViewDTO.getTotalPage()) {
                            detailsSheet = wb.createSheet("数据" + sheetIndex);
                            Integer[] excelTypes = request.getExcelTypes();
                            details.add(0, request.getHeader());
                            ViewDetailField[] detailFields = request.getDetailFields();
                            Object[] header = request.getHeader();
                            ChartDataServer.setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes);
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
                        setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes);
                    } else {
                        for (int i = 0; i < request.getMultiInfo().size(); i++) {
                            ChartExcelRequestInner requestInner = request.getMultiInfo().get(i);
                            List<Object[]> details = requestInner.getDetails();
                            Integer[] excelTypes = requestInner.getExcelTypes();
                            details.add(0, requestInner.getHeader());
                            ViewDetailField[] detailFields = requestInner.getDetailFields();
                            Object[] header = requestInner.getHeader();
                            Sheet detailsSheet = wb.createSheet("数据 " + (i + 1));
                            setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes);
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
            exportCenterManage.addTask(request.getViewId(), "chart", request);
            return;
        }
    }

    @DeLinkPermit("#p0.dvId")
    @Override
    public void innerExportDataSetDetails(ChartExcelRequest request, HttpServletResponse response) throws Exception {
        this.innerExportDetails(request, response);
    }
    public static void setExcelData(Sheet detailsSheet, CellStyle cellStyle, Object[] header, List<Object[]> details, ViewDetailField[] detailFields, Integer[] excelTypes) {
        setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes,null);
    }


    public static void setExcelData(Sheet detailsSheet, CellStyle cellStyle, Object[] header, List<Object[]> details, ViewDetailField[] detailFields, Integer[] excelTypes,Comment comment) {
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
                                cell.setCellValue(cellValObj.toString());
                            } catch (Exception e) {
                                LogUtil.warn("export excel data transform error");
                            }
                        }


                    }
                }
            }
        }
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
