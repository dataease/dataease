package io.dataease.websocket;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(value = "/socket", configurator = ServerEndpointConfigurator.class)
@Component
public class WebSocketServer {
    @OnOpen
    public void onOpen(Session session) throws IOException {
        // Get session and WebSocket connection
//        System.out.println("open: " + session.isOpen());
//        System.out.println("open: " + SessionUtils.getUser());
//        System.out.println("open: " + session.getUserProperties().get("user"));
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        // Handle new messages
//        System.out.println(message);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        // WebSocket connection closes
//        System.out.println("close: " + session.isOpen());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        // Do error handling here
        throwable.printStackTrace();
    }
}
