package io.dataease.config;

import io.dataease.visualization.server.StaticResourceContentGuard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/** Applied only to uploaded static resources, never to the application HTML. */
public class StaticResourceSecurityInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof ResourceHttpRequestHandler)) return true;
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy",
                "sandbox; default-src 'none'; script-src 'none'; style-src 'unsafe-inline'; base-uri 'none'; form-action 'none'");
        String decodedPath = org.springframework.web.util.UriUtils.decode(request.getRequestURI(), java.nio.charset.StandardCharsets.UTF_8);
        if (decodedPath.contains("de-material-")
                || decodedPath.contains(".material-tmp")
                || !StaticResourceContentGuard.allowedExtension(request.getRequestURI())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }
        return true;
    }
}
