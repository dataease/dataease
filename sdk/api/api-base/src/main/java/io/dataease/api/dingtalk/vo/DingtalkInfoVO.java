package io.dataease.api.dingtalk.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Schema(description = "钉钉信息")
@Data
public class DingtalkInfoVO implements Serializable {
    @Schema(description = "corpId")
    private String corpId;
    @Schema(description = "agentId")
    private String agentId;
    @Schema(description = "appKey")
    private String appKey;
    @Schema(description = "appSecret")
    private String appSecret;
    @Schema(description = "回调域名")
    private String callBack;
    @Schema(description = "是否开启")
    private Boolean enable = false;
    @Schema(description = "是否可用")
    private Boolean valid = false;

    @Schema(description = "机器人Code")
    private String robotCode;

    @Schema(description = "群列表")
    private List<DingtalkChatItem> chatList;

}
