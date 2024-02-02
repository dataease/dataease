package io.dataease.api.dingtalk.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "钉钉设置构造器")
@Data
public class DingtalkSettingCreator implements Serializable {
    @Schema(description = "agentId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String agentId;
    @Schema(description = "appKey", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appKey;
    @Schema(description = "appSecret", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appSecret;
    @Schema(description = "回调域名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String callBack;
    @Schema(description = "是否可用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean enable;
    @Schema(description = "是否有效")
    private Boolean valid;
}
