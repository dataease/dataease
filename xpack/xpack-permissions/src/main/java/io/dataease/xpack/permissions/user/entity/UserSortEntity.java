package io.dataease.xpack.permissions.user.entity;

import lombok.Data;

@Data
public class UserSortEntity {

    private String columnName;

    private boolean isAsc;
}
