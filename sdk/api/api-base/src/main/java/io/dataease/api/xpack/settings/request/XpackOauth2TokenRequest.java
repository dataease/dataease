package io.dataease.api.xpack.settings.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class XpackOauth2TokenRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 489213446985742448L;

    private String code;

    private String state;
}
