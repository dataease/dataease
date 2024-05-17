package io.dataease.api.lark.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class LarkGroupItem implements Serializable {
    @Serial
    private static final long serialVersionUID = -3458959523154279946L;

    private String chat_id;
    private String name;
}
