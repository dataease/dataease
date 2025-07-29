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
    private List<String[]> assistDataOriginList;
    private List<ChartSeniorAssistDTO> dynamicAssistFieldsOriginList;
    private Map<String, Object> context;
    private String querySql;
}
