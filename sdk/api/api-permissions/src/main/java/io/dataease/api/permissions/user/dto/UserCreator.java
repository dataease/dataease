package io.dataease.api.permissions.user.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class UserCreator implements Serializable {

    @Serial
    private static final long serialVersionUID = 5231186463604221044L;

    private String name;
    private String account;
    private String email;
    private String phonePrefix;
    private String phone;
    private List<Long> roleIds;
    private Boolean enable;
}
