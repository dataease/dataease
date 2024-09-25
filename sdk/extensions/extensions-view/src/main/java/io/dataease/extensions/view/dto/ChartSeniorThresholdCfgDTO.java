package io.dataease.extensions.view.dto;

import lombok.Data;

import java.util.List;


@Data
public class ChartSeniorThresholdCfgDTO {
    /**
     * 是否启用
     */
    private boolean enable;

    /**
     * 表格阈值
     */
    private List<TableThresholdDTO> tableThreshold;
}
