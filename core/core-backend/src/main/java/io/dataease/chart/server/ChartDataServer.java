package io.dataease.chart.server;

import io.dataease.api.chart.ChartDataApi;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.api.chart.dto.ViewDetailField;
import io.dataease.api.chart.request.ChartExcelRequest;
import io.dataease.api.chart.request.ChartExcelRequestInner;
import io.dataease.chart.constant.ChartConstants;
import io.dataease.chart.manage.ChartDataManage;
import io.dataease.constant.AuthConstant;
import io.dataease.constant.CommonConstants;
import io.dataease.engine.constant.DeTypeConstants;
import io.dataease.exception.DEException;
import io.dataease.exportCenter.manage.ExportCenterManage;
import io.dataease.result.ResultCode;
import io.dataease.utils.LogUtil;
import io.dataease.visualization.manage.VisualizationTemplateExtendDataManage;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author Junjun
 */
@RestController
@RequestMapping("chartData")
public class ChartDataServer implements ChartDataApi {
    @Resource
    private ChartDataManage chartDataManage;
    @Resource
    private ExportCenterManage exportCenterManage;

    @Resource
    private VisualizationTemplateExtendDataManage extendDataManage;
    @Value("${export.views.limit:500000}")
    private Integer limit;

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
            DEException.throwException(ResultCode.DATA_IS_WRONG.code(), e.getMessage());
        }
        return null;
    }

    public void findExcelData(ChartExcelRequest request) {
        try {
            ChartViewDTO viewDTO = request.getViewInfo();
            viewDTO.setIsExcelExport(true);
            if(ChartConstants.VIEW_RESULT_MODE.CUSTOM.equals(viewDTO.getResultMode())){
                Integer limitCount = viewDTO.getResultCount();
                viewDTO.setResultCount(limitCount>limit?limit:limitCount);
            }else{
                viewDTO.setResultCount(limit);
            }
            ChartViewDTO chartViewInfo = getData(viewDTO);
            List<Object[]> tableRow = (List) chartViewInfo.getData().get("sourceData");
            request.setDetails(tableRow);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void innerExportDetails(ChartExcelRequest request, HttpServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String linkToken = httpServletRequest.getHeader(AuthConstant.LINK_TOKEN_KEY);
        if (StringUtils.isEmpty(linkToken)) {
            exportCenterManage.addTask(request.getViewId(), "chart", request);
            return;
        }
        OutputStream outputStream = response.getOutputStream();
        try {
            findExcelData(request);

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

                setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes);
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

                    setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes);
                }
            }

            response.setContentType("application/vnd.ms-excel");
            //文件名称
            response.setHeader("Content-disposition", "attachment;filename=" + request.getViewName() + ".xlsx");
            wb.write(outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            DEException.throwException(e);
        }
    }


    public static void setExcelData(Sheet detailsSheet, CellStyle cellStyle, Object[] header, List<Object[]> details, ViewDetailField[] detailFields, Integer[] excelTypes) {
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
    }

    @Override
    public List<String> getFieldData(ChartViewDTO view, Long fieldId, String fieldType) throws Exception {
        return chartDataManage.getFieldData(view, fieldId, fieldType);
    }
}
