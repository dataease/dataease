package io.dataease.xpack.permissions.apisix.manage;

import io.dataease.exception.DEException;
import io.dataease.utils.CacheUtils;
import io.dataease.utils.ServletUtils;
import io.dataease.utils.WhitelistUtils;
import io.dataease.xpack.permissions.apisix.dao.mapper.DeTokenMapper;
import io.dataease.xpack.permissions.bo.TokenBO;
import io.dataease.xpack.permissions.utils.PerTokenUtils;
import io.dataease.xpack.permissions.utils.TokenCacheUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ApisixTokenManage {


    @Resource
    private DeTokenMapper deTokenMapper;


    public boolean needAuth() {
        HttpServletRequest request = ServletUtils.request();
        String path = request.getHeader("X-Forwarded-Uri");
        return !WhitelistUtils.match(path);
    }

    public TokenBO validate(String token) {
        if (StringUtils.isBlank(token)) {
            String uri = ServletUtils.request().getRequestURI();
            DEException.throwException("token is empty for uri {" + uri + "}");
        }
        TokenBO tokenBO = PerTokenUtils.boByToken(token);
        Long userId = tokenBO.getUserId();
        if (PerTokenUtils.timeExp(tokenBO)) {
            DEException.throwException("token is Expired");
        }
        if (!TokenCacheUtils.tokenValid(userId, token)) {
            DEException.throwException("token is destroyed");
        }
        String secret = secret(userId);
        String newToken = PerTokenUtils.refreshTemp(tokenBO, secret);
        if (StringUtils.isNotBlank(newToken)) {
            TokenCacheUtils.delayDel(userId, token);
            token = newToken;
        }
        PerTokenUtils.verify(token, tokenBO, secret);
        return tokenBO;
    }

    private String secret(Long uid) {
        Object userPwd = CacheUtils.get("user_pwd", uid.toString());
        if (ObjectUtils.isNotEmpty(userPwd)) {
            return userPwd.toString();
        }
        String secret = deTokenMapper.secret(uid);
        CacheUtils.put("user_pwd", uid.toString(), secret);
        return secret;
    }
}
