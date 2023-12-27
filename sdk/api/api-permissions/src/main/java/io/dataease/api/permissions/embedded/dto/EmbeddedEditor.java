package io.dataease.api.permissions.embedded.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "嵌入式应用编辑器")
@Data
public class EmbeddedEditor implements Serializable {
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "应用名称")
    private String name;
    @Schema(description = "应用域名")
    private String domain;
}
