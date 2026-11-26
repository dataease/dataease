package io.dataease.chart.server;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.chart.ChartDataApi;
import io.dataease.api.chart.dto.ViewDetailField;
import io.dataease.api.chart.request.ChartCalcFieldValidateRequest;
import io.dataease.api.chart.request.ChartExcelRequest;
import io.dataease.api.chart.request.ChartExcelRequestInner;
import io.dataease.auth.DeLinkPermit;
import io.dataease.chart.constant.ChartConstants;
import io.dataease.chart.manage.ChartDataManage;
import io.dataease.chart.manage.ExcelExportContentService;
import io.dataease.constant.*;
import io.dataease.dataset.manage.PermissionManage;
import io.dataease.dataset.server.DatasetFieldServer;
import io.dataease.dataset.utils.DatasetUtils;
import io.dataease.exception.DEException;
import io.dataease.exportCenter.manage.ExportCenterDownLoadManage;
import io.dataease.exportCenter.manage.ExportCenterManage;
import io.dataease.exportCenter.manage.ExportContentService;
import io.dataease.exportCenter.util.ExportCenterUtils;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.dto.*;
import io.dataease.i18n.Lang;
import io.dataease.license.manage.F2CLicLimitedManage;
import io.dataease.log.DeLog;
import io.dataease.result.ResultCode;
import io.dataease.utils.CommonBeanFactory;
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
import org.apache.poi.ss.util.RegionUtil;
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
    private io.dataease.share.manage.ShareVisitorPermissionManage shareVisitorPermissionManage;

    @Resource
    private ChartDataManage chartDataManage;
    @Resource
    private ExcelExportContentService excelExportContentService;
    @Resource
    private ExportCenterManage exportCenterManage;

    @Resource
    private VisualizationTemplateExtendDataManage extendDataManage;

    @Resource
    private PermissionManage permissionManage;
    @Resource
    private ExportContentService exportContentService;

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
        }  catch (Exception e) {
            DEException.throwException(ResultCode.DATA_IS_WRONG.code(), e.getMessage() + "\n\n" + ExceptionUtils.getStackTrace(e));
        }
        return null;
    }

    @Override
    public void validateCalcField(ChartCalcFieldValidateRequest request) throws Exception {
        try {
            if (StringUtils.isNotBlank(request.getOriginName())) {
                request.setOriginName(DatasetUtils.getDecode(request.getOriginName()));
            }
            chartDataManage.validateCalcField(request);
        } catch (Exception e) {
            // 校验失败使用普通业务异常，避免图表取数错误码被前端当作成功响应。
            DEException.throwException(e.getMessage());
        }
    }

    public ChartViewDTO findExcelData(ChartExcelRequest request) {
        return exportContentService.findExcelData(request);
    }

    private int getExcelExportLimit(String downloadType) {
        long viewLimit = ExportCenterUtils.getExportLimit("view");
        if ("dataset".equals(downloadType)) {
            long datasetLimit = ExportCenterUtils.getExportLimit("dataset");
            return Math.toIntExact(Math.min(viewLimit, datasetLimit));
        }
        return Math.toIntExact(viewLimit);
    }

    @Override
    public List<String> getFieldData(ChartViewDTO view, Long fieldId, String fieldType) throws Exception {
        return chartDataManage.getFieldData(view, fieldId, fieldType);
    }

    @Override
    public List<String> getDrillFieldData(ChartViewDTO view, Long fieldId) throws Exception {
        return chartDataManage.getDrillFieldData(view, fieldId);
    }

    @DeLog(id = "#p0", ot = LogOT.EXPORT, st = LogST.PANEL)
    public void exportPanelViewLog(Long id) {
    }

    @DeLog(id = "#p0", ot = LogOT.EXPORT, st = LogST.SCREEN)
    public void exportScreenViewLog(Long id) {
    }

    @DeLinkPermit("#p0.dvId")
    @Override
    public void innerExportDetails(ChartExcelRequest request, HttpServletResponse response) throws Exception {
        shareVisitorPermissionManage.require(io.dataease.share.manage.ShareVisitorPermissionManage.EXPORT_DATA);
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String linkToken = httpServletRequest.getHeader(AuthConstant.LINK_TOKEN_KEY);
        LogUtil.info(request.getViewInfo().getId() + " " + StringUtils.isNotEmpty(linkToken) + " " + request.isDataEaseBi());
        boolean embeddedSyncExport = request.isDataEaseBi() && StringUtils.isEmpty(linkToken) && !StringUtils.equalsIgnoreCase(exportCenterManage.singleValue(XpackSettingConstants.EMBEDDED_EXPORT_MODE), "async");
        if ((StringUtils.isNotEmpty(linkToken) && !request.isDataEaseBi()) || embeddedSyncExport) {
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

                    boolean summaryEnabled = !"dataset".equals(request.getDownloadType()) && excelExportContentService.isSummaryEnabled(request.getViewInfo());
                    ExcelExportContentService.SummaryConfig summaryConfig = null;
                    ExcelExportContentService.SummaryAccumulator summaryAcc = null;
                    List<ChartViewFieldDTO> allExportColumns = null;
                    Map<String, BigDecimal> customSumResult = null;
                    if (summaryEnabled) {
                        summaryConfig = excelExportContentService.parseSummaryConfig(request.getViewInfo());
                        summaryAcc = new ExcelExportContentService.SummaryAccumulator();
                        allExportColumns = excelExportContentService.getAllExportColumns(request.getViewInfo());
                    }

                    request.getViewInfo().getChartExtRequest().setPageSize(Long.valueOf(extractPageSize));
                    ChartViewDTO chartViewDTO = findExcelData(request);
                    for (long i = 1; i < chartViewDTO.getTotalPage() + 1; i++) {
                        request.getViewInfo().getChartExtRequest().setGoPage(i);
                        ChartViewDTO pageDto = findExcelData(request);
                        details.addAll(request.getDetails());

                        if (summaryEnabled) {
                            excelExportContentService.accumulatePageStats(summaryAcc, request.getDetails(), allExportColumns, summaryConfig);
                            if (i == chartViewDTO.getTotalPage() && pageDto.getData() != null && pageDto.getData().get("customSumResult") != null) {
                                customSumResult = (Map<String, BigDecimal>) pageDto.getData().get("customSumResult");
                            }
                        }

                        if ((details.size() + extractPageSize) > sheetLimit || i == chartViewDTO.getTotalPage()) {
                            boolean hasSummaryRow = i == chartViewDTO.getTotalPage() && summaryEnabled && summaryAcc.totalCount > 0;
                            if (hasSummaryRow) {
                                Object[] totalRow = excelExportContentService.buildSummaryRow(allExportColumns, summaryConfig, summaryAcc, customSumResult);
                                details.add(totalRow);
                            }

                            detailsSheet = wb.createSheet("数据" + sheetIndex);
                            Integer[] excelTypes = request.getExcelTypes();
                            Object[] header = excelExportContentService.filterExportHeader(request.getHeader(), request.getViewInfo());
                            details.add(0, header);
                            List<Integer> columnIndexs = excelExportContentService.getHiddenExportColumnIndexes(header, request.getViewInfo());
                            ExportCenterDownLoadManage.removeColumn(details, columnIndexs);
                            ViewDetailField[] detailFields = request.getDetailFields();
                            excelExportContentService.setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes, null, request.getViewInfo(), wb, hasSummaryRow);
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
                            excelExportContentService.setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes, request.getViewInfo(), wb);
                        } else {
                            excelExportContentService.setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes, request.getViewInfo(), null);
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
                            excelExportContentService.setExcelData(detailsSheet, cellStyle, header, details, detailFields, excelTypes, request.getViewInfo(), null);
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

                try {
                    if (request.getBusiFlag().equalsIgnoreCase("dashboard")) {
                        CommonBeanFactory.proxy(this.getClass()).exportPanelViewLog(Long.parseLong(request.getViewId()));
                    } else {
                        CommonBeanFactory.proxy(this.getClass()).exportScreenViewLog(Long.parseLong(request.getViewId()));
                    }
                } catch (Exception e) {
                    LogUtil.error(e);
                }
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

}
