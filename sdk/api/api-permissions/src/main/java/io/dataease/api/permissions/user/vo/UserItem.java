package io.dataease.api.permissions.user.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserItem implements Serializable {
    @Serial
    private static final long serialVersionUID = -3423336650739339624L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    private String name;
}
