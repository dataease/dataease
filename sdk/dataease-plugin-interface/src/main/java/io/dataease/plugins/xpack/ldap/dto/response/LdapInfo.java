package io.dataease.plugins.xpack.ldap.dto.response;

import java.io.Serializable;

import lombok.Data;

@Data
public class LdapInfo implements Serializable{

    private String url;
    private String dn;
    private String password;
    private String ou;
    private String filter;
    private String mapping;
    private String open;
}
