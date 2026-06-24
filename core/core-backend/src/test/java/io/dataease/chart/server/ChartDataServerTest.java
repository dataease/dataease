package io.dataease.chart.server;

import io.dataease.api.chart.request.ChartExcelRequest;
import io.dataease.chart.constant.ChartConstants;
import io.dataease.chart.manage.ChartDataManage;
import io.dataease.constant.DeTypeConstants;
import io.dataease.exportCenter.manage.ExportCenterLimitManage;
import io.dataease.exportCenter.util.ExportCenterUtils;
import io.dataease.extensions.view.dto.ChartViewFieldDTO;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.extensions.view.dto.FormatterCfgDTO;
import org.junit.Test;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    public void findExcelDataStoresCalculatedFieldsOnViewInfo() throws Exception {
        ChartDataServer chartDataServer = chartDataServerWithExportLimits(1_000_000L, 1_000_000L);

        ChartViewDTO view = new ChartViewDTO();
        view.setResultMode(ChartConstants.VIEW_RESULT_MODE.ALL);

        ChartExcelRequest request = new ChartExcelRequest();
        request.setDownloadType("view");
        request.setViewInfo(view);

        chartDataServer.findExcelData(request);

        assertNotNull(view.getData());
        assertEquals(1, ((List<?>) view.getData().get("fields")).size());
    }

    @Test
    public void setExcelDataUsesCurrentDrillFieldOrderForNumericFormats() {
        ChartViewFieldDTO province = field("province", "省份", DeTypeConstants.DE_STRING);
        ChartViewFieldDTO city = field("city", "城市", DeTypeConstants.DE_STRING);
        ChartViewFieldDTO amount = field("amount", "销售额", DeTypeConstants.DE_FLOAT);
        amount.setFormatterCfg(new FormatterCfgDTO().setType("value").setDecimalCount(2));

        ChartViewDTO view = new ChartViewDTO();
        view.setType("table-info");
        view.setXAxis(new ArrayList<>(Arrays.asList(province, amount)));
        view.setDrillFields(new ArrayList<>(List.of(city)));
        Map<String, Object> customAttr = new HashMap<>();
        customAttr.put("tableHeader", new HashMap<String, Object>());
        customAttr.put("tableCell", new HashMap<>(Map.of("mergeCells", false)));
        view.setCustomAttr(customAttr);
        Map<String, Object> data = new HashMap<>();
        data.put("fields", new ArrayList<>(Arrays.asList(province, city, amount)));
        view.setData(data);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("data");
            CellStyle headStyle = workbook.createCellStyle();
            List<Object[]> details = new ArrayList<>();
            details.add(new Object[]{"省份", "城市", "销售额"});
            details.add(new Object[]{"浙江", "杭州", "1234.5"});

            ChartDataServer.setExcelData(
                    sheet,
                    headStyle,
                    new Object[]{"省份", "城市", "销售额"},
                    details,
                    null,
                    new Integer[]{DeTypeConstants.DE_STRING, DeTypeConstants.DE_STRING, DeTypeConstants.DE_FLOAT},
                    view,
                    workbook
            );

            assertEquals("General", sheet.getRow(1).getCell(1).getCellStyle().getDataFormatString());
            assertEquals("0.00", sheet.getRow(1).getCell(2).getCellStyle().getDataFormatString());
            assertEquals(1234.5D, sheet.getRow(1).getCell(2).getNumericCellValue(), 0.0001D);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private ChartDataServer chartDataServerWithExportLimits(Long viewLimit, Long datasetLimit) throws Exception {
        ChartDataManage chartDataManage = mock(ChartDataManage.class);
        when(chartDataManage.calcData(any(ChartViewDTO.class))).thenAnswer(invocation -> {
            ChartViewDTO result = new ChartViewDTO();
            Map<String, Object> data = new HashMap<>();
            data.put("sourceData", new ArrayList<Object[]>());
            data.put("fields", List.of(field("name", "名称", DeTypeConstants.DE_STRING)));
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

    private static ChartViewFieldDTO field(String dataeaseName, String name, Integer deType) {
        ChartViewFieldDTO field = new ChartViewFieldDTO();
        field.setDataeaseName(dataeaseName);
        field.setName(name);
        field.setDeType(deType);
        return field;
    }
}
