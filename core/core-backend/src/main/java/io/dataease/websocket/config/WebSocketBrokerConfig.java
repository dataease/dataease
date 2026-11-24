package io.dataease.websocket.config;

import io.dataease.websocket.WebSocketAuthService;
import io.dataease.websocket.auth.CommunityWebSocketAuthService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

/**
 * WebSocket 消息代理开关。
 * 与 WsConfig（WebSocketMessageBrokerConfigurer）分离，避免配置类实现 Configurer
 * 时 @EnableWebSocketMessageBroker 导入 DelegatingWebSocketMessageBrokerConfiguration
 * 形成配置类循环依赖。
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketBrokerConfig {

    @Bean
    @ConditionalOnMissingBean(WebSocketAuthService.class)
    public WebSocketAuthService webSocketAuthService() {
        return new CommunityWebSocketAuthService();
    }
}
