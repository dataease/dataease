package io.dataease.extensions.view.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ChartCalcDataResult {
    private Map<String, Object> data;
    private List<String[]> originData;
    private List<String[]> assistData;
    private List<ChartSeniorAssistDTO> dynamicAssistFields;
    private Map<String, Object> context;
    // TODO 数据源插件化之后换成整个请求对象
    private String querySql;
}
