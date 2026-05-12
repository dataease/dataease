package io.dataease.api.permissions.role.dto;

import io.dataease.model.KeywordRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

@Schema(description = "角色过滤器")
@EqualsAndHashCode(callSuper = true)
@Data
public class RoleRequest extends KeywordRequest {


    @Serial
    private static final long serialVersionUID = 7354856549096378406L;
    @Schema(description = "用户ID")
    private Long uid;
    @Schema(description = "组织ID")
    private Long oid;
    @Schema(description = "组织ID列表(支持多选)")
    private List<Long> oidList;
}
