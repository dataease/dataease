package io.dataease.api.permissions.user.domain;

import lombok.Data;

import java.util.List;

@Data
public class UserGridVO {

    private Long id;

    private String account;

    private String name;

    private List<UserGridRoleItem> roleItems;

    private List<UserGridOrgItem> orgItems;

    private String email;

    private Boolean enable;

    private Long createTime;
}
