package io.dataease.api.lark.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LarkSettingCreator implements Serializable {

    private String appId;

    private String appSecret;

    private String callBack;

    private Boolean enable;
}
