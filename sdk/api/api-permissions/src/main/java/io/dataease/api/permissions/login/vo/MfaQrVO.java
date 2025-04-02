package io.dataease.api.permissions.login.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Schema(description = "MFA二维码信息")
@Data
public class MfaQrVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -3465640829593927730L;

    @Schema(description = "图片")
    private String img;

    @Schema(description = "KEY")
    private String key;
}
