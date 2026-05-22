package io.dataease.api.permissions.auth.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema(description = "关联权限")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionOrigin implements Serializable {
    @Serial
    private static final long serialVersionUID = 1455588932869130794L;

    @JsonSerialize(using= ToStringSerializer.class)
    @Schema(description = "关联ID")
    private Long id;
    @Schema(description = "关联名称")
    private String name;
    @Schema(description = "来源类型: 0=user, 1=role, 2=org")
    private Integer type;
    @Schema(description = "关联权限项")
    private List<PermissionItem> permissions;
}
