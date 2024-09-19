package io.dataease.api.xpack.settings.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class XpackOauth2VO implements Serializable {
    @Serial
    private static final long serialVersionUID = 2395518228048236146L;

    private String clientId;

    private String clientSecret;

    private String authEndpoint;

    private String tokenEndpoint;

    private String userInfoEndpoint;

    private String logoutEndpoint;

    private String scope;

    private String mapping;

    private String redirectUri;
}
