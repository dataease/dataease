package io.dataease.api.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "地理信息构造器")
@Data
public class GeometryNodeCreator implements Serializable {
    @Schema(description = "区域代码")
    private String code;
    @Schema(description = "区域名称")
    private String name;
    @Schema(description = "上级区域代码")
    private String pid;
}
