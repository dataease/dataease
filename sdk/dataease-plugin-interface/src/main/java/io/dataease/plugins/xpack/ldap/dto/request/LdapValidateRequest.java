package io.dataease.plugins.xpack.ldap.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class LdapValidateRequest implements Serializable {

    private String userName;

    private String password;

    public LdapValidateRequest() {
    }

    public LdapValidateRequest(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
}
