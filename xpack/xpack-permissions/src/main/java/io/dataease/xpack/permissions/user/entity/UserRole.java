package io.dataease.xpack.permissions.user.entity;

import lombok.Data;

@Data
public class UserRole extends RoleInfo {

    private String name;

    public boolean isRootAdmin() {
        return isRoot() && !isReadonly();
    }

    public boolean isRootReadonly() {
        return isRoot() && isReadonly();
    }
}

