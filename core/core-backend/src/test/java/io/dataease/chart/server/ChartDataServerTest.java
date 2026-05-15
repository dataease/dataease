package io.dataease.chart.server;

import io.dataease.api.chart.request.ChartExcelRequest;
import io.dataease.chart.constant.ChartConstants;
import io.dataease.chart.manage.ChartDataManage;
import io.dataease.exportCenter.manage.ExportCenterLimitManage;
import io.dataease.exportCenter.util.ExportCenterUtils;
import io.dataease.extensions.view.dto.ChartViewDTO;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
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
}
