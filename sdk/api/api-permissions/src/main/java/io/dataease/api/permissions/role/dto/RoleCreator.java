package io.dataease.api.permissions.role.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "角色构造器")
@Data
public class RoleCreator implements Serializable {
    @Serial
    private static final long serialVersionUID = -5311145649863484035L;
    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;
    @Schema(description = "类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer typeCode;
    @Schema(description = "描述", hidden = true)
    private String desc;
    @JsonIgnore
    @Schema(hidden = true)
    private Long rid;


}
