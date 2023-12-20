package io.dataease.api.permissions.role.dto;

import io.dataease.model.KeywordRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;


@Schema(description = "用户过滤器")
@EqualsAndHashCode(callSuper = true)
@Data
public class UserRequest extends KeywordRequest  {

    @Serial
    private static final long serialVersionUID = -2740015284392981297L;
    @Schema(description = "角色ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long rid;
    @Schema(description = "排序规则")
    private String order;

}
