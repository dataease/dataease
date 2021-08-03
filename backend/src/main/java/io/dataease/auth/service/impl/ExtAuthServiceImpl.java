package io.dataease.auth.service.impl;

import io.dataease.auth.service.ExtAuthService;
import io.dataease.base.domain.SysAuth;
import io.dataease.base.domain.SysAuthExample;
import io.dataease.base.mapper.SysAuthMapper;
import io.dataease.base.mapper.ext.ExtAuthMapper;
import io.dataease.commons.model.AuthURD;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExtAuthServiceImpl implements ExtAuthService {

    @Resource
    private ExtAuthMapper extAuthMapper;

    @Resource
    private SysAuthMapper sysAuthMapper;

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
        SysAuthExample example = new SysAuthExample();
        example.createCriteria().andAuthSourceEqualTo(resourceId);
        List<SysAuth> sysAuths = sysAuthMapper.selectByExample(example);
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
}
