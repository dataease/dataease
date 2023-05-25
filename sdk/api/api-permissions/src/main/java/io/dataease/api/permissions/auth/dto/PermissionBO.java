package io.dataease.api.permissions.auth.dto;

import io.dataease.api.permissions.auth.vo.PermissionItem;
import lombok.Data;


@Data
public class PermissionBO extends PermissionItem {

    private Long resourceId;
}
