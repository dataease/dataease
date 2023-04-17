package io.dataease.api.permissions.role.dto;

import io.dataease.model.KeywordRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class UserRequest extends KeywordRequest  {

    @ApiModelProperty("角色ID")
    private Long rid;

}
