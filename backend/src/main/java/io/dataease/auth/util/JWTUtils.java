package io.dataease.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.filter.JWTFilter;
import io.dataease.commons.utils.CommonBeanFactory;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import java.util.Date;


public class JWTUtils {


    // token过期时间1min (过期会自动刷新续命 目的是避免一直都是同一个token )
    private static final long EXPIRE_TIME = 5*60*1000;
    // 登录间隔时间10min 超过这个时间强制重新登录
    private static final long Login_Interval = 30*60*1000;


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
        verifier.verify(token);
        if (loginExpire(token)){
            // 登录超时
            throw new AuthenticationException(JWTFilter.expireMessage);
            // 前端拦截 登录超时状态 直接logout
            //return false;
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

    /**
     * 当前token是否登录超时
     * @param token
     * @return
     */
    public static boolean loginExpire(String token){
        Long now = System.currentTimeMillis();
        Long lastOperateTime = tokenLastOperateTime(token);
        if (lastOperateTime == null) return true;
        return now - lastOperateTime > Login_Interval;
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

    public static String signLink(String resourceId, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.create().withClaim("resourceId", resourceId).sign(algorithm);
    }

    public static boolean verifyLink(String token,String resourceId, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
                .withClaim("resourceId", resourceId)
                .build();
        try {
            verifier.verify(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 获取当前token上次操作时间
     * @param token
     * @return
     */
    public static Long tokenLastOperateTime(String token){
        CacheManager cacheManager = CommonBeanFactory.getBean(CacheManager.class);
        Cache tokens_expire = cacheManager.getCache("tokens_expire");
        Long expTime = tokens_expire.get(token, Long.class);
        return expTime;
    }

    public static void removeTokenExpire(String token){
        CacheManager cacheManager = CommonBeanFactory.getBean(CacheManager.class);
        Cache tokens_expire = cacheManager.getCache("tokens_expire");
        tokens_expire.evict(token);
    }

    public static void addTokenExpire(String token){
        CacheManager cacheManager = CommonBeanFactory.getBean(CacheManager.class);
        Cache tokens_expire = cacheManager.getCache("tokens_expire");
        long now = System.currentTimeMillis();
        tokens_expire.put(token, now);
    }
}
