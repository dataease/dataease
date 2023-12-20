package io.dataease.api.permissions.org.dto;

import io.dataease.model.KeywordRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;


@Schema(description = "组织列表过滤器")
@EqualsAndHashCode(callSuper = true)
@Data
public class OrgRequest extends KeywordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1697526057837588192L;
    @Schema(description = "是否降序", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean desc = true;
}
