package io.dataease.api.chart.request;

import io.dataease.api.chart.dto.ChartViewDTO;
import io.dataease.api.chart.dto.ViewDetailField;
import lombok.Data;

import java.util.List;

/**
 * @Author wangjiahao
 */
@Data
public class ChartExcelRequest {

    private String viewId;

    private String viewName;

    private String[] header;

    private Integer[] excelTypes;

    private List<Object[]> details;

    private ViewDetailField[] detailFields;

    private ChartViewDTO viewInfo;

    private List<String> excelHeaderKeys;

}
