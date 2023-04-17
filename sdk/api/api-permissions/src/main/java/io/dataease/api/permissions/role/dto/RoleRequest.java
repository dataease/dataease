package io.dataease.api.permissions.role.dto;

import io.dataease.model.KeywordRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RoleRequest extends KeywordRequest {

    @ApiModelProperty("用户ID")
    private Long uid;
}
