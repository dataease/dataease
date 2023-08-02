package io.dataease.api.permissions.user.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ModifyPwdRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -6583458043271002864L;

    private String pwd;

    private String newPwd;
}
