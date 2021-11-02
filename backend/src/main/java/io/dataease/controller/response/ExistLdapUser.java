package io.dataease.controller.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data

@ApiModel("已同步用户")
public class ExistLdapUser {

    @ApiModelProperty("账号")
    private String username;

}
