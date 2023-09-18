package io.dataease.api.xpack.settings.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class XpackAuthenticationEditor implements Serializable {
    @Serial
    private static final long serialVersionUID = 8817503683420624977L;

    private Long id;

    private boolean enable;
}
