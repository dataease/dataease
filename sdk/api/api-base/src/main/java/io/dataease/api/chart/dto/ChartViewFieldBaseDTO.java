package io.dataease.api.chart.dto;

import io.dataease.dto.dataset.DatasetTableFieldDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * 视图字段base类，与数据集字段表基本一致
 */
@Data
public class ChartViewFieldBaseDTO extends DatasetTableFieldDTO implements Serializable {

    private String summary;

    private String sort;

    private String dateStyle;

    private String datePattern;

    private Integer extField;

    private String chartType;

    private ChartFieldCompareDTO compareCalc;

    private String logic;

    private String filterType;

    private Long chartId;

    private Integer index;

    private FormatterCfgDTO formatterCfg;

    private String chartShowName;

}
