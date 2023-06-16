package io.dataease.xpack.permissions.auth.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.dataease.api.permissions.auth.vo.PermissionOrigin;
import io.dataease.utils.CacheUtils;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthMenu;
import io.dataease.xpack.permissions.auth.dao.auto.mapper.PerAuthMenuMapper;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.MenuAuthExtMapper;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MenuAuthManage extends ServiceImpl<PerAuthMenuMapper, PerAuthMenu> {

    private static final String cacheName = "role_menu_pers";
    @Resource
    private MenuAuthExtMapper menuAuthExtMapper;

    @Resource
    private AuthWeightService authWeightService;

    public List<PermissionItem> permissionItems(Long rid) {
        List<PermissionItem> permissionItems = null;
        String key = rid.toString();
        if (CacheUtils.keyExist(cacheName, key)) {
            permissionItems = (List<PermissionItem>) CacheUtils.get(cacheName, key);
        } else {
            permissionItems = menuAuthExtMapper.rolePermission(rid);
            CacheUtils.put(cacheName, key, permissionItems);
        }
        return authWeightService.filterValid(permissionItems);
    }

    public List<PermissionOrigin> roleOrigin(List<Long> notMatchRids) {
        if (CollectionUtil.isNotEmpty(notMatchRids)) {
            List<PermissionOrigin> permissionOrigins = menuAuthExtMapper.batchRolePermission(notMatchRids);
            return permissionOrigins;
        }
        return null;
    }
    public List<PermissionItem> permissionItems(List<Long> rids) {
        List<PermissionItem> permissionItems = new ArrayList<>();
        List<Long> notMatchRids = rids.stream().filter(rid -> {
            String key = rid.toString();
            if (CacheUtils.keyExist(cacheName, key)) {
                permissionItems.addAll((List<PermissionItem>) CacheUtils.get(cacheName, key));
                return false;
            }
            return true;
        }).toList();

        if (CollectionUtil.isNotEmpty(notMatchRids)) {
            List<PermissionOrigin> permissionOrigins = roleOrigin(notMatchRids);
            permissionOrigins.forEach(origin -> {
                Long rid = origin.getId();
                List<PermissionItem> permissions = origin.getPermissions();
                CacheUtils.put(cacheName, rid.toString(), permissions);
                if (CollectionUtil.isNotEmpty(permissions)) {
                    permissionItems.addAll(permissions);
                }
            });
        }
        return permissionItems.stream().distinct().toList();
    }

}
