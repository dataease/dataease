package io.dataease.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.auth.config.SubstituleLoginConfig;
import io.dataease.exception.DEException;
import io.dataease.license.utils.LicenseUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

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
        TokenUserBO userBO = userBOByToken(token);
        if (ObjectUtils.isEmpty(userBO) || LicenseUtil.licenseValid()) {
            return userBO;
        }
        Long userId = userBO.getUserId();
        String secret = null;
        if (ObjectUtils.isEmpty(CommonBeanFactory.getBean("loginServer"))) {
            String pwd = SubstituleLoginConfig.getPwd();
            secret = Md5Utils.md5(pwd);
        } else {
            Object apisixTokenManage = CommonBeanFactory.getBean("apisixTokenManage");
            Method method = DeReflectUtil.findMethod(apisixTokenManage.getClass(), "userCacheBO");
            Object o = ReflectionUtils.invokeMethod(method, apisixTokenManage, userId);
            Method pwdMethod = DeReflectUtil.findMethod(o.getClass(), "getPwd");
            Object pwdObj = ReflectionUtils.invokeMethod(pwdMethod, o);
            secret = pwdObj.toString();
        }
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Verification verification = JWT.require(algorithm).withClaim("uid", userId).withClaim("oid", userBO.getDefaultOid());
        JWTVerifier verifier = verification.build();
        DecodedJWT decode = JWT.decode(token);
        algorithm.verify(decode);
        verifier.verify(token);
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
