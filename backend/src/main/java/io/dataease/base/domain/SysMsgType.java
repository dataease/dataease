package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysMsgType implements Serializable {
    private Long msgTypeId;

    private Long pid;

    private String typeName;

    private String router;

    private String callback;

    private static final long serialVersionUID = 1L;
}