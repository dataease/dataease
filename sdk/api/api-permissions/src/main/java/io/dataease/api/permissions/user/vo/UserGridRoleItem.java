package io.dataease.api.permissions.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "角色项")
@Data
public class UserGridRoleItem {

    @Schema(description = "角色ID")
    private Long id;
    @Schema(description = "角色名称")
    private String name;
}
