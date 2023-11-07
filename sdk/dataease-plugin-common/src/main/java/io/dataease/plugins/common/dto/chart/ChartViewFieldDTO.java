package io.dataease.plugins.common.dto.chart;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author gin
 * @Date 2021/3/11 1:18 下午
 */
@Data
public class ChartViewFieldDTO extends ChartViewFieldBaseDTO implements Serializable {
    private List<ChartViewFieldFilterDTO> filter;

    private List<String> customSort;

    private String busiType;

    private boolean drill;
}
