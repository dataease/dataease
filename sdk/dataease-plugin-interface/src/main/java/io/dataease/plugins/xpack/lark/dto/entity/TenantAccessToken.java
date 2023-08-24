package io.dataease.plugins.xpack.lark.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class TenantAccessToken extends LarkBaseResult implements Serializable {

    private String tenant_access_token;

    private Long  expire;
}
