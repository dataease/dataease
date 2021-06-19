package io.dataease.dto.chart;

import io.dataease.base.domain.ChartViewWithBLOBs;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @Author gin
 * @Date 2021/3/1 4:19 下午
 */
@Setter
@Getter
public class ChartViewDTO extends ChartViewWithBLOBs {
    private Map<String, Object> data;

    private String privileges;

    private Boolean isLeaf;
    private String pid;
}
