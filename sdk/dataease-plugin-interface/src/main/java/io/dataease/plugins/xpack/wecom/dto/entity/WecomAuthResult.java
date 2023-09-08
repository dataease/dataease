package io.dataease.plugins.xpack.wecom.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class WecomAuthResult extends BaseResult implements Serializable {

    private String userId;

    private String openId;
}
