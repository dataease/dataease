package io.dataease.plugins.common.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysUsersRolesKey implements Serializable {
    private Long userId;

    private Long roleId;

    private static final long serialVersionUID = 1L;
}