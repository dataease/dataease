package io.dataease.api.webhook.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class WebhookSwitchRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -2194668885324981185L;

    private Long id;

    private boolean ssl;
}
