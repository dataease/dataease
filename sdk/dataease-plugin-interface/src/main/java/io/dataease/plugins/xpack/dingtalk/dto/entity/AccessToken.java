package io.dataease.plugins.xpack.dingtalk.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccessToken implements Serializable {

    private String accessToken;

    private Integer expireIn;
}
