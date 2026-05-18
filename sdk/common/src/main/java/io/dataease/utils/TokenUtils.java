package io.dataease.utils;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.auth.config.SubstituleLoginConfig;
import io.dataease.exception.DEException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

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
        Object apisixTokenManage = CommonBeanFactory.getBean("apisixTokenManage");
        if (ObjectUtils.isNotEmpty(apisixTokenManage)) {
            return validateByApisixTokenManage(apisixTokenManage, token);
        }
        TokenUserBO userBO = userBOByToken(token);
        validateSubstituteToken(token, userBO);
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

    private static TokenUserBO validateByApisixTokenManage(Object apisixTokenManage, String token) {
        Object tokenBO = invokeMethod(apisixTokenManage, "validate", new Class[]{String.class}, token);
        if (ObjectUtils.isEmpty(tokenBO)) {
            DEException.throwException("token is invalid");
        }
        Long userId = (Long) invokeMethod(tokenBO, "getUserId", null);
        Long defaultOid = (Long) invokeMethod(tokenBO, "getDefaultOid", null);
        return new TokenUserBO(userId, defaultOid);
    }

    private static void validateSubstituteToken(String token, TokenUserBO userBO) {
        String secret = SubstituleLoginConfig.getTokenSecret();
        if (StringUtils.isBlank(secret)) {
            DEException.throwException("token is invalid");
        }
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Verification verification = JWT.require(algorithm).withClaim("uid", userBO.getUserId()).withClaim("oid", userBO.getDefaultOid());
        JWTVerifier verifier = verification.build();
        verifier.verify(token);
    }

    private static Object invokeMethod(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = parameterTypes == null ? target.getClass().getMethod(methodName) : target.getClass().getMethod(methodName, parameterTypes);
            return method.invoke(target, args);
        } catch (Exception e) {
            DEException.throwException("token is invalid");
            return null;
        }
    }
}
