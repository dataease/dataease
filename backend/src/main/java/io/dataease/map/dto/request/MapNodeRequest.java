package io.dataease.map.dto.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class MapNodeRequest implements Serializable {
    @ApiModelProperty("区域代码")
    private String code;
    @ApiModelProperty("区域名称")
    private String name;
    @ApiModelProperty("上级代码")
    private String pcode;
    @ApiModelProperty("上级级别")
    private Integer plevel;


}
