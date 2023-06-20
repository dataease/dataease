package io.dataease.api.permissions.role.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RoleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3488550489306534641L;
    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
    private String name;
    private boolean readonly;
    private boolean root;
}
