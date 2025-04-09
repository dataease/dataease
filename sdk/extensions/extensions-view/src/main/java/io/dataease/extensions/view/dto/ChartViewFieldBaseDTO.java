package io.dataease.extensions.view.dto;

import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import lombok.Data;

import java.io.Serializable;

/**
 * 图表字段base类，与数据集字段表基本一致
 */
@Data
public class ChartViewFieldBaseDTO extends DatasetTableFieldDTO implements Serializable {

    private String summary;

    private String sort;

    /**
     * 日期解析格式，后端参与聚合运算
     */
    private String dateStyle;

    /**
     * 日期分隔符
     */
    private String datePattern;

    /**
     * 日期显示格式，仅前端图表格式化
     */
    private String dateShowFormat;

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
