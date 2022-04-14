package io.dataease.websocket.factory;

import io.dataease.websocket.util.WsUtil;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;

public class DeWebSocketHandlerDecorator extends WebSocketHandlerDecorator {


    public DeWebSocketHandlerDecorator(WebSocketHandler delegate) {
        super(delegate);
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String name = session.getPrincipal().getName();
        Long userId = Long.parseLong(name);
        WsUtil.onLine(userId);
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String name = session.getPrincipal().getName();
        Long userId = Long.parseLong(name);
        WsUtil.offLine(userId);
        super.afterConnectionClosed(session, closeStatus);
    }

}
