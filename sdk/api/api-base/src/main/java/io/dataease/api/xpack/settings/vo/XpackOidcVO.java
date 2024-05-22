package io.dataease.api.xpack.settings.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class XpackOidcVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3386314375628396307L;

    private String discovery;

    private String clientId;

    private String clientSecret;

    private String realm;

    private String scope;

    private boolean usePkce;

    private String redirectUri;
}
