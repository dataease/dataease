package io.dataease.api.permissions.org.dto;

import io.dataease.model.KeywordRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "组织成员查询请求")
@EqualsAndHashCode(callSuper = true)
@Data
public class SysOrgMemberRequest extends KeywordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "组织ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orgId;

    @Schema(description = "当前页码")
    private Long currentPage = 1L;

    @Schema(description = "每页大小")
    private Long pageSize = 10L;
}
