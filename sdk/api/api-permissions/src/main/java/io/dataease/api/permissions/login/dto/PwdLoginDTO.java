package io.dataease.api.permissions.login.dto;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Schema(description = "登录DTO")
@Data
public class PwdLoginDTO {

    @Schema(description = "账号(需加密)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "login.validator.name")
    private String name;
    @Schema(description = "密码(需加密)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "login.validator.pwd")
    private String pwd;
    @Hidden
    private Integer origin = 0;

}
