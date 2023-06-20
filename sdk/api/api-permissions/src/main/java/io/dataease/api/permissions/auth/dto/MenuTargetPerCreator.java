package io.dataease.api.permissions.auth.dto;

import io.dataease.api.permissions.auth.vo.PermissionItem;
import lombok.Data;

import java.util.List;

@Data
public class MenuTargetPerCreator extends TargetPerCreator{

    private List<PermissionItem> permissions;
}
