package io.dataease.api.permissions.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Schema(description = "资源权限构造器")
@Data
public class BusiTargetPerCreator extends MenuTargetPerCreator{
    @Schema(description = "类型")
    private Integer type;
    @Schema(description = "标记")
    private String flag;
}
