package io.dataease.api.permissions.apikey.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

@Data
public class ApiKeyVO implements Serializable {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String accessKey;

    private String accessSecret;

    private Boolean enable;

    private Long createTime;
}
