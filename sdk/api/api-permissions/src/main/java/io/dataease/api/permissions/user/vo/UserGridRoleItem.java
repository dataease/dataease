package io.dataease.api.permissions.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "角色项")
@Data
public class UserGridRoleItem {

    @JsonSerialize(using= ToStringSerializer.class)
    @Schema(description = "角色ID")
    private Long id;
    @Schema(description = "角色名称")
    private String name;
}
