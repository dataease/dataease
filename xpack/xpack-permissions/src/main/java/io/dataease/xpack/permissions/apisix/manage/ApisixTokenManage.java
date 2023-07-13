package io.dataease.xpack.permissions.apisix.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.constant.AuthConstant;
import io.dataease.exception.DEException;
import io.dataease.utils.CacheUtils;
import io.dataease.utils.ServletUtils;
import io.dataease.utils.WhitelistUtils;
import io.dataease.xpack.permissions.apisix.dao.auto.entity.PerEmbeddedInstance;
import io.dataease.xpack.permissions.apisix.dao.auto.mapper.PerEmbeddedInstanceMapper;
import io.dataease.xpack.permissions.apisix.dao.ext.DeTokenMapper;
import io.dataease.xpack.permissions.bo.EmbeddedToken;
import io.dataease.xpack.permissions.bo.TokenBO;
import io.dataease.xpack.permissions.utils.EmbeddedTokenUtils;
import io.dataease.xpack.permissions.utils.PerTokenUtils;
import io.dataease.xpack.permissions.utils.TokenCacheUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ApisixTokenManage {


    @Resource
    private DeTokenMapper deTokenMapper;

    @Resource
    private PerEmbeddedInstanceMapper perEmbeddedInstanceMapper;


    public boolean needAuth() {
        HttpServletRequest request = ServletUtils.request();
        String path = request.getHeader("X-Forwarded-Uri");
        return !WhitelistUtils.match(path);
    }

    public TokenBO validate(String token) {
        if (StringUtils.isBlank(token)) {
            String embeddedTokenStr = ServletUtils.request().getHeader(AuthConstant.EMBEDDED_TOKEN_KEY);
            if (StringUtils.isBlank(embeddedTokenStr)) {
                String uri = ServletUtils.request().getRequestURI();
                DEException.throwException("token is empty for uri {" + uri + "}");
            }
            token = transformToken(embeddedTokenStr);
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

    public String transformToken(String embeddedTokenStr) {
        EmbeddedToken embeddedToken = EmbeddedTokenUtils.analysisEmbeddedToken(embeddedTokenStr);
        PerEmbeddedInstance instance = getInstance(embeddedToken.getAppId());
        EmbeddedTokenUtils.verifyEmbedded(embeddedTokenStr, embeddedToken, instance.getAppSecret());
        // embeddedToken换取deToken
        TokenUserBO tokenUserBO = queryUserBO(embeddedToken.getAccount());
        String token = PerTokenUtils.generate(tokenUserBO, secret(tokenUserBO.getUserId()));
        HttpServletResponse response = ServletUtils.response();
        response.addHeader(AuthConstant.REFRESH_TOKEN_KEY, token);
        return token;
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

    private PerEmbeddedInstance getInstance(String appId) {

        QueryWrapper<PerEmbeddedInstance> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id", appId);
        PerEmbeddedInstance instance = perEmbeddedInstanceMapper.selectOne(queryWrapper);
        return instance;
    }

    private TokenUserBO queryUserBO(String account) {
        TokenUserBO tokenUserBO = deTokenMapper.convertFromAccount(account);
        return tokenUserBO;
    }
}
