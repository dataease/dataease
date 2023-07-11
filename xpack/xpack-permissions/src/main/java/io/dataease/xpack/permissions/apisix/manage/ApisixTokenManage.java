package io.dataease.xpack.permissions.apisix.manage;

import io.dataease.auth.bo.TokenUserBO;
import io.dataease.exception.DEException;
import io.dataease.utils.CacheUtils;
import io.dataease.utils.ServletUtils;
import io.dataease.utils.TokenUtils;
import io.dataease.utils.WhitelistUtils;
import io.dataease.xpack.permissions.apisix.dao.mapper.DeTokenMapper;
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

    public TokenUserBO validate(String token) {
        if (StringUtils.isBlank(token)) {
            String uri = ServletUtils.request().getRequestURI();
            DEException.throwException("token is empty for uri {" + uri + "}");
        }
        TokenUserBO tokenUserBO = TokenUtils.userBOByToken(token);
        String secret = secret(tokenUserBO.getUserId());
        TokenUtils.verify(token, tokenUserBO, secret);
        return tokenUserBO;
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
