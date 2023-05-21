package io.dataease.utils;

import io.dataease.constant.AuthConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class ServletUtils {

    public static HttpServletRequest request() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return request;
    }

    public static HttpServletResponse response() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
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

    public static boolean apisixCheck() {
        System.out.println("apisixCheck");
        return true;
//        String head = getHead(AuthConstant.APISIX_FLAG_KEY);
//        if (StringUtils.isBlank(head)) return false;
//        long time = Long.parseLong(head);
//        return System.currentTimeMillis() - time < 10000;
    }
}
