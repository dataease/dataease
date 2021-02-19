package io.dataease.commons.auth.service.impl;

import io.dataease.commons.auth.bean.ExtPermissionBean;
import io.dataease.commons.auth.dao.ExtUserMapper;
import io.dataease.commons.auth.service.ShiroService;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

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

        List<ExtPermissionBean> extPermissionBeans = extUserMapper.getPermissions();

        extPermissionBeans.forEach(item -> {
            StringJoiner f2cPerms = new StringJoiner(",", "f2cPerms[", "]");
            f2cPerms.add(item.getPermission());
            filterChainDefinitionMap.put(item.getPath(), "jwt," + f2cPerms);
        });

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
