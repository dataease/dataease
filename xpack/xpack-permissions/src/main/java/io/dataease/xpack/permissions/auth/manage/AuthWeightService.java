package io.dataease.xpack.permissions.auth.manage;

import cn.hutool.core.collection.CollectionUtil;
import io.dataease.api.permissions.auth.vo.PermissionItem;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthWeightService {

    public List<PermissionItem> filterValid(List<PermissionItem> permissionItems) {
        if (CollectionUtil.isNotEmpty(permissionItems)) {
            permissionItems = permissionItems.stream().filter(this::weightValid).toList();
        }
        return permissionItems;
    }
    private boolean weightValid(int weight) {
        return weight > 0;
    }

    private boolean weightValid(PermissionItem item) {
        return ObjectUtils.isNotEmpty(item) && weightValid(item.getWeight());
    }
}
