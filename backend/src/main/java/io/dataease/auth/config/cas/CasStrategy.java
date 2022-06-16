package io.dataease.auth.config.cas;

import io.dataease.auth.service.impl.ShiroServiceImpl;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.service.system.SystemParameterService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.AntPathMatcher;
import org.jasig.cas.client.authentication.UrlPatternMatcherStrategy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class CasStrategy implements UrlPatternMatcherStrategy {


    private static Set<String>  releaseTypes = new HashSet<>();

    @PostConstruct
    public void init() {
        releaseTypes.add("anon");
        releaseTypes.add("link");
        releaseTypes.add("doc");
    }
    @Override
    public boolean matches(String s) {
        SystemParameterService service = CommonBeanFactory.getBean(SystemParameterService.class);
        String serviceValue = service.getValue("cas.callBack");
        if (StringUtils.isBlank(serviceValue)) return false;
        String serverName = serviceValue.substring(0, serviceValue.indexOf("/cas/callBack"));
        int beginIndex = -1;
        if ((beginIndex = s.indexOf(serverName)) != -1) {
            s = s.substring(beginIndex + serverName.length());
        }
        if (StringUtils.equals("/", s)) return false;
        if (StringUtils.equals("/login", s)) return false;
        if (StringUtils.startsWith(s, "/cas/callBack")) return false;
        if (StringUtils.equals("/api/auth/deLogout", s)) return true;
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        ShiroServiceImpl shiroService = CommonBeanFactory.getBean(ShiroServiceImpl.class);
        Map<String, String> stringStringMap = shiroService.loadFilterChainDefinitionMap();
        for (Map.Entry<String, String> entry : stringStringMap.entrySet()) {
            if (releaseTypes.contains(entry.getValue())) {
                boolean matches = antPathMatcher.matches(entry.getKey(), s);
                if (matches) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void setPattern(String s) {

    }
}
