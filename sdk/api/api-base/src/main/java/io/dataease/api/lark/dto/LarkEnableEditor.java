package io.dataease.api.lark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "开启状态切换器")
@Data
public class LarkEnableEditor implements Serializable {
    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean enable;
}
