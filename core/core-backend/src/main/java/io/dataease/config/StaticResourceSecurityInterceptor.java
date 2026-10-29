package io.dataease.config;

import io.dataease.utils.StaticResourceUtils;
import io.dataease.visualization.server.StaticResourceContentGuard;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

/** Applied only to uploaded static resources, never to the application HTML. */
public class StaticResourceSecurityInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof ResourceHttpRequestHandler resourceHandler)) return true;
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Content-Security-Policy",
                "sandbox; default-src 'none'; script-src 'none'; style-src 'unsafe-inline'; base-uri 'none'; form-action 'none'");
        try {
            String fileName = StaticResourceUtils.getResourceFileName(request.getRequestURI());
            if (!StaticResourceContentGuard.allowedExtension(fileName)) {
                throw new IOException("Invalid static resource extension");
            }
            for (var location : resourceHandler.getLocations()) {
                if (!location.isFile()) continue;
                Path root = location.getFile().toPath();
                if (Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
                    StaticResourceUtils.resolveResourcePath(root, fileName);
                }
            }
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return false;
        }
        return true;
    }
}
