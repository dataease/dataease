package io.dataease.plugins.xpack.wecom.dto.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class WecomInfo implements Serializable {

    private String corpid;

    private String agentid;

    private String secret;

    private String open;

    private String redirectUri;
}
