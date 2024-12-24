package io.dataease.api.webhook.vo;

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

    private String secret;

    private String contentType;

    private Boolean ssl;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long oid;

    private Long createTime;
}
