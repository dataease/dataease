package io.dataease.api.permissions.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.dataease.api.permissions.variable.dto.SysVariableValueItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema(description = "用户构造器")
@Data
public class UserCreator implements Serializable {

    @Serial
    private static final long serialVersionUID = 5231186463604221044L;
    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(description = "账号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String account;
    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
    @Schema(description = "电话前缀")
    private String phonePrefix;
    @Schema(description = "电话")
    private String phone;
    @Schema(description = "角色ID集合", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> roleIds;
    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean enable;
    @Schema(hidden = true)
    @JsonIgnore
    private Long uid;
    @Schema(description = "系统变量")
    private List<SysVariableValueItem> variables;
}
