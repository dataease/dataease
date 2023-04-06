package io.dataease.api.permissions.role.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RoleCreator implements Serializable {
    @Serial
    private static final long serialVersionUID = -5311145649863484035L;

    @ApiModelProperty("名称")
    private String name;
    @ApiModelProperty("类型0普通1组织")
    private Integer typeCode;
    @ApiModelProperty("描述")
    private String desc;


}
