package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class SysUser implements Serializable {
    private Long userId;

    private Long deptId;

    private String username;

    private String nickName;

    private String gender;

    private String phone;

    private String email;

    private String password;

    private Boolean isAdmin;

    private Long enabled;

    private String createBy;

    private String updateBy;

    private Long pwdResetTime;

    private Long createTime;

    private Long updateTime;

    private String language;

    private static final long serialVersionUID = 1L;
}