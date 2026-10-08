package io.dataease.websocket.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.dataease.auth.config.SubstituleLoginConfig;
import io.dataease.utils.LocalModelUtils;
import io.dataease.utils.Md5Utils;
import io.dataease.websocket.WebSocketAuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * 社区/桌面版默认实现：desktop 本地单用户放行 uid=1；
 * 其他社区部署校验 admin 登录 JWT（secret 与 SubstituleLoginServer 保持一致）。
 */
@Component
@ConditionalOnMissingBean(WebSocketAuthService.class)
public class CommunityWebSocketAuthService implements WebSocketAuthService {

    @Override
    public Long authenticate(String token) {
        if (LocalModelUtils.isDesktop()) {
            return 1L;
        }
        if (StringUtils.isBlank(token)) {
            return null;
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(Md5Utils.md5(SubstituleLoginConfig.getPwd()));
            DecodedJWT jwt = JWT.require(algorithm).withClaim("uid", 1L).build().verify(token);
            Long userId = jwt.getClaim("uid").asLong();
            return Long.valueOf(1L).equals(userId) ? userId : null;
        } catch (Exception e) {
            return null;
        }
    }
}
