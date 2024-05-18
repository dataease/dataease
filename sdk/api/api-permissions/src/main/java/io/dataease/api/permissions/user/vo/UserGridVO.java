package io.dataease.api.permissions.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户列表VO")
@Data
public class UserGridVO {

    @Schema(description = "ID")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    @Schema(description = "账号")
    private String account;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "角色")
    private List<UserGridRoleItem> roleItems;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "电话")
    private Boolean enable;
    @Schema(description = "创建时间")
    private Long createTime;
    @Schema(description = "系统变量")
    private String sysVariable;
    @Schema(description = "用户来源")
    private Integer origin;
}
