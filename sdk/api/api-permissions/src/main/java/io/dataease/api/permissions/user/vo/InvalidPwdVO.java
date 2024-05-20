package io.dataease.api.permissions.user.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class InvalidPwdVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3684394012648654165L;

    private boolean invalid;

    private Long validityPeriod;
}
