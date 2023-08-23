package io.dataease.plugins.xpack.wecom.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccessToken extends BaseResult implements Serializable {

    private String access_token;

    private Long expires_in;
}
