package io.dataease.websocket.handler;

import io.dataease.constant.AuthConstant;
import io.dataease.websocket.WebSocketAuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 握手认证与 Origin 校验。
 * 客户端声明的 userId 在这里被完全忽略，Principal 只来自服务端认证结果。
 */
public class DeHandshakeInterceptor implements HandshakeInterceptor {

    public static final String AUTH_USER_ID_ATTRIBUTE = DeHandshakeInterceptor.class.getName() + ".AUTH_USER_ID";

    private final WebSocketAuthService webSocketAuthService;
    private final List<String> allowedOriginPatterns;

    public DeHandshakeInterceptor(WebSocketAuthService webSocketAuthService, List<String> allowedOriginPatterns) {
        this.webSocketAuthService = webSocketAuthService;
        this.allowedOriginPatterns = allowedOriginPatterns == null ? List.of() : List.copyOf(allowedOriginPatterns);
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {
        if (!isOriginAllowed(request)) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return false;
        }
        Long userId = webSocketAuthService.authenticate(resolveToken(request));
        if (userId == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        attributes.put(AUTH_USER_ID_ATTRIBUTE, userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }

    private boolean isOriginAllowed(ServerHttpRequest request) {
        String origin = request.getHeaders().getOrigin();
        if (StringUtils.isBlank(origin)) {
            return true;
        }
        for (String pattern : allowedOriginPatterns) {
            if ("*".equals(pattern) || StringUtils.equalsIgnoreCase(pattern, origin)) {
                return true;
            }
        }
        return false;
    }

    private String resolveToken(ServerHttpRequest request) {
        String token = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("token");
        if (StringUtils.isBlank(token)) {
            HttpHeaders headers = request.getHeaders();
            token = headers.getFirst(AuthConstant.TOKEN_KEY);
        }
        return token;
    }
}
