package io.dataease.api.permissions.role.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RoleEditor implements Serializable {
    @Serial
    private static final long serialVersionUID = -4071819873019095722L;

    private Long id;
    private String name;
    private String desc;

}
