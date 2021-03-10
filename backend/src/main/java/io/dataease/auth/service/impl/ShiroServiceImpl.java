package io.dataease.auth.service.impl;

import io.dataease.auth.service.ShiroService;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Service
public class ShiroServiceImpl implements ShiroService {

    @Override
    public Map<String, String> loadFilterChainDefinitionMap() {
        // 权限控制map
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 配置过滤:不会被拦截的链接 -> 放行 start ----------------------------------------------------------
        // 放行Swagger2页面，需要放行这些

        filterChainDefinitionMap.put("/swagger-ui.html","anon");
        filterChainDefinitionMap.put("/swagger-ui/**","anon");

        filterChainDefinitionMap.put("/swagger/**","anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**","anon");
        filterChainDefinitionMap.put("/v2/**","anon");
        filterChainDefinitionMap.put("/v3/**","anon");
        filterChainDefinitionMap.put("/static/**", "anon");


        // filterChainDefinitionMap.put("/401", "anon");
        // filterChainDefinitionMap.put("/404", "anon");
        // 登陆
        // filterChainDefinitionMap.put("/api/auth/logout", "anon");
        filterChainDefinitionMap.put("/api/auth/test", "anon");
        filterChainDefinitionMap.put("/api/auth/login", "anon");
        // 退出

        // 放行未授权接口，重定向使用
        filterChainDefinitionMap.put("/unauth", "anon");
        filterChainDefinitionMap.put("/display/**", "anon");

        // token过期接口
        filterChainDefinitionMap.put("/tokenExpired", "anon");
        // 被挤下线
        filterChainDefinitionMap.put("/downline", "anon");
        // 放行 end ----------------------------------------------------------

        /*List<ExtPermissionBean> extPermissionBeans = extUserMapper.getPermissions();

        extPermissionBeans.forEach(item -> {
            StringJoiner f2cPerms = new StringJoiner(",", "f2cPerms[", "]");
            f2cPerms.add(item.getPermission());
            filterChainDefinitionMap.put(item.getPath(), "jwt," + f2cPerms);
        });
*/
        filterChainDefinitionMap.put("/api/auth/logout", "logout");
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
