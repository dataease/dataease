package io.dataease.api.permissions.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "权限查询条件")
@Data
public class BusiPermissionRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -2424587989223319563L;

    @Schema(description = "对象ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
    @Schema(description = "对象类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer type;
    @Schema(description = "资源类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String flag;
}
