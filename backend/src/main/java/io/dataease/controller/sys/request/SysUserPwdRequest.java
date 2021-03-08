package io.dataease.controller.sys.request;


import lombok.Data;

import java.io.Serializable;

@Data
public class SysUserPwdRequest implements Serializable {

    private Long userId;
    private String password;
    private String repeatPassword;
    private String newPassword;


}
