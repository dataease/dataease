package io.dataease.api.xpack.settings.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class XpackAuthenticationStatusVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3394065091528285702L;

    private String name;

    private boolean enable;
}
