package io.dataease.plugins.xpack.dingtalk.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class UnionidEntity implements Serializable {

    private String nick;

    private String unionid;

    private String openid;

    private Boolean main_org_auth_high_level;
}
