package io.dataease.plugins.xpack.oidc.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SSOToken implements Serializable {

    private String accessToken;
    private String idToken;
}
