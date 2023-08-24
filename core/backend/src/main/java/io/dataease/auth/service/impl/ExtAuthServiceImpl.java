package io.dataease.auth.service.impl;

import io.dataease.auth.entity.AuthItem;
import io.dataease.auth.service.ExtAuthService;
import io.dataease.plugins.common.base.domain.SysAuth;
import io.dataease.ext.ExtAuthMapper;
import io.dataease.commons.constants.AuthConstants;
import io.dataease.commons.model.AuthURD;
import io.dataease.commons.utils.LogUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExtAuthServiceImpl implements ExtAuthService {

    private static final List<AuthItem> emptyResult = new ArrayList();

    @Resource
    private ExtAuthMapper extAuthMapper;

    @Override
    public Set<Long> userIdsByRD(AuthURD request) {
        Set<Long> result = new HashSet<>();
        List<Long> roleIds = request.getRoleIds();
        List<Long> deptIds = request.getDeptIds();
        if (!CollectionUtils.isEmpty(roleIds)) {
            result.addAll(extAuthMapper.queryUserIdWithRoleIds(roleIds));
        }
        if (!CollectionUtils.isEmpty(deptIds)) {
            result.addAll(extAuthMapper.queryUserIdWithDeptIds(deptIds));
        }

        return result;
    }

    @Override
    public AuthURD resourceTarget(String resourceId) {
        AuthURD authURD = new AuthURD();

        List<SysAuth> sysAuths = extAuthMapper.queryByResource(resourceId);

        Map<String, List<SysAuth>> authMap = sysAuths.stream().collect(Collectors.groupingBy(SysAuth::getAuthTargetType));
        if (!CollectionUtils.isEmpty(authMap.get("user"))) {
            authURD.setUserIds(authMap.get("user").stream().map(item -> Long.parseLong(item.getAuthTarget())).collect(Collectors.toList()));
        }

        if (!CollectionUtils.isEmpty(authMap.get("role"))) {
            authURD.setRoleIds(authMap.get("role").stream().map(item -> Long.parseLong(item.getAuthTarget())).collect(Collectors.toList()));
        }

        if (!CollectionUtils.isEmpty(authMap.get("dept"))) {
            authURD.setDeptIds(authMap.get("dept").stream().map(item -> Long.parseLong(item.getAuthTarget())).collect(Collectors.toList()));
        }
        return authURD;
    }

    @Cacheable(value = AuthConstants.USER_LINK_NAME, key = "'user' + #userId")
    @Override
    public List<AuthItem> dataSourceIdByUser(Long userId) {
        return extAuthMapper.dataSourceIdByUser(userId.toString());
    }

    @Cacheable(value = AuthConstants.USER_DATASET_NAME, key = "'user' + #userId")
    @Override
    public List<AuthItem> dataSetIdByUser(Long userId) {
        return extAuthMapper.dataSetIdByUser(userId.toString());
    }

    @Cacheable(value = AuthConstants.USER_PANEL_NAME, key = "'user' + #userId")
    @Override
    public List<AuthItem> panelIdByUser(Long userId) {
        return extAuthMapper.panelIdByUser(userId.toString());
    }


    @Cacheable(value = AuthConstants.ROLE_LINK_NAME, key = "'role' + #roleId")
    @Override
    public List<AuthItem> dataSourceIdByRole(Long roleId) {
        return extAuthMapper.dataSourceIdByRole(roleId.toString());
    }

    @Cacheable(value = AuthConstants.ROLE_DATASET_NAME, key = "'role' + #roleId")
    @Override
    public List<AuthItem> dataSetIdByRole(Long roleId) {
        return extAuthMapper.dataSetIdByRole(roleId.toString());
    }

    @Cacheable(value = AuthConstants.ROLE_PANEL_NAME, key = "'role' + #roleId")
    @Override
    public List<AuthItem> panelIdByRole(Long roleId) {
        return extAuthMapper.panelIdByRole(roleId.toString());
    }

    @Cacheable(value = AuthConstants.DEPT_LINK_NAME, key = "'dept' + #deptId")
    @Override
    public List<AuthItem> dataSourceIdByDept(Long deptId) {
        if (ObjectUtils.isEmpty(deptId)) return emptyResult;
        return extAuthMapper.dataSourceIdByDept(deptId.toString());
    }

    @Cacheable(value = AuthConstants.DEPT_DATASET_NAME, key = "'dept' + #deptId")
    @Override
    public List<AuthItem> dataSetIdByDept(Long deptId) {
        if (ObjectUtils.isEmpty(deptId)) return emptyResult;
        return extAuthMapper.dataSetIdByDept(deptId.toString());
    }

    @Cacheable(value = AuthConstants.DEPT_PANEL_NAME, key = "'dept' + #deptId")
    @Override
    public List<AuthItem> panelIdByDept(Long deptId) {
        if (ObjectUtils.isEmpty(deptId)) return emptyResult;
        return extAuthMapper.panelIdByDept(deptId.toString());
    }

    @Caching(evict = {
            @CacheEvict(value = AuthConstants.USER_LINK_NAME, key = "'user' + #userId"),
            @CacheEvict(value = AuthConstants.USER_DATASET_NAME, key = "'user' + #userId"),
            @CacheEvict(value = AuthConstants.USER_PANEL_NAME, key = "'user' + #userId")
    })
    public void clearUserResource(Long userId) {
        LogUtil.info("all permission resource of user {} is cleaning...", userId);
    }

    @Caching(evict = {
            @CacheEvict(value = AuthConstants.DEPT_LINK_NAME, key = "'dept' + #deptId"),
            @CacheEvict(value = AuthConstants.DEPT_DATASET_NAME, key = "'dept' + #deptId"),
            @CacheEvict(value = AuthConstants.DEPT_PANEL_NAME, key = "'dept' + #deptId")
    })
    public void clearDeptResource(Long deptId) {
        LogUtil.info("all permission resource of dept {} is cleaning...", deptId);
    }

    @Caching(evict = {
            @CacheEvict(value = AuthConstants.ROLE_LINK_NAME, key = "'role' + #roleId"),
            @CacheEvict(value = AuthConstants.ROLE_DATASET_NAME, key = "'role' + #roleId"),
            @CacheEvict(value = AuthConstants.ROLE_PANEL_NAME, key = "'role' + #roleId")
    })
    public void clearRoleResource(Long roleId) {
        LogUtil.info("all permission resource of role {} is cleaning...", roleId);
    }

    @Override
    public List<String> parentResource(String resourceId, String type) {
        String s = extAuthMapper.parentResource(resourceId, type);
        if (StringUtils.isNotBlank(s)) {
            String[] split = s.split(",");
            List<String> results = new ArrayList<>();
            for (int i = 0; i < split.length; i++) {
                String s1 = split[i];
                if (StringUtils.isNotBlank(s1)) {
                    results.add(s1);
                }
            }
            return CollectionUtils.isEmpty(results) ? null : results;
        }
        return null;
    }
}
