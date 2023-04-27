package io.dataease.api.permissions.auth.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PermissionItem implements Serializable {

    @Serial
    private static final long serialVersionUID = -6537851979745319692L;
    @ApiModelProperty("资源ID")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    @ApiModelProperty("权重")
    private int weight;
}
