package io.dataease.plugins.xpack.dingtalk.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserIdEntity implements Serializable {

    private String contact_type;

    private String userid;
}
