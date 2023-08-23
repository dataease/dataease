package io.dataease.plugins.xpack.dingtalk.dto.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class DingQrResult implements Serializable {

    private String appid;

    private String state;

    private String redirectUri;
}
