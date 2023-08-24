package io.dataease.plugins.xpack.larksuite.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccessTokenData implements Serializable {

    private String access_token;
    private String token_type;
    private Integer expires_in;
    private String name;
    private String en_name;
    private String open_id;
    private String union_id;
    private String email;
    private String enterprise_email;
    private String user_id;
    private String mobile;
    private String tenant_key;
    private Integer refresh_expires_in;
    private String refresh_token;

}
