package io.dataease.api.permissions.org.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Intermediate DTO for raw query rows in memberPage (one row = user + one role)
 */
@Data
public class SysOrgMemberRowDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String account;
    private String name;
    private String email;
    private Long roleId;
    private String roleName;
    private Boolean enable;
}
