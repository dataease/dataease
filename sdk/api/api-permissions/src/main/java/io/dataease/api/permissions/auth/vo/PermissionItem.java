package io.dataease.api.permissions.auth.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "权限项")
@Data
public class PermissionItem implements Serializable {

    @Serial
    private static final long serialVersionUID = -6537851979745319692L;
    @JsonSerialize(using= ToStringSerializer.class)
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "权重")
    private int weight;
}
