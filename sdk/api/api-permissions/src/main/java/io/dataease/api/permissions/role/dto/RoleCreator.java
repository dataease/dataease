package io.dataease.api.permissions.role.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RoleCreator implements Serializable {
    @Serial
    private static final long serialVersionUID = -5311145649863484035L;

    private String name;
    private Integer typeCode;
    private String desc;


}
