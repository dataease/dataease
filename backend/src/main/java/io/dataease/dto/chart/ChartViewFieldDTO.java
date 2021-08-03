package io.dataease.dto.chart;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/3/11 1:18 下午
 */
@Data
public class ChartViewFieldDTO implements Serializable {
    private String id;

    private String tableId;

    private String originName;

    private String dataeaseName;

    private String name;

    private String type;

    private Boolean checked;

    private Integer columnIndex;

    private Long lastSyncTime;

    private Integer deType;

    private String summary;

    private String sort;

    private List<ChartViewFieldFilterDTO> filter;

    private Integer deExtractType;

    private String dateStyle;

    private String datePattern;

    private Integer extField;
}
