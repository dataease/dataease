package io.dataease.api.permissions.auth.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class BusiResourceEditor implements Serializable {

    @Serial
    private static final long serialVersionUID = 5193320462388120821L;
    @ApiModelProperty("资源ID")
    private Long id;
    @ApiModelProperty("资源名称")
    private String name;
    @ApiModelProperty("类型")
    private String flag;
    @ApiModelProperty("额外标识")
    private int extraFlag;
}
