package io.dataease.api.permissions.auth.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema(description = "关联权限")
@Data
public class PermissionOrigin implements Serializable {
    @Serial
    private static final long serialVersionUID = 1455588932869130794L;

    @JsonSerialize(using= ToStringSerializer.class)
    @Schema(description = "关联ID")
    private Long id;
    @Schema(description = "关联名称")
    private String name;
    @Schema(description = "关联权限项")
    private List<PermissionItem> permissions;
}
