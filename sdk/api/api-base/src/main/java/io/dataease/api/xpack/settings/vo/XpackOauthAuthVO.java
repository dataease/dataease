package io.dataease.api.xpack.settings.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class XpackOauthAuthVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -3658093847024323465L;

    private String state;

    private String clientId;

    private String redirectUri;

    private String authEndpoint;

    private String scope;
}
