package io.dataease.extensions.view.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 维度、指标、过滤器
 */
@Data
public class ChartViewFieldDTO extends ChartViewFieldBaseDTO implements Serializable {
    /**
     * 过滤
     */
    private List<ChartViewFieldFilterDTO> filter;

    /**
     * 排序
     */
    private List<String> customSort;

    private String busiType;

    private boolean isAgg;

    /**
     * 字段来源
     */
    @JsonIgnore
    private FieldSource source;
}
