package io.dataease.api.permissions.embedded.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "嵌入式密钥重置器")
@Data
public class EmbeddedResetRequest implements Serializable {

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "新密钥")
    private String appSecret;
}
