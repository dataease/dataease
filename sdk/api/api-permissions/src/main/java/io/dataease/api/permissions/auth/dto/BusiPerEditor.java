package io.dataease.api.permissions.auth.dto;

import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
@EqualsAndHashCode(callSuper = true)
@Schema(description = "业务权限编辑器")
@Data
public class BusiPerEditor extends BusiPermissionRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 3067994331757489447L;
    @Schema(description = "编辑权限节点集合")
    private List<PermissionItem> permissions;
}
