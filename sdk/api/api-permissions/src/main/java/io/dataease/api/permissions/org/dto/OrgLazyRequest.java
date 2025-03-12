package io.dataease.api.permissions.org.dto;

import io.dataease.model.KeywordRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
@Schema(description = "组织列表过滤器")
@EqualsAndHashCode(callSuper = true)
@Data
public class OrgLazyRequest extends KeywordRequest implements Serializable {
    @Schema(description = "上级节点ID")
    private Long pid;
    @Schema(description = "是否降序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean desc = true;
}
