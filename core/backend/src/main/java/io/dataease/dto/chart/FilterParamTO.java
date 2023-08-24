package io.dataease.dto.chart;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FilterParamTO {

    private Map<String, Object> component;

    private List<String> value;

    private String operator;

    private Boolean isTree = false;
}
