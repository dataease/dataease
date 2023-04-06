package io.dataease.api.permissions.role.dto;

import io.dataease.model.KeywordRequest;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserRequest extends KeywordRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = -6926472834709745689L;

    @ApiModelProperty("角色ID")
    private Long rId;

}
