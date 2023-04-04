package io.dataease.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.exception.DEException;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.env.Environment;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TokenUtils {

    private static Long expireTime;
    public static String generate(TokenUserBO bo, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Long userId = bo.getUserId();
        Long defaultOid = bo.getDefaultOid();
        Date expDate = new Date(System.currentTimeMillis() + getExpireTime(TimeUnit.MILLISECONDS));
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("uid", userId).withClaim("oid", defaultOid);
        String token = builder.withExpiresAt(expDate).sign(algorithm);
        return token;
    }

    public static TokenUserBO userBOByToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        Long userId = jwt.getClaim("uid").asLong();
        Long oid = jwt.getClaim("oid").asLong();
        if (ObjectUtils.isEmpty(userId)) {
            DEException.throwException("token格式错误！");
        }
        return new TokenUserBO(userId, oid);
    }

    public static boolean verify(String token, TokenUserBO bo, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Verification verification = JWT.require(algorithm)
                .withClaim("userId", bo.getUserId());
        JWTVerifier verifier = verification.build();
        DecodedJWT decode = JWT.decode(token);
        algorithm.verify(decode);
        verifier.verify(token);
        return true;
    }

    public static Long getExpireTime(TimeUnit unit) {
        if (ObjectUtils.isEmpty(expireTime)) {
            expireTime = CommonBeanFactory.getBean(Environment.class).getProperty("dataease.login_timeout", Long.class, 480L);
        }
        if (ObjectUtils.isEmpty(unit) || unit == TimeUnit.MINUTES) {
            return expireTime;
        }
        if (unit == TimeUnit.SECONDS) {
            return expireTime * 60L;
        }
        if (unit == TimeUnit.MILLISECONDS) {
            return expireTime * 60000L;
        }
        DEException.throwException("Unsupported timeUnit");
        return null;
    }
}
