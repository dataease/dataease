package io.dataease.auth.service.impl;

import io.dataease.auth.service.ShiroService;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ShiroServiceImpl implements ShiroService {

    private final static String ANON = "anon";

    @Override
    public Map<String, String> loadFilterChainDefinitionMap() {
        // 权限控制map
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 配置过滤:不会被拦截的链接 -> 放行 start
        // ----------------------------------------------------------
        // 放行Swagger2页面，需要放行这些

        filterChainDefinitionMap.put("/doc.html**", "doc");
        filterChainDefinitionMap.put("/deApi**", ANON);
        filterChainDefinitionMap.put("/swagger-ui.html", ANON);
        filterChainDefinitionMap.put("/swagger-ui/**", ANON);
        filterChainDefinitionMap.put("/swagger/**", ANON);
        filterChainDefinitionMap.put("/webjars/**", ANON);
        filterChainDefinitionMap.put("/swagger-resources/**", ANON);
        filterChainDefinitionMap.put("/v2/**", ANON);
        filterChainDefinitionMap.put("/v3/**", ANON);

        filterChainDefinitionMap.put("/static/**", ANON);
        filterChainDefinitionMap.put("/css/**", ANON);
        filterChainDefinitionMap.put("/js/**", ANON);
        filterChainDefinitionMap.put("/img/**", ANON);
        filterChainDefinitionMap.put("/fonts/**", ANON);
        filterChainDefinitionMap.put("/favicon.ico", ANON);
        filterChainDefinitionMap.put("/", ANON);
        filterChainDefinitionMap.put("/login", ANON);
        filterChainDefinitionMap.put("/link/**", ANON);
        filterChainDefinitionMap.put("/index.html", ANON);
        filterChainDefinitionMap.put("/link.html", ANON);
        filterChainDefinitionMap.put("/board/**", ANON);

        // 获取主题信息
        filterChainDefinitionMap.put("/plugin/theme/themes", ANON);
        filterChainDefinitionMap.put("/plugin/theme/items/**", ANON);
        filterChainDefinitionMap.put("/plugin/view/types", ANON);

        // 验证链接
        filterChainDefinitionMap.put("/api/link/validate**", ANON);
        filterChainDefinitionMap.put("/api/map/areaEntitys/**", ANON);
        filterChainDefinitionMap.put("/dataset/field/fieldValues/**", ANON);
        filterChainDefinitionMap.put("/linkJump/queryPanelJumpInfo/**", ANON);
        filterChainDefinitionMap.put("/linkJump/queryTargetPanelJumpInfo", ANON);

        //外部跳转参数
        filterChainDefinitionMap.put("/outerParams/**", ANON);


        filterChainDefinitionMap.put("/tempMobileLink/**", ANON);
        filterChainDefinitionMap.put("/de-app/**", ANON);
        filterChainDefinitionMap.put("/app.html", ANON);

        filterChainDefinitionMap.put("/**/*.json", ANON);
        filterChainDefinitionMap.put("/system/ui/**", ANON);
        filterChainDefinitionMap.put("/**/*.js", ANON);
        filterChainDefinitionMap.put("/**/*.css", ANON);
        filterChainDefinitionMap.put("/**/*.map", ANON);
        filterChainDefinitionMap.put("/**/*.svg", ANON);


        filterChainDefinitionMap.put("/api/auth/login", ANON);
        filterChainDefinitionMap.put("/api/auth/isPluginLoaded", ANON);
        filterChainDefinitionMap.put("/system/requestTimeOut", ANON);
        filterChainDefinitionMap.put("/api/auth/validateName", ANON);
        filterChainDefinitionMap.put("/api/auth/isOpenLdap", ANON);
        filterChainDefinitionMap.put("/api/auth/isOpenOidc", ANON);
        filterChainDefinitionMap.put("/api/auth/getPublicKey", ANON);
        filterChainDefinitionMap.put("/api/pluginCommon/component/*", ANON);
        filterChainDefinitionMap.put("/api/pluginCommon/staticInfo/**", ANON);
        filterChainDefinitionMap.put("/plugin/oidc/authInfo", ANON);
        filterChainDefinitionMap.put("/sso/callBack*", ANON);

        filterChainDefinitionMap.put("/unauth", ANON);
        filterChainDefinitionMap.put("/display/**", ANON);
        filterChainDefinitionMap.put("/tokenExpired", ANON);
        filterChainDefinitionMap.put("/downline", ANON);
        filterChainDefinitionMap.put("/common-files/**", ANON);
        filterChainDefinitionMap.put("/linkage/getPanelAllLinkageInfo/**", ANON);
        filterChainDefinitionMap.put("/api/auth/logout", "logout");

        filterChainDefinitionMap.put("/api/link/resourceDetail/**", "link");
        filterChainDefinitionMap.put("/api/link/viewDetail/**", "link");
        filterChainDefinitionMap.put("/dataset/field/linkMultFieldValues", "link");

        filterChainDefinitionMap.put("/**", "authc");

        filterChainDefinitionMap.put("/**", "jwt");

        return filterChainDefinitionMap;
    }

    @Override
    public void updatePermission(ShiroFilterFactoryBean shiroFilterFactoryBean, Integer roleId,
            Boolean isRemoveSession) {

    }

    @Override
    public void updatePermissionByRoleId(Integer roleId, Boolean isRemoveSession) {

    }
}
