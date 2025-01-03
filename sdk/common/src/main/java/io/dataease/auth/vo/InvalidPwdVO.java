package io.dataease.auth.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class InvalidPwdVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3684394012648654165L;

    @JsonSerialize(using= ToStringSerializer.class)
    private Long uid;

    private boolean invalid;

    private Long validityPeriod;
}
