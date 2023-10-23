package io.dataease.api.permissions.role.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MountExternalUserRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -1682136323964916544L;

    private Long rid;

    private Long uid;
}
