package io.dataease.api.permissions.user.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class EnableSwitchRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 8477475476294666602L;

    private Long id;

    private Boolean enable;
}
