package io.dataease.map.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class MapNodeReadResponse implements Serializable {

    @ApiModelProperty("区域代码")
    private String code;
    @ApiModelProperty("区域名称")
    private String name;
    @ApiModelProperty("区域级别")
    private Integer level;
    @ApiModelProperty("上级区域")
    private MapNodeReadResponse parent;
    @ApiModelProperty("geoGson")
    private String json;
}
