package io.dataease.api.permissions.role.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UnmountUserRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1361046648092771178L;
    private Long rid;
    private Long uid;
}
