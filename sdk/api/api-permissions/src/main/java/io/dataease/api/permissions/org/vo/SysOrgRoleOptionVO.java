package io.dataease.api.permissions.org.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Schema(description = "组织角色选项VO")
@Data
public class SysOrgRoleOptionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "角色ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @Schema(description = "角色名称")
    private String name;
}
