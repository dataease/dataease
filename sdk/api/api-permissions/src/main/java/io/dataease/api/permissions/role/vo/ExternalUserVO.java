package io.dataease.api.permissions.role.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "组织外用户VO")
@Data
public class ExternalUserVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -5244308239452360019L;

    @Schema(description = "用户ID")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long uid;
    @Schema(description = "用户账号")
    private String account;
    @Schema(description = "用户名称")
    private String name;
    @Schema(description = "用户邮箱")
    private String email;
    @Schema(description = "用户电话")
    private String phone;
}
