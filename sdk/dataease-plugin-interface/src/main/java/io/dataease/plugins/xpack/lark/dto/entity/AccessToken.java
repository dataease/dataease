package io.dataease.plugins.xpack.lark.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccessToken implements Serializable {

    private String access_token;

    private String token_type;

    private Long  expires_in;

    private String refresh_token;

    private String refresh_expires_in;

    private String error;

    private String error_description;
}
