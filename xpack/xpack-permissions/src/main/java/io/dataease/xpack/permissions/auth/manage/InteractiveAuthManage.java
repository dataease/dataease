package io.dataease.xpack.permissions.auth.manage;

import cn.hutool.core.collection.CollectionUtil;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.utils.AuthUtils;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.MenuAuthExtMapper;
import io.dataease.xpack.permissions.user.entity.UserRole;
import io.dataease.xpack.permissions.user.manage.RoleManage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InteractiveAuthManage {

    @Resource
    private RoleAuthManage roleAuthManage;

    @Resource
    private RoleManage roleManage;

    @Resource
    private MenuAuthExtMapper menuAuthExtMapper;
    public List<Long> menuIds() {
        TokenUserBO user = AuthUtils.getUser();
        Long uid = user.getUserId();
        if (AuthUtils.isSysAdmin(uid)) {
            return roleAuthManage.queryMenuIds();
        }
        List<UserRole> userRoles = roleManage.userRole(uid, user.getDefaultOid());
        if (isRootAdmin(userRoles)) {
            return roleAuthManage.queryMenuIds();
        }
        List<Long> rids = userRoles.stream().filter(item -> !item.isRoot()).map(UserRole::getId).toList();
        if (CollectionUtil.isNotEmpty(rids)) {
            return menuAuthExtMapper.interactiveMenuIds(rids);
        }
        return null;
    }





    private boolean isRootAdmin(List<UserRole> roles) {
        return roles.stream().anyMatch(UserRole::isRootAdmin);
    }

}
