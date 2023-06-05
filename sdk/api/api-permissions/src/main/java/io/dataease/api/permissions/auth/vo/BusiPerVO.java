package io.dataease.api.permissions.auth.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BusiPerVO implements Serializable {


    @Serial
    private static final long serialVersionUID = 8191619596741217494L;

    @ApiModelProperty("资源ID")
    private Long id;
    @ApiModelProperty("资源名称")
    private String name;
    @ApiModelProperty("是否叶子结点")
    private Boolean leaf;
    @ApiModelProperty("权重")
    private Integer weight;
    @ApiModelProperty("子节点")
    private List<BusiPerVO> children;
}
