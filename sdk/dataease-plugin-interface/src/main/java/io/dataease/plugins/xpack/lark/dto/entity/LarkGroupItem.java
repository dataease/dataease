package io.dataease.plugins.xpack.lark.dto.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class LarkGroupItem implements Serializable {

    private String chat_id;

    private String name;
}
