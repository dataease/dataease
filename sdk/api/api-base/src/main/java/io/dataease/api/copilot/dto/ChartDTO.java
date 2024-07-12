package io.dataease.api.copilot.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author Junjun
 */
@Data
public class ChartDTO {
    private String type;
    private String title;
    private AxisDTO axis;
    private AxisFieldDTO column;
    private List<AxisFieldDTO> columns;
}
