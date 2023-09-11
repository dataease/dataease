package io.dataease.plugins.xpack.wecom.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseResult implements Serializable {

    private Integer errcode;

    private String errmsg;
}
