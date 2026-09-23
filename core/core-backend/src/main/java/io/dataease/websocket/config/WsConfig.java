package io.dataease.websocket.config;

import io.dataease.websocket.factory.DeWsHandlerFactory;
import io.dataease.websocket.handler.PrincipalHandshakeHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WsConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${dataease.cors-strict:false}")
    private boolean corsStrict;

    @Value("#{'${dataease.origin-list:}'.split(',')}")
    private List<String> originList;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        StompWebSocketEndpointRegistration registration = registry.addEndpoint("/websocket")
                .setHandshakeHandler(new PrincipalHandshakeHandler());
        if (corsStrict) {
            List<String> allowedOrigins = originList == null ? List.of() : originList.stream()
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .distinct()
                    .toList();
            if (!allowedOrigins.isEmpty()) {
                registration.setAllowedOrigins(allowedOrigins.toArray(new String[0]));
            }
        } else {
            registration.setAllowedOriginPatterns("*");
        }
        registration.withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/user");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.addDecoratorFactory(new DeWsHandlerFactory());
        registry.setMessageSizeLimit(8192) //设置消息字节数大小
                .setSendBufferSizeLimit(8192)//设置消息缓存大小
                .setSendTimeLimit(10000); //设置消息发送时间限制毫秒
    }
}
