package io.dataease.api.permissions.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserItemVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -311077645822242697L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;

    private String account;

    private String name;

    private String email;
}
