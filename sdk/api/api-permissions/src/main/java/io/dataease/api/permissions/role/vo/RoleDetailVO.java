package io.dataease.api.permissions.role.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.api.permissions.role.dto.RoleCreator;
import lombok.Data;

@Data
public class RoleDetailVO extends RoleCreator {

    @JsonSerialize(using= ToStringSerializer.class)
    private Long id;
}
