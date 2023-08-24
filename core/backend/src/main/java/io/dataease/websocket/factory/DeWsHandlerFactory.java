package io.dataease.websocket.factory;

import org.springframework.web.socket.WebSocketHandler;

import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

public class DeWsHandlerFactory implements WebSocketHandlerDecoratorFactory {


    @Override
    public WebSocketHandler decorate(WebSocketHandler webSocketHandler) {
        return new DeWebSocketHandlerDecorator(webSocketHandler);
    }
}
