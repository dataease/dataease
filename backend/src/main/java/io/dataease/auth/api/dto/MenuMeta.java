package io.dataease.auth.api.dto;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class MenuMeta implements Serializable {
    @ApiModelProperty("标题")
    private String title;
    @ApiModelProperty("图标")
    private String icon;
}
