package io.dataease.api.permissions.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "用户概要")
@Data
public class UserItem implements Serializable {
    @Serial
    private static final long serialVersionUID = -3423336650739339624L;

    @JsonSerialize(using= ToStringSerializer.class)
    @Schema(description = "用户ID")
    private Long id;
    @Schema(description = "用户名称")
    private String name;
}
