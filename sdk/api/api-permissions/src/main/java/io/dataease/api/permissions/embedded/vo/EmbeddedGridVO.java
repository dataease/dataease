package io.dataease.api.permissions.embedded.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "嵌入式列表VO")
@Data
public class EmbeddedGridVO implements Serializable {

    @Schema(description = "ID")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    @Schema(description = "应用名称")
    private String name;
    @Schema(description = "应用ID")
    private String appId;
    @Schema(description = "应用密钥")
    private String appSecret;
    @Schema(description = "应用域名")
    private String domain;
}
