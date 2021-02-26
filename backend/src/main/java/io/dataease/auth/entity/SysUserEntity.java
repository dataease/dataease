package io.dataease.auth.entity;


import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
}
