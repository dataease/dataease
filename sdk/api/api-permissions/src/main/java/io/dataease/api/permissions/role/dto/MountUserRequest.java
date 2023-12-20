package io.dataease.api.permissions.role.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Schema(description = "用户绑定器")
@Data
public class MountUserRequest implements Serializable {

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long rid;
    @Schema(description = "用户ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> uids;
}
