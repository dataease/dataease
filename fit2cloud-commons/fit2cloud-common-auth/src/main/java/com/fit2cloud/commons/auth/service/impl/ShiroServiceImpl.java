package com.fit2cloud.commons.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fit2cloud.commons.auth.dao.ExtUserMapper;
import com.fit2cloud.commons.auth.entity.Permission;
import com.fit2cloud.commons.auth.service.ShiroService;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ShiroServiceImpl implements ShiroService {


    @Autowired(required = false)
    private ExtUserMapper extUserMapper;


    @Override
    public Map<String, String> loadFilterChainDefinitionMap() {
        // 权限控制map
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 配置过滤:不会被拦截的链接 -> 放行 start ----------------------------------------------------------
        // 放行Swagger2页面，需要放行这些

        filterChainDefinitionMap.put("/swagger-ui.html","anon");
        filterChainDefinitionMap.put("/swagger/**","anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**","anon");
        filterChainDefinitionMap.put("/v2/**","anon");
        filterChainDefinitionMap.put("/static/**", "anon");

        // 登陆
        filterChainDefinitionMap.put("/login", "anon");
        // 退出
        //filterChainDefinitionMap.put("/logout", "anon");
        // 放行未授权接口，重定向使用
        filterChainDefinitionMap.put("/unauth", "anon");
        // token过期接口
        filterChainDefinitionMap.put("/tokenExpired", "anon");
        // 被挤下线
        filterChainDefinitionMap.put("/downline", "anon");
        // 放行 end ----------------------------------------------------------
        filterChainDefinitionMap.put("/logout", "logout");
        //List<Resource> resources = resourceMapper.selectList(null);
        QueryWrapper<Permission> wrapper = new QueryWrapper<>();
        wrapper.eq("type", "role");
        /*List<ExtPermissionBean> extPermissionBeans = extUserMapper.getPermissions();


        if (CollectionUtils.isNotEmpty(extPermissionBeans)){
            Map<PermissionKey, List<ExtPermissionBean>> resourcePerMap = extPermissionBeans.stream().collect(Collectors.groupingBy(ExtPermissionBean::getKey));
            resourcePerMap.entrySet().stream().forEach(entry -> {
                PermissionKey permissionKey = entry.getKey();
                String url = permissionKey.getUrl();
                String resourceId = permissionKey.getResourceId();
                //List<ExtPermissionBean> permissionList = entry.getValue();
                //StringJoiner f2cRoles = new StringJoiner(",", "f2cRoles[", "]");
                StringJoiner f2cPerms = new StringJoiner(",", "f2cPerms[", "]");
                f2cPerms.add(resourceId);
                *//*permissionList.forEach(per -> {
                    String roleId = per.getRelationId();
                    f2cRoles.add(roleId);
                });*//*
                filterChainDefinitionMap.put(url, "jwt," + f2cPerms);
            });
        }*/
        filterChainDefinitionMap.put("/**", "jwt");
        return filterChainDefinitionMap;
    }

    @Override
    public void updatePermission(ShiroFilterFactoryBean shiroFilterFactoryBean, Integer roleId, Boolean isRemoveSession) {

    }

    @Override
    public void updatePermissionByRoleId(Integer roleId, Boolean isRemoveSession) {

    }
}
