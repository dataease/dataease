package io.dataease.websocket.handler;

import io.dataease.websocket.entity.DePrincipal;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class PrincipalHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Object userId = attributes.get(DeHandshakeInterceptor.AUTH_USER_ID_ATTRIBUTE);
        if (userId instanceof Long authenticatedUserId) {
            return new DePrincipal(authenticatedUserId.toString());
        }
        return null;
    }
}
