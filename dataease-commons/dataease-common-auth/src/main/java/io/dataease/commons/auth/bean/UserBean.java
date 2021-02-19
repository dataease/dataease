package io.dataease.commons.auth.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBean {

    private String username;

    private String password;

    private String role;

    private String permission;
}
