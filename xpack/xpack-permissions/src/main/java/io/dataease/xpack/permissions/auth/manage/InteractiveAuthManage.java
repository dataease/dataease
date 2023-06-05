package io.dataease.xpack.permissions.auth.manage;

import cn.hutool.core.collection.CollectionUtil;
import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.dataease.api.permissions.auth.vo.PermissionOrigin;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.exception.DEException;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.utils.AuthUtils;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.MenuAuthExtMapper;
import io.dataease.xpack.permissions.user.entity.UserRole;
import io.dataease.xpack.permissions.user.manage.RoleManage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InteractiveAuthManage {

    private static final List<Long> XPACKMENUIDS = new ArrayList<>();
    @Resource
    private RoleAuthManage roleAuthManage;

    @Resource
    private RoleManage roleManage;

    @Resource
    private BusiAuthManage busiAuthManage;

    @Resource
    private UserAuthManage userAuthManage;

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

    public List<Long> resourceIds(String flag) {
        BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(flag.toUpperCase());
        if (ObjectUtils.isEmpty(busiResourceEnum)) {
            DEException.throwException("invalid flag value");
        }
        int enumFlag = busiResourceEnum.getFlag();
        TokenUserBO user = AuthUtils.getUser();
        Long uid = user.getUserId();
        Long oid = user.getDefaultOid();
        if (AuthUtils.isSysAdmin(uid)) {
            return busiAuthManage.resourceIdsByRt(enumFlag, oid);
        }
        List<UserRole> userRoles = roleManage.userRole(uid, user.getDefaultOid());
        if (isRootAdmin(userRoles)) {
            return busiAuthManage.resourceIdsByRt(enumFlag, oid);
        }
        Set<Long> set = null;
        List<PermissionItem> permissionItems = userAuthManage.permissionItems(uid, oid, enumFlag);
        if (CollectionUtil.isNotEmpty(permissionItems)) {
            set = permissionItems.stream().map(PermissionItem::getId).collect(Collectors.toSet());
        }
        if (CollectionUtil.isNotEmpty(userRoles)) {
            List<PermissionOrigin> permissionOrigins = roleAuthManage.roleOrigin(userRoles, enumFlag);
            if (CollectionUtil.isNotEmpty(permissionOrigins)) {
                Set<Long> itemSet = permissionOrigins.stream().flatMap(origin -> origin.getPermissions().stream().map(PermissionItem::getId)).collect(Collectors.toSet());
                set.addAll(itemSet);
            }
        }
        if (CollectionUtil.isNotEmpty(set)) {
            return new ArrayList<>(set);
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
