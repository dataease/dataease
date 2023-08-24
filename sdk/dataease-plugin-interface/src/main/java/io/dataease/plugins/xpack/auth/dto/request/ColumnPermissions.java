package io.dataease.plugins.xpack.auth.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ColumnPermissions {
    private Boolean enable;
    private List<ColumnPermissionItem> columns;
}
