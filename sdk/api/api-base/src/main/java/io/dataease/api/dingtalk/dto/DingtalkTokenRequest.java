package io.dataease.api.dingtalk.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DingtalkTokenRequest implements Serializable {

    private String code;

    private String state;
}
