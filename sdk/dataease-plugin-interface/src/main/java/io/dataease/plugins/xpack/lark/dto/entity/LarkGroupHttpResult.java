package io.dataease.plugins.xpack.lark.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class LarkGroupHttpResult extends LarkBaseResult implements Serializable {

    private LarkGroupData data;
}
