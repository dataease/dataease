package io.dataease.visualization.dto;

import lombok.Data;

@Data
public class WatermarkContentDTO {

    private Boolean enable;

    private Boolean excelEnable = false;

    private Boolean enablePanelCustom;

    private String type;

    private String content;

    private String watermark_color;

    private Integer watermark_x_space;

    private Integer watermark_y_space;

    private Integer watermark_fontsize;
}
