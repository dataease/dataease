package io.dataease.api.permissions.user.vo;

import lombok.Data;

import java.util.List;

@Data
public class UserGridVO {

    private Long id;

    private String account;

    private String name;

    private List<UserGridRoleItem> roleItems;

    private String email;

    private Boolean enable;

    private Long createTime;
}
