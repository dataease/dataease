package io.dataease.api.system.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "在线地图构造器")
@Data
public class OnlineMapEditor implements Serializable {
    @Schema(description = "在线地图key", requiredMode = Schema.RequiredMode.REQUIRED)
    private String key;
}
