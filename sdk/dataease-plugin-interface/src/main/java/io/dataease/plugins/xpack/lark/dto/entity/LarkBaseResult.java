package io.dataease.plugins.xpack.lark.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class LarkBaseResult implements Serializable {

    private Integer code;

    private String msg;
}
