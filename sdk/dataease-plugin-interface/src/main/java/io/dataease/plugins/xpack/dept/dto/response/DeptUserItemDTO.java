package io.dataease.plugins.xpack.dept.dto.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DeptUserItemDTO implements Serializable {

    @ApiModelProperty("用户ID")
    private Long userId;
    @ApiModelProperty("账号")
    private String username;
    @ApiModelProperty("姓名")
    private String nickName;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("组织")
    private String deptName;
    @ApiModelProperty("角色")
    private List<String> roleNames;
    @ApiModelProperty("其他组织已绑定")
    private Boolean otherBinded;
}
