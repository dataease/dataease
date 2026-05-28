package io.dataease.api.permissions.auth.dto;

import io.dataease.api.permissions.auth.vo.BasePermissionItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "资源权限构造器")
@Data
public class ResourcePermissionEditor {

    @Schema(description = "资源集合")
    private List<ResourceItemDTO> resourceList;
    @Schema(description = "主体类型")
    private Integer type;
    @Schema(description = "资源类型标记")
    private String flag;
    @Schema(description = "权限集合")
    private List<BasePermissionItem> permissions;
}
