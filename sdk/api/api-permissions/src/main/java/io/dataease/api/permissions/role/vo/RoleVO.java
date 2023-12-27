package io.dataease.api.permissions.role.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "角色VO")
@Data
public class RoleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3488550489306534641L;
    @JsonSerialize(using= ToStringSerializer.class)
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "角色名称")
    private String name;
    @Schema(description = "只读")
    private boolean readonly;
    @Schema(description = "根结点")
    private boolean root;
}
