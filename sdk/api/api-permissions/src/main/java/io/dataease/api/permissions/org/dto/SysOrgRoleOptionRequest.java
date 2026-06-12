package io.dataease.api.permissions.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "组织角色选项请求")
@Data
public class SysOrgRoleOptionRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orgId;
}
