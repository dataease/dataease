package io.dataease.api.chart.dto;

import lombok.Data;

import java.util.List;

@Data
public class ColumnPermissions {
    private Boolean enable;
    private List<ColumnPermissionItem> columns;
}
