package io.dataease.plugins.xpack.lark.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class AppAccessToken extends LarkBaseResult implements Serializable {

    private String app_access_token;

    private Long expire;
}
