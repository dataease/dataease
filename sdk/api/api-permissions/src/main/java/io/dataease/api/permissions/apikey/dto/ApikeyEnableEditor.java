package io.dataease.api.permissions.apikey.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "状态切换器")
@Data
public class ApikeyEnableEditor implements Serializable {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "状态", defaultValue = "false")
    private Boolean enable = false;
}
