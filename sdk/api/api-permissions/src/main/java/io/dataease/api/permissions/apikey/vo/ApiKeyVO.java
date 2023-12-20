package io.dataease.api.permissions.apikey.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "API Key VO")
@Data
public class ApiKeyVO implements Serializable {

    @Schema(description = "ID")
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    @Schema(description = "accessKey")
    private String accessKey;

    @Schema(description = "accessSecret")
    private String accessSecret;

    @Schema(description = "状态")
    private Boolean enable;

    @Schema(description = "创建时间")
    private Long createTime;
}
