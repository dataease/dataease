package io.dataease.api.xpack.settings.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "状态切换器")
@Data
public class XpackAuthenticationEditor implements Serializable {
    @Serial
    private static final long serialVersionUID = 8817503683420624977L;

    @Schema(description = "ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean enable;
}
