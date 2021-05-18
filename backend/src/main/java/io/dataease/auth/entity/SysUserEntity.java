package io.dataease.auth.entity;


import lombok.Data;
import java.io.Serializable;

@Data

public class SysUserEntity implements Serializable {

    private Long userId;

    private String username;

    private String nickName;

    private Long deptId;

    private String password;

    private Integer enabled;

    private String email;

    private String phone;

    private Boolean isAdmin;
}
