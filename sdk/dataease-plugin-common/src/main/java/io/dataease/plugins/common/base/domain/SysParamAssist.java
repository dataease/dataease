package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysParamAssist implements Serializable {
    private Long id;

    private byte[] content;

    private static final long serialVersionUID = 1L;
}