package io.dataease.api.lark.vo;



import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Schema(description = "飞书信息")
@Data
public class LarkInfoVO implements Serializable {

    @Schema(description = "appId")
    private String appId;
    @Schema(description = "appSecret")
    private String appSecret;
    @Schema(description = "回调域名")
    private String callBack;
    @Schema(description = "是否开启")
    private Boolean enable = false;
    @Schema(description = "是否可用")
    private Boolean valid = false;
}
