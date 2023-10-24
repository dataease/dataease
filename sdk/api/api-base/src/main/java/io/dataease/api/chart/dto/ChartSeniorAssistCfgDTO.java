package io.dataease.api.chart.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChartSeniorAssistCfgDTO {
    private boolean enable;
    private List<ChartSeniorAssistDTO> assistLine;
}
