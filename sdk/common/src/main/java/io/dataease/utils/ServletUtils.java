package io.dataease.utils;

import io.dataease.constant.AuthConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ServletUtils {

    public static HttpServletRequest request() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (ObjectUtils.isEmpty(servletRequestAttributes)) return null;
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return request;
    }

    public static HttpServletResponse response() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (ObjectUtils.isEmpty(servletRequestAttributes)) return null;
        HttpServletResponse response = servletRequestAttributes.getResponse();
        return response;
    }

    public static String getHead(String key) {
        HttpServletRequest request = request();
        return request.getHeader(key);
    }

    public static String getToken() {
        return getHead(AuthConstant.TOKEN_KEY);
    }

    public static String getXUserinfo() {
        return getHead(AuthConstant.OIDC_X_USER);
    }

    public static String getLdapUser() {
        String authorization = getHead(AuthConstant.DE_LDAP_AUTHORIZATION);
        if (StringUtils.isBlank(authorization)) return null;
        return authorization;
    }

    public static String getCasUser() {
        return getHead(AuthConstant.CAS_X_USER);
    }

    public static boolean apisixCheck() {
        return true;
    }


}
