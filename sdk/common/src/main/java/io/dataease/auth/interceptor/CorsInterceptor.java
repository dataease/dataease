package io.dataease.auth.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

public class CorsInterceptor implements HandlerInterceptor {




    private List<String> originList;

    public CorsInterceptor(List<String> originList) {
        this.originList = originList;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String origin = request.getHeader("Origin");
        if (StringUtils.isNotBlank(origin) && originList.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Max-Age", "3600");
        }
        if (StringUtils.equalsIgnoreCase(request.getMethod(), "options")) {
            response.setStatus(200);
            return false;
        }
        return true;
    }
}
