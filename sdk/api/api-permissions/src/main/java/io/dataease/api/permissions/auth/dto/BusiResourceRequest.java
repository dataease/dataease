package io.dataease.api.permissions.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "业务资源请求参数")
@Data
public class BusiResourceRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 6769377426446797907L;

    @Schema(description = "资源flag", requiredMode = Schema.RequiredMode.REQUIRED)
    private String flag;

    @Schema(description = "是否系统级查询")
    private boolean system;
}
