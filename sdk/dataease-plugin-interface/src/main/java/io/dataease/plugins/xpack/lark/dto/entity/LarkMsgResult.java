package io.dataease.plugins.xpack.lark.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class LarkMsgResult extends LarkBaseResult implements Serializable {

    private LarkMsgEntity data;
}
