package io.dataease.api.permissions.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class AuthRequest implements Serializable {

    private Long userId;

    private Long orgId;

    private Integer resourceTypeId;
}
