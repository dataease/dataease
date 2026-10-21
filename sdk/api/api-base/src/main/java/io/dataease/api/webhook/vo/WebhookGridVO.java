package io.dataease.api.webhook.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class WebhookGridVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 2846767076219865522L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;

    private String url;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String secret;

    private Boolean hasSecret;

    private String contentType;

    private Boolean ssl;
}
