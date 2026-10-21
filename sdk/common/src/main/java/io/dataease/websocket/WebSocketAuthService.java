package io.dataease.websocket;

/**
 * WebSocket 握手认证契约：把客户端提交的 token 解析为服务端已认证的用户 ID。
 * 各部署形态提供自己的实现，握手层不再信任客户端声明的 userId。
 */
public interface WebSocketAuthService {

    /**
     * @return 认证通过时返回用户 ID；否则返回 null（由握手层拒绝连接）
     */
    Long authenticate(String token);
}
