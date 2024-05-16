package io.dataease.api.permissions.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.api.permissions.variable.dto.SysVariableValueItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Schema(description = "用户详情VO")
@Data
public class UserFormVO implements Serializable {

    @Schema(description = "ID")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    @Schema(description = "账号")
    private String account;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "角色ID集合")
    private List<String> roleIds;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "状态")
    private Boolean enable;

    @Schema(description = "电话前缀")
    private String phonePrefix;

    @Schema(description = "电话")
    private String phone;

    @Schema(description = "IP")
    private String ip;

    @Schema(description = "模式")
    private String model;

    @Schema(description = "系统变量")
    private List<SysVariableValueItem> variables;
}
