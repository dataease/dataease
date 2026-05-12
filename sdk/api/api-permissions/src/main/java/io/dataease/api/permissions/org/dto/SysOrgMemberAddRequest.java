package io.dataease.api.permissions.org.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema(description = "组织成员添加请求")
@Data
public class SysOrgMemberAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orgId;

    @Schema(description = "用户ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> userIds;

    @Schema(description = "角色ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> roleIds;
}
