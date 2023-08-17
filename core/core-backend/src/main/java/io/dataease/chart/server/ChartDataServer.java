package io.dataease.chart.server;

import io.dataease.api.chart.ChartDataApi;
import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.chart.manage.ChartDataManage;
import io.dataease.exception.DEException;
import io.dataease.result.ResultCode;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author Junjun
 */
@RestController
@RequestMapping("chartData")
public class ChartDataServer implements ChartDataApi {
    @Resource
    private ChartDataManage chartDataManage;

    @Override
    public ChartViewDTO getData(ChartViewDTO chartViewDTO) throws Exception {
        try {
            return chartDataManage.calcData(chartViewDTO);
        } catch (Exception e) {
            DEException.throwException(ResultCode.DATA_IS_WRONG.code(), e.getMessage());
        }
        return null;
    }

    @Override
    public List<String> getFieldData(ChartViewDTO view, Long fieldId, String fieldType) throws Exception {
        return chartDataManage.getFieldData(view, fieldId, fieldType);
    }
}
