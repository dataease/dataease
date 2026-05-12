package io.dataease.api.permissions.org.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema(description = "组织成员VO")
@Data
public class SysOrgMemberVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "账号")
    private String account;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "角色列表")
    private List<RoleItem> roles;

    @Schema(description = "用户状态")
    private Boolean enable;

    @Data
    public static class RoleItem implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "角色ID")
        @JsonSerialize(using = ToStringSerializer.class)
        private Long roleId;

        @Schema(description = "角色名称")
        private String roleName;
    }
}
