package io.dataease.api.permissions.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "用户项VO")
@Data
public class UserItemVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -311077645822242697L;

    @Schema(description = "ID")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    @Schema(description = "账号")
    private String account;
    @Schema(description = "名称")
    private String name;
    @Schema(description = "邮箱")
    private String email;
}
