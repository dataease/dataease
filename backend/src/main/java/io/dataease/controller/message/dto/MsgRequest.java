package io.dataease.controller.message.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MsgRequest implements Serializable {

    private Integer type;

    private Boolean status;
}
