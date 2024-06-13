package io.dataease.extensions.view.dto;

import lombok.Data;

import java.util.List;


@Data
public class ChartSeniorAssistCfgDTO {
    private boolean enable;
    private List<ChartSeniorAssistDTO> assistLine;
}
