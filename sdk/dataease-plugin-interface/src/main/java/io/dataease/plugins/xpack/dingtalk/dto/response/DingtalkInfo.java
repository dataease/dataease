package io.dataease.plugins.xpack.dingtalk.dto.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class DingtalkInfo implements Serializable {

    private String corpid;

    private String agentid;

    private String appKey;

    private String appSecret;

    private String open;

    private String redirectUri;
}
