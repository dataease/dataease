package io.dataease.api.permissions.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "密码修改器")
@Data
public class ModifyPwdRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -6583458043271002864L;

    @Schema(description = "原始密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String pwd;
    @Schema(description = "新密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPwd;
}
