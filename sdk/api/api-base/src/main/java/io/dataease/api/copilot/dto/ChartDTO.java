package io.dataease.api.copilot.dto;

import lombok.Data;

/**
 * @Author Junjun
 */
@Data
public class ChartDTO {
    private String type;
    private AxisDTO axis;
    private String title;
}
