package io.dataease.api.lark.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LarkTokenRequest implements Serializable {

    private String code;

    private String state;
}
