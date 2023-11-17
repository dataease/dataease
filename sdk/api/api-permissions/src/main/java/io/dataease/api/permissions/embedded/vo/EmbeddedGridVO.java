package io.dataease.api.permissions.embedded.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

@Data
public class EmbeddedGridVO implements Serializable {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String name;

    private String appId;

    private String appSecret;

    private String domain;
}
