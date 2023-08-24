package io.dataease.plugins.common.entity;

import lombok.Data;

@Data
public class XpackLdapUserEntity {
    /* 
    private String userName;

    private String nickName;
    */

    private String username;

    private String nickname;
    
    private String email;

}
