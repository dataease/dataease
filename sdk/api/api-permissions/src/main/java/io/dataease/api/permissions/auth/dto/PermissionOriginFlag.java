package io.dataease.api.permissions.auth.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionOriginFlag implements Serializable {

    @Schema(description = "是否根结点")
    private boolean root;
    @Schema(description = "角色类型编码: 0=普通用户,7=数据分析师,9=组织管理员")
    private Integer typeCode = 0;

    @JsonSerialize(using= ToStringSerializer.class)
    @Schema(description = "顶层目录ID")
    private Long topRootDirId;
}
