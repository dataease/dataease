package io.dataease.share.interceptor;

import io.dataease.auth.DeLinkPermit;
import io.dataease.constant.AuthConstant;
import io.dataease.exception.DEException;
import io.dataease.utils.ServletUtils;
import io.dataease.utils.WhitelistUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;


@Component
public class LinkInterceptor implements HandlerInterceptor {

    private final static String whiteListText = "/user/ipInfo, /apisix/check, /datasetData/enumValue, /datasetData/enumValueObj, /datasetData/getFieldTree, /dekey, /symmetricKey, /share/validate, /sysParameter/queryOnlineMap, /xpackComponent/viewPlugins";


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String linkToken = ServletUtils.getHead(AuthConstant.LINK_TOKEN_KEY);
        if (linkToken == null) {
            return true;
        }
        if (handler instanceof HandlerMethod handlerMethod) {
            DeLinkPermit deLinkPermit = handlerMethod.getMethodAnnotation(DeLinkPermit.class);
            if (deLinkPermit == null) {

                List<String> whiteList = Arrays.stream(StringUtils.split(whiteListText, ",")).map(String::trim).toList();
                String requestURI = ServletUtils.request().getRequestURI();
                if (StringUtils.startsWith(requestURI, WhitelistUtils.getContextPath())) {
                    requestURI = requestURI.replaceFirst(WhitelistUtils.getContextPath(), "");
                }
                if (StringUtils.startsWith(requestURI, AuthConstant.DE_API_PREFIX)) {
                    requestURI = requestURI.replaceFirst(AuthConstant.DE_API_PREFIX, "");
                }
                boolean valid = whiteList.contains(requestURI) || WhitelistUtils.match(requestURI);
                if (!valid) {
                    DEException.throwException("分享链接Token不支持访问当前url[" + requestURI + "]");
                }
                return true;
            }
        }
        return true;
    }


}
