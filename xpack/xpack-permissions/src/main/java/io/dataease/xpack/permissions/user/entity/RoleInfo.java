package io.dataease.xpack.permissions.user.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RoleInfo implements Serializable {
    @Serial
    private static final long serialVersionUID = -2439433928710678018L;

    private Long id;

    private boolean root;

    private boolean readonly;

}
