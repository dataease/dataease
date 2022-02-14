package io.dataease.commons.utils;

import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.entity.AuthItem;
import io.dataease.auth.service.ExtAuthService;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.model.AuthURD;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class AuthUtils {

    private static final String[] defaultPanelPermissions = {"panel_list"};
    private static final String[] defaultDataSetPermissions = {"0"};
    private static final String[] defaultLinkPermissions = {"0"};

    private static ExtAuthService extAuthService;

    @Autowired
    public void setExtAuthService(ExtAuthService extAuthService) {
        AuthUtils.extAuthService = extAuthService;
    }

    public static CurrentUserDto getUser() {
        CurrentUserDto userDto = (CurrentUserDto) SecurityUtils.getSubject().getPrincipal();
        return userDto;
    }

    //根据组织 角色 用户 获取下属用户ID
    public static Set<Long> userIdsByURD(AuthURD request) {
        Set<Long> userIds = extAuthService.userIdsByRD(request);
        if (!CollectionUtils.isEmpty(request.getUserIds())) {
            userIds.addAll(request.getUserIds());
        }
        return userIds;
    }

    // 获取资源对那些人/角色/组织 有权限
    public static AuthURD authURDR(String resourceId) {
        return extAuthService.resourceTarget(resourceId);
    }



    public static Set<AuthItem> permissionByType(String type) {
        CurrentUserDto user = getUser();
        Long userId = user.getUserId();
        Long deptId = user.getDeptId();
        List<CurrentRoleDto> roles = user.getRoles();
        Set<AuthItem> result = new HashSet<>();
        if (StringUtils.equals(DePermissionType.DATASOURCE.name().toLowerCase(), type)) {
            Set<AuthItem> userSet = extAuthService.dataSourceIdByUser(userId).stream().collect(Collectors.toSet());
            Set<AuthItem> roleSet = roles.stream().map(role -> extAuthService.dataSourceIdByRole(role.getId())).flatMap(Collection::stream).collect(Collectors.toSet());
            Set<AuthItem> deptSet = extAuthService.dataSourceIdByDept(deptId).stream().collect(Collectors.toSet());
            result.addAll(userSet);
            result.addAll(roleSet);
            result.addAll(deptSet);
            Arrays.stream(defaultLinkPermissions).forEach(item -> {
                result.add(new AuthItem(item, ResourceAuthLevel.LINK_LEVEL_MANAGE.getLevel()));
            });
            return result;
        }

        else if (StringUtils.equals(DePermissionType.DATASET.name().toLowerCase(), type)) {
            Set<AuthItem> userSet = extAuthService.dataSetIdByUser(userId).stream().collect(Collectors.toSet());
            Set<AuthItem> roleSet = roles.stream().map(role -> extAuthService.dataSetIdByRole(role.getId())).flatMap(Collection::stream).collect(Collectors.toSet());
            Set<AuthItem> deptSet = extAuthService.dataSetIdByDept(deptId).stream().collect(Collectors.toSet());
            result.addAll(userSet);
            result.addAll(roleSet);
            result.addAll(deptSet);
            Arrays.stream(defaultDataSetPermissions).forEach(item -> {
                result.add(new AuthItem(item, ResourceAuthLevel.DATASET_LEVEL_MANAGE.getLevel()));
            });
            return result;
        }
        else if (StringUtils.equals(DePermissionType.PANEL.name().toLowerCase(), type)) {
            Set<AuthItem> userSet = extAuthService.panelIdByUser(userId).stream().collect(Collectors.toSet());
            Set<AuthItem> roleSet = roles.stream().map(role -> extAuthService.panelIdByRole(role.getId())).flatMap(Collection::stream).collect(Collectors.toSet());
            Set<AuthItem> deptSet = extAuthService.panelIdByDept(deptId).stream().collect(Collectors.toSet());
            result.addAll(userSet);
            result.addAll(roleSet);
            result.addAll(deptSet);
            Arrays.stream(defaultPanelPermissions).forEach(item -> {
                result.add(new AuthItem(item, ResourceAuthLevel.PANNEL_LEVEL_MANAGE.getLevel()));
            });
            return result;
        }
        return result;

    }
}
