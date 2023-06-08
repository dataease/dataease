package io.dataease.api.permissions.role.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ExternalUserVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -5244308239452360019L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long uid;

    private String account;

    private String name;

    private String email;

    private String phone;
}
