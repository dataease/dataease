package io.dataease.controller.request.chart;

import io.dataease.base.domain.DatasetTableField;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @Author gin
 * @Date 2021/4/19 10:24 上午
 */
@Getter
@Setter
public class ChartExtFilterRequest {
    private String componentId;
    private String fieldId;
    private String operator;
    private List<String> value;
    private List<String> viewIds;
    private DatasetTableField datasetTableField;
}
