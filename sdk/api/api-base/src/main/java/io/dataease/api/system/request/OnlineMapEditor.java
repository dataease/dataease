package io.dataease.api.system.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "在线地图构造器")
@Data
public class OnlineMapEditor implements Serializable {
    @Schema(description = "在线地图类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mapType;
    @Schema(description = "在线地图key", requiredMode = Schema.RequiredMode.REQUIRED)
    private String key;
    @Schema(description = "在线地图安全密钥", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String securityCode;
}
