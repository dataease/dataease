package io.dataease.chart.server;

import io.dataease.api.chart.request.ChartExcelRequest;
import io.dataease.chart.constant.ChartConstants;
import io.dataease.chart.manage.ChartDataManage;
import io.dataease.constant.DeTypeConstants;
import io.dataease.exportCenter.manage.ExportCenterLimitManage;
import io.dataease.exportCenter.util.ExportCenterUtils;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChartDataServerTest {

    @Test
    public void findExcelDataUsesConfiguredViewExportLimitWhenViewUsesCustomResultCount() throws Exception {
        ChartDataServer chartDataServer = chartDataServerWithExportLimits(1_000_000L, 1_000_000L);

        ChartViewDTO view = new ChartViewDTO();
        view.setResultMode(ChartConstants.VIEW_RESULT_MODE.CUSTOM);
        view.setResultCount(100_000);

        ChartExcelRequest request = new ChartExcelRequest();
        request.setDownloadType("view");
        request.setViewInfo(view);

        chartDataServer.findExcelData(request);

        assertEquals(Integer.valueOf(1_000_000), view.getResultCount());
    }

    @Test
    public void findExcelDataUsesViewExportLimitForViewDownloads() throws Exception {
        ChartDataServer chartDataServer = chartDataServerWithExportLimits(1_000_000L, 100_000L);

        ChartViewDTO view = new ChartViewDTO();
        view.setResultMode(ChartConstants.VIEW_RESULT_MODE.ALL);
        view.setResultCount(100_000);

        ChartExcelRequest request = new ChartExcelRequest();
        request.setDownloadType("view");
        request.setViewInfo(view);

        chartDataServer.findExcelData(request);

        assertEquals(Integer.valueOf(1_000_000), view.getResultCount());
    }

    @Test
    public void excelExportLimitUsesLowerLimitForDatasetDownloads() throws Exception {
        ChartDataServer chartDataServer = chartDataServerWithExportLimits(1_000_000L, 100_000L);

        int resultCount = ReflectionTestUtils.invokeMethod(chartDataServer, "getExcelExportLimit", "dataset");

        assertEquals(100_000, resultCount);
    }

    @Test
    public void setExcelDataKeepsTextColumnAfterHiddenNumericColumn() throws Exception {
        ChartViewDTO view = tableView("table-info", Arrays.asList(
                field("dim_a", "维度A", DeTypeConstants.DE_STRING, false),
                field("metric_b", "指标B", DeTypeConstants.DE_FLOAT, true),
                field("dim_c", "维度C", DeTypeConstants.DE_STRING, false)
        ));

        List<Object[]> details = new ArrayList<>();
        details.add(new Object[]{"维度A", "维度C"});
        details.add(new Object[]{"a1", "c1"});
        Integer[] excelTypes = new Integer[]{
                DeTypeConstants.DE_STRING,
                DeTypeConstants.DE_FLOAT,
                DeTypeConstants.DE_STRING
        };

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("data");

            ChartDataServer.setExcelData(
                    sheet,
                    wb.createCellStyle(),
                    details.get(0),
                    details,
                    null,
                    excelTypes,
                    view,
                    wb
            );

            Row dataRow = sheet.getRow(1);
            assertEquals("c1", dataRow.getCell(1).getStringCellValue());
        }
    }

    @Test
    public void setExcelDataKeepsNumericTypeAfterHiddenTextColumnInNormalTable() throws Exception {
        ChartViewDTO view = tableNormalView(
                Arrays.asList(
                        field("dim_a", "维度A", DeTypeConstants.DE_STRING, false),
                        field("dim_b", "维度B", DeTypeConstants.DE_STRING, true)
                ),
                List.of(field("metric_c", "指标C", DeTypeConstants.DE_FLOAT, false))
        );

        List<Object[]> details = new ArrayList<>();
        details.add(new Object[]{"维度A", "指标C"});
        details.add(new Object[]{"a1", "12.5"});
        Integer[] excelTypes = new Integer[]{
                DeTypeConstants.DE_STRING,
                DeTypeConstants.DE_STRING,
                DeTypeConstants.DE_FLOAT
        };

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("data");

            ChartDataServer.setExcelData(
                    sheet,
                    wb.createCellStyle(),
                    details.get(0),
                    details,
                    null,
                    excelTypes,
                    view,
                    wb
            );

            Row dataRow = sheet.getRow(1);
            assertEquals(CellType.NUMERIC, dataRow.getCell(1).getCellType());
            assertEquals(12.5, dataRow.getCell(1).getNumericCellValue(), 0.000001);
        }
    }

    private ChartDataServer chartDataServerWithExportLimits(Long viewLimit, Long datasetLimit) throws Exception {
        ChartDataManage chartDataManage = mock(ChartDataManage.class);
        when(chartDataManage.calcData(any(ChartViewDTO.class))).thenAnswer(invocation -> {
            ChartViewDTO result = new ChartViewDTO();
            Map<String, Object> data = new HashMap<>();
            data.put("sourceData", new ArrayList<Object[]>());
            result.setData(data);
            return result;
        });

        ExportCenterLimitManage limitManage = new ExportCenterLimitManage();
        ReflectionTestUtils.setField(limitManage, "viewLimit", viewLimit);
        ReflectionTestUtils.setField(limitManage, "datasetLimit", datasetLimit);
        new ExportCenterUtils().setExportCenterLimitManage(limitManage);

        ChartDataServer chartDataServer = new ChartDataServer();
        ReflectionTestUtils.setField(chartDataServer, "chartDataManage", chartDataManage);
        return chartDataServer;
    }

    private ChartViewDTO tableView(String type, List<ChartViewFieldDTO> xAxis) {
        ChartViewDTO view = new ChartViewDTO();
        view.setType(type);
        view.setXAxis(xAxis);
        view.setYAxis(new ArrayList<>());
        view.setXAxisExt(new ArrayList<>());
        view.setYAxisExt(new ArrayList<>());
        view.setExtStack(new ArrayList<>());
        view.setDrillFields(new ArrayList<>());
        view.setCustomAttr(tableCustomAttr());
        return view;
    }

    private ChartViewDTO tableNormalView(List<ChartViewFieldDTO> xAxis, List<ChartViewFieldDTO> yAxis) {
        ChartViewDTO view = tableView("table-normal", xAxis);
        view.setYAxis(yAxis);
        return view;
    }

    private Map<String, Object> tableCustomAttr() {
        Map<String, Object> customAttr = new HashMap<>();
        Map<String, Object> tableHeader = new HashMap<>();
        tableHeader.put("headerGroup", false);
        Map<String, Object> tableCell = new HashMap<>();
        tableCell.put("mergeCells", false);
        customAttr.put("tableHeader", tableHeader);
        customAttr.put("tableCell", tableCell);
        return customAttr;
    }

    private ChartViewFieldDTO field(String dataeaseName, String name, Integer deType, boolean hide) {
        ChartViewFieldDTO field = new ChartViewFieldDTO();
        field.setDataeaseName(dataeaseName);
        field.setName(name);
        field.setChartShowName(name);
        field.setDeType(deType);
        field.setHide(hide);
        return field;
    }
}
