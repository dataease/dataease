package io.dataease.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.auth.config.SubstituleLoginConfig;
import io.dataease.exception.DEException;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.model.DeModel;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Objects;

public class TokenUtils {

    public static TokenUserBO userBOByToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        Long userId = jwt.getClaim("uid").asLong();
        Long oid = jwt.getClaim("oid").asLong();
        if (ObjectUtils.isEmpty(userId)) {
            DEException.throwException("token格式错误！");
        }
        return new TokenUserBO(userId, oid);
    }

    public static TokenUserBO validate(String token) {
        if (StringUtils.isBlank(token)) {
            String uri = ServletUtils.request().getRequestURI();
            DEException.throwException("token is empty for uri {" + uri + "}");
        }
        if (StringUtils.length(token) < 100) {
            DEException.throwException("token is invalid");
        }
        return userBOByToken(token);
    }

    public static TokenUserBO validateBusinessToken(String token) {
        Object tokenManage = CommonBeanFactory.getBean("apisixTokenManage");
        if (tokenManage != null) {
            Method method = ReflectionUtils.findMethod(tokenManage.getClass(), "validateBusinessToken", String.class);
            TokenUserBO userBO = (TokenUserBO) ReflectionUtils.invokeMethod(Objects.requireNonNull(method), tokenManage, token);
            if (userBO == null || userBO.getUserId() == null) {
                DEException.throwException("token is invalid");
            }
            return userBO;
        }
        // Missing enterprise authentication must not fall back to the community signing key.
        if (ModelUtils.get() == DeModel.DISTRIBUTED || LicenseUtil.licenseValid()
                || CommonBeanFactory.getBean("loginServer") != null) {
            DEException.throwException("token validation is unavailable");
        }
        TokenUserBO userBO = validate(token);
        JWT.require(Algorithm.HMAC256(SubstituleLoginConfig.getTokenSecret()))
                .withClaim("uid", userBO.getUserId())
                .withClaim("oid", userBO.getDefaultOid())
                .build().verify(token);
        return userBO;
    }

    public static TokenUserBO validateLinkToken(String linkToken) {
        if (StringUtils.isBlank(linkToken)) {
            String uri = ServletUtils.request().getRequestURI();
            DEException.throwException("link token is empty for uri {" + uri + "}");
        }
        if (StringUtils.length(linkToken) < 100) {
            DEException.throwException("token is invalid");
        }
        DecodedJWT jwt = JWT.decode(linkToken);
        Long userId = jwt.getClaim("uid").asLong();
        Long oid = jwt.getClaim("oid").asLong();
        if (ObjectUtils.isEmpty(userId)) {
            DEException.throwException("link token格式错误！");
        }
        return new TokenUserBO(userId, oid);
    }
}
