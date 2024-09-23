package io.dataease.api.xpack.settings.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class XpackOauthTokenVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -3594367641594329352L;

    private String token;

    private String idToken;
}
