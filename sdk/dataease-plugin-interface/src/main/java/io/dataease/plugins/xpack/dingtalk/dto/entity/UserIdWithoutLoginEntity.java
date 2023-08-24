package io.dataease.plugins.xpack.dingtalk.dto.entity;

import lombok.Data;

@Data
public class UserIdWithoutLoginEntity {
    private String associated_unionid;
    private String unionid;
    private String device_id;
    private String sys_level;
    private String userid;
    private Boolean sys;
}
