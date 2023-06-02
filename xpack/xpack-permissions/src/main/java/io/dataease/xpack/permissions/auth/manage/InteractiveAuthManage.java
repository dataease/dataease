package io.dataease.xpack.permissions.auth.manage;

import cn.hutool.core.collection.CollectionUtil;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.utils.AuthUtils;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.MenuAuthExtMapper;
import io.dataease.xpack.permissions.user.entity.UserRole;
import io.dataease.xpack.permissions.user.manage.RoleManage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class InteractiveAuthManage {

    private static final List<Long> XPACKMENUIDS = new ArrayList<>();
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
            return xpackFilter(roleAuthManage.queryMenuIds());
        }
        List<UserRole> userRoles = roleManage.userRole(uid, user.getDefaultOid());
        if (isRootAdmin(userRoles)) {
            return xpackFilter(roleAuthManage.queryMenuIds());
        }
        List<Long> rids = userRoles.stream().filter(item -> !item.isRoot()).map(UserRole::getId).toList();
        if (CollectionUtil.isNotEmpty(rids)) {
            return xpackFilter(menuAuthExtMapper.interactiveMenuIds(rids));
        }
        return null;
    }

    private List<Long> xpackFilter(List<Long> mids) {
        if (LicenseUtil.licenseValid()) return mids;
        return mids.stream().filter(id -> !XPACKMENUIDS.contains(id)).toList();
    }





    private boolean isRootAdmin(List<UserRole> roles) {
        return roles.stream().anyMatch(UserRole::isRootAdmin);
    }


    @PostConstruct
    public void init() {
        XPACKMENUIDS.add(8L);
        XPACKMENUIDS.add(9L);
        XPACKMENUIDS.add(10L);
    }

}
