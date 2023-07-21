package io.dataease.xpack.permissions.auth.manage;

import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.dataease.api.permissions.auth.vo.PermissionOrigin;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.exception.DEException;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.xpack.permissions.user.entity.UserRole;
import io.dataease.xpack.permissions.user.manage.RoleManage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiAuthManage extends OrgResourceManage {

    private static final String ERROR_MSG = "没有访问权限";

    @Resource
    private RoleManage roleManage;

    @Resource
    private UserAuthManage userAuthManage;

    @Resource
    private RoleAuthManage roleAuthManage;


    public void checkMenu(Long menuId, Integer weight, TokenUserBO userBO) {
        Long uid = userBO.getUserId();
        Long oid = userBO.getDefaultOid();
        List<UserRole> userRoles = userRoles(uid, oid);
        if (AuthUtils.isSysAdmin(uid) || isRootAdmin(userRoles)) {
            return;
        }
        if (isRootReadonly(userRoles) && weight == 1) {
            return;
        }
        List<PermissionOrigin> permissionOrigins = roleAuthManage.roleOrigin(userRoles, 0);
        if (permissionOrigins.stream().anyMatch(origin -> origin.getPermissions().stream().anyMatch(per -> per.getId().equals(menuId) && per.getWeight() >= weight))) {
            return;
        }
        DEException.throwException(ERROR_MSG);
    }

    public void checkResource(Long resourceId, Integer flag, Integer weight, TokenUserBO userBO) {
        Long uid = userBO.getUserId();
        Long oid = userBO.getDefaultOid();
        List<UserRole> userRoles = userRoles(uid, oid);
        List<Long> resourceIds = orgResource(flag, oid);
        if (!resourceIds.contains(resourceId)) {
            DEException.throwException(ERROR_MSG);
        }
        if (flag > 4) return;
        if (AuthUtils.isSysAdmin(uid) || isRootAdmin(userRoles)) {
            return;
        }
        if (isRootReadonly(userRoles) && weight == 1) {
            return;
        }
        List<PermissionItem> userPers = userAuthManage.permissionItems(uid, oid, flag);
        if (userPers.stream().anyMatch(per -> per.getId().equals(resourceId) && per.getWeight() >= weight)) {
            return;
        }
        List<PermissionOrigin> permissionOrigins = roleAuthManage.roleOrigin(userRoles, flag);
        if (permissionOrigins.stream().anyMatch(origin -> origin.getPermissions().stream().anyMatch(per -> per.getId().equals(resourceId) && per.getWeight() >= weight))) {
            return;
        }
        DEException.throwException(ERROR_MSG);
    }

    private List<Long> orgResource(Integer flag, Long oid) {
        ApiAuthManage manage = proxy();

        switch (flag) {
            case 1:
                return manage.panelIds(oid);
            case 2:
                return manage.screenIds(oid);
            case 3:
                return manage.datasetIds(oid);
            case 4:
                return manage.datasourceIds(oid);
            case 5:
                return manage.uids(oid);
            case 6:
                return manage.rids(oid);
        }
        return null;
    }

    private List<UserRole> userRoles(Long uid, Long oid) {
        List<UserRole> userRoles = roleManage.userRole(uid, oid);
        return userRoles;
    }

    private ApiAuthManage proxy() {
        return CommonBeanFactory.getBean(ApiAuthManage.class);
    }

    private boolean isRootAdmin(List<UserRole> roles) {
        return roles.stream().anyMatch(UserRole::isRootAdmin);
    }

    private boolean isRootReadonly(List<UserRole> roles) {
        return roles.stream().anyMatch(UserRole::isRootReadonly);
    }
}
