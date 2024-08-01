package io.dataease.api.xpack.settings.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class XpackLdapVO implements Serializable {
    @Serial
    private static final long serialVersionUID = -2996803523472015035L;

    private String addr;

    private String dn;

    private String pwd;

    private String ou;

    private String filter;

    private String mapping;

    private boolean enable;

}
