package io.dataease.share.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class LinkTokenUtil {
    private static final String defaultPwd = "link-pwd-fit2cloud";
    public static String generate(Long uid, Long resourceId, Long exp, String pwd, Long oid) {
        pwd = StringUtils.isBlank(pwd) ? defaultPwd : pwd;
        Algorithm algorithm = Algorithm.HMAC256(pwd);
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("uid", uid).withClaim("resourceId", resourceId).withClaim("oid", oid);
        if (ObjectUtils.isNotEmpty(exp) && !exp.equals(0L)) {
            builder = builder.withExpiresAt(new Date(exp));
        }
        return builder.sign(algorithm);
    }
}
