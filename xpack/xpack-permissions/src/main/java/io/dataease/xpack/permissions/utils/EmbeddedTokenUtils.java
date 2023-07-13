package io.dataease.xpack.permissions.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import io.dataease.xpack.permissions.bo.EmbeddedToken;

public class EmbeddedTokenUtils {

    public static EmbeddedToken analysisEmbeddedToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        String account = jwt.getClaim("account").asString();
        String appId = jwt.getClaim("appId").asString();
        return new EmbeddedToken(account, appId);
    }

    public static boolean validateDomain(String domain, String appId) {
        return true;
    }

    public static boolean verifyEmbedded(String token, EmbeddedToken bo, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Verification verification = JWT.require(algorithm).withClaim("account", bo.getAccount()).withClaim("appId", bo.getAppId());
        JWTVerifier verifier = verification.build();
        DecodedJWT decode = JWT.decode(token);
        algorithm.verify(decode);
        verifier.verify(token);
        return true;
    }


}
