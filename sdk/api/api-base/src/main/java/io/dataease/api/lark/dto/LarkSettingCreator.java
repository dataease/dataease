package io.dataease.api.lark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "飞书设置构造器")
@Data
public class LarkSettingCreator implements Serializable {
    @Schema(description = "appId", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appId;
    @Schema(description = "appSecret", requiredMode = Schema.RequiredMode.REQUIRED)
    private String appSecret;
    @Schema(description = "回调域名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String callBack;
    @Schema(description = "是否可用", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean enable;
    @Schema(description = "是否有效")
    private Boolean valid;
}
