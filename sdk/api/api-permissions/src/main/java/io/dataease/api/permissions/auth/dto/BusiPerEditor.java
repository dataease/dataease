package io.dataease.api.permissions.auth.dto;

import io.dataease.api.permissions.auth.vo.PermissionItem;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class BusiPerEditor extends BusiPermissionRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 3067994331757489447L;

    private List<PermissionItem> permissions;
}
