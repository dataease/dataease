package io.dataease.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.entity.TokenInfo.TokenInfoBuilder;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.exception.DataEaseException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import java.util.Date;


public class JWTUtils {


    // token过期时间1min (过期会自动刷新续命 目的是避免一直都是同一个token )
    private static final long EXPIRE_TIME = 1 * 60 * 1000;
    // 登录间隔时间10min 超过这个时间强制重新登录
    private static long Login_Interval;


    /**
     * 校验token是否正确
     *
     * @param token  密钥
     * @param secret 用户的密码
     * @return 是否正确
     */
    public static boolean verify(String token, TokenInfo tokenInfo, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Verification verification = JWT.require(algorithm)
                .withClaim("username", tokenInfo.getUsername())
                .withClaim("userId", tokenInfo.getUserId());
        JWTVerifier verifier = verification.build();
        verifier.verify(token);
        return true;
    }

    /**
     * 获得token中的信息无需secret解密也能获得
     *
     * @return token中包含的用户名
     */
    public static TokenInfo tokenInfoByToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        String username = jwt.getClaim("username").asString();
        Long userId = jwt.getClaim("userId").asLong();
        if (StringUtils.isEmpty(username) || ObjectUtils.isEmpty(userId)) {
            DataEaseException.throwException("token格式错误！");
        }
        TokenInfoBuilder tokenInfoBuilder = TokenInfo.builder().username(username).userId(userId);
        return tokenInfoBuilder.build();
    }

    public static boolean needRefresh(String token) {
        Date exp = JWTUtils.getExp(token);
        return new Date().getTime() >= exp.getTime();
    }

    /**
     * 当前token是否登录超时
     *
     * @param token
     * @return
     */
    public static boolean loginExpire(String token) {
        if (Login_Interval == 0) {
            // 默认超时时间是8h
            int minute = CommonBeanFactory.getBean(Environment.class).getProperty("dataease.login_timeout", Integer.class, 8 * 60);
            // 分钟换算成毫秒
            Login_Interval = minute * 1000 * 60;
        }
        Long lastOperateTime = tokenLastOperateTime(token);
        boolean isExpire = true;
        if (lastOperateTime != null) {
            Long now = System.currentTimeMillis();
            isExpire = now - lastOperateTime > Login_Interval;
        }
        return isExpire;
    }

    public static Date getExp(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("exp").asDate();
        } catch (JWTDecodeException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 生成签名,5min后过期
     *
     * @param tokenInfo 用户信息
     * @param secret    用户的密码
     * @return 加密的token
     */
    public static String sign(TokenInfo tokenInfo, String secret) {
        try {
            Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            Builder builder = JWT.create()
                    .withClaim("username", tokenInfo.getUsername())
                    .withClaim("userId", tokenInfo.getUserId());
            return builder.withExpiresAt(date).sign(algorithm);

        } catch (Exception e) {
            return null;
        }
    }

    public static String signLink(String resourceId, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create().withClaim("resourceId", resourceId).sign(algorithm);
    }

    public static boolean verifyLink(String token, String resourceId, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
                .withClaim("resourceId", resourceId)
                .build();
        try {
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取当前token上次操作时间
     *
     * @param token
     * @return
     */
    public static Long tokenLastOperateTime(String token) {
        DecodedJWT jwt = JWT.decode(token);
        Date expiresAt = jwt.getExpiresAt();
        return expiresAt.getTime();
    }

}
