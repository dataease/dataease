package io.dataease.api.permissions.auth.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.model.TreeResultModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BusiPerVO implements TreeResultModel<BusiPerVO>, Serializable {


    @Serial
    private static final long serialVersionUID = 8191619596741217494L;

    @ApiModelProperty("资源ID")
    @JsonSerialize(using= ToStringSerializer.class)
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
