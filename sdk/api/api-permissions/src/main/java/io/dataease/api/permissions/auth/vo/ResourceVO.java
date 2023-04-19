package io.dataease.api.permissions.auth.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ResourceVO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8523999682424087399L;
    @JsonSerialize(using= ToStringSerializer.class)
    @ApiModelProperty("资源ID")
    private Long id;
    @ApiModelProperty("资源名称")
    private String name;
    @ApiModelProperty("子节点")
    private List<ResourceVO> children;
}
