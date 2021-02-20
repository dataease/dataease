package io.dataease.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRoleHelpDTO {

    private String roleId;
    private String roleName;
    private String sourceId;
    private String sourceName;
    private String parentId;

}
