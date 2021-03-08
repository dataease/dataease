package io.dataease.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.commons.utils.ServletUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;


public class JWTUtils {


    // token过期时间5min (过期会自动刷新续命 目的是避免一直都是同一个token )
    private static final long EXPIRE_TIME = 1*60*1000;
    // 登录间隔时间10min 超过这个时间强制重新登录
    private static final long Login_Interval = 10*60*1000;


    /**
     * 校验token是否正确
     * @param token 密钥
     * @param secret 用户的密码
     * @return 是否正确
     */
    public static boolean verify(String token, TokenInfo tokenInfo, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
                .withClaim("lastLoginTime", tokenInfo.getLastLoginTime())
                .withClaim("username", tokenInfo.getUsername())
                .withClaim("userId", tokenInfo.getUserId())
                .build();
        DecodedJWT jwt = verifier.verify(token);
        Long lastLoginTime = jwt.getClaim("lastLoginTime").asLong();
        long now = System.currentTimeMillis();
        if (now - lastLoginTime > Login_Interval){
            // 登录超时
            HttpServletResponse response = ServletUtils.response();
            response.addHeader("Access-Control-Expose-Headers", "authentication-status");
            response.setHeader("authentication-status", "login_expire");
            // 前端拦截 登录超时状态 直接logout
            return false;
        }
        return true;
    }

    /**
     * 获得token中的信息无需secret解密也能获得
     * @return token中包含的用户名
     */
    public static TokenInfo tokenInfoByToken(String token) {
        DecodedJWT jwt = JWT.decode(token);
        String username = jwt.getClaim("username").asString();
        Long userId = jwt.getClaim("userId").asLong();
        Long lastLoginTime = jwt.getClaim("lastLoginTime").asLong();
        if (StringUtils.isEmpty(username) || ObjectUtils.isEmpty(userId) || ObjectUtils.isEmpty(lastLoginTime)){
            throw new RuntimeException("token格式错误！");
        }
        TokenInfo tokenInfo = TokenInfo.builder().username(username).userId(userId).lastLoginTime(lastLoginTime).build();
        return tokenInfo;
    }





    public static boolean needRefresh(String token){
        Date exp = JWTUtils.getExp(token);
        return new Date().getTime() >= exp.getTime();
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
     * @param tokenInfo 用户信息
     * @param secret 用户的密码
     * @return 加密的token
     */
    public static String sign(TokenInfo tokenInfo, String secret) {
        try {
            Date date = new Date(System.currentTimeMillis()+EXPIRE_TIME);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            // 附带username信息
            return JWT.create()
                    .withClaim("lastLoginTime", tokenInfo.getLastLoginTime())
                    .withClaim("username", tokenInfo.getUsername())
                    .withClaim("userId", tokenInfo.getUserId())
                    .withClaim("exp", date)
                    .withExpiresAt(date)
                    .sign(algorithm);
        } catch (Exception e) {
            return null;
        }
    }
}
