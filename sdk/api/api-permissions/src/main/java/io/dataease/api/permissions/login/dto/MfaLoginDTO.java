package io.dataease.api.permissions.login.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "MFA登录DTO")
@Data
public class MfaLoginDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = -8218773323394184937L;
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "CODE")
    private String code;
    @Schema(description = "KEY")
    private String key;
}
