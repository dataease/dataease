package io.dataease.share.interceptor;

import io.dataease.permission.util.TokenUtils;
import io.dataease.permission.util.V3UserUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LinkInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (V3UserUtil.getLink() != null) {
            try {
                TokenUtils.linkVerifier().checkRequestPath(request.getRequestURI(), request.getMethod());
            } catch (IllegalArgumentException e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setHeader("DE-FORBIDDEN-FLAG", "No access permission");
                return false;
            }
        }
        return true;
    }
}
