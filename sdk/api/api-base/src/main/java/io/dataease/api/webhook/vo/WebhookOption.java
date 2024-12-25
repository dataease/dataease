package io.dataease.api.webhook.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class WebhookOption implements Serializable {
    @Serial
    private static final long serialVersionUID = 5994555955483485729L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;
}
