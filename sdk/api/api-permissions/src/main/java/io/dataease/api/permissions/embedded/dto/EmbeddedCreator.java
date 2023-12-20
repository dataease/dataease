package io.dataease.api.permissions.embedded.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "嵌入式应用构造器")
@Data
public class EmbeddedCreator implements Serializable {
    @Schema(description = "应用名称")
    private String name;
    @Schema(description = "应用域名")
    private String domain;
}
