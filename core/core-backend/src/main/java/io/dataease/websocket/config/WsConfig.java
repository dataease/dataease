package io.dataease.websocket.config;

import io.dataease.auth.interceptor.CorsConfig;
import io.dataease.websocket.WebSocketAuthService;
import io.dataease.websocket.auth.CommunityWebSocketAuthService;
import io.dataease.websocket.factory.DeWsHandlerFactory;
import io.dataease.websocket.handler.DeHandshakeInterceptor;
import io.dataease.websocket.handler.PrincipalHandshakeHandler;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WsConfig implements WebSocketMessageBrokerConfigurer {

    @Resource
    private WebSocketAuthService webSocketAuthService;

    @Resource
    private CorsConfig corsConfig;

    @Bean
    @ConditionalOnMissingBean(WebSocketAuthService.class)
    public WebSocketAuthService webSocketAuthService() {
        return new CommunityWebSocketAuthService();
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        List<String> allowedOriginPatterns = corsConfig.isCorsStrict()
                ? corsConfig.getAllOrigins()
                : List.of("*");
        registry.addEndpoint("/websocket")
                .setAllowedOriginPatterns(allowedOriginPatterns.toArray(new String[0]))
                .setHandshakeHandler(new PrincipalHandshakeHandler())
                .addInterceptors(new DeHandshakeInterceptor(webSocketAuthService, allowedOriginPatterns))
                .withSockJS();
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
