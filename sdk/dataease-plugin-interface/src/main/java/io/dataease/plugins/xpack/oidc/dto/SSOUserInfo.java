package io.dataease.plugins.xpack.oidc.dto;


import lombok.Data;

import java.io.Serializable;

@Data
public class SSOUserInfo implements Serializable {

    private String sub;

    private String username;

    private String nickName;

    private String email;
}
