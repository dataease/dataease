package io.dataease.plugins.xpack.wecom.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseQrResult implements Serializable {

    private String appid;

    private String agentid;

    private String redirectUri;

    private String state;

    private String lang;
}
