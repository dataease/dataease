package io.dataease.dto;

import io.dataease.base.domain.Role;
import io.dataease.base.domain.User;
import io.dataease.base.domain.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UserDTO extends User {

    private List<Role> roles = new ArrayList<>();

    private List<UserRole> userRoles = new ArrayList<>();

    private static final long serialVersionUID = 1L;

}
