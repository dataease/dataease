package io.dataease.api.permissions.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "角色编辑器")
@Data
public class RoleEditor implements Serializable {
    @Serial
    private static final long serialVersionUID = -4071819873019095722L;
    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(description = "名称", hidden = true)
    private String desc;

}
