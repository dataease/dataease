package io.dataease.api.xpack.share.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Schema(description = "切换器")
@Data
public class TicketSwitchRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 7670768142874123370L;
    @Schema(description = "资源ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String resourceId;
    @Schema(description = "是否必填", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean require = false;
}
