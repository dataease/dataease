package io.dataease.auth.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.google.gson.Gson;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.entity.TokenInfo.TokenInfoBuilder;
import io.dataease.commons.model.OnlineUserModel;
import io.dataease.commons.utils.*;
import io.dataease.exception.DataEaseException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

public class JWTUtils {


    private static Long expireTime;

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

        verifySign(algorithm, token);
        verifier.verify(token);
        return true;
    }

    public static void verifySign(Algorithm algorithm, String token) {
        DecodedJWT decode = JWT.decode(token);
        algorithm.verify(decode);
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

    /**
     * @param tokenInfo 用户信息
     * @param secret    用户的密码
     * @return 加密的token
     */
    public static String sign(TokenInfo tokenInfo, String secret) {
        return sign(tokenInfo, secret, true);
    }

    private static boolean tokenValid(OnlineUserModel model) {
        String token = model.getToken();
        // 如果已经加入黑名单 则直接返回无效
        boolean invalid = TokenCacheUtils.invalid(token);
        if (invalid) return false;

        Long loginTime = model.getLoginTime();
        if (ObjectUtils.isEmpty(expireTime)) {
            expireTime = CommonBeanFactory.getBean(Environment.class).getProperty("dataease.login_timeout", Long.class, 480L);
        }
        long expireTimeMillis = expireTime * 60000L;
        // 如果当前时间减去登录时间小于超时时间则说明token未过期 返回有效状态
        return System.currentTimeMillis() - loginTime < expireTimeMillis;

    }

    private static String models2Json(OnlineUserModel model, boolean withCurToken, String token) {
        Set<OnlineUserModel> models = new LinkedHashSet<>();
        models.add(model);
        Gson gson = new Gson();
        List<OnlineUserModel> userModels = models.stream().map(item -> {
            item.setToken(null);
            return item;
        }).collect(Collectors.toList());
        if (withCurToken) {
            userModels.get(0).setToken(token);
        }
        String json = gson.toJson(userModels);
        try {
            return URLEncoder.encode(json, "utf-8");
        } catch (Exception e) {
            return null;
        }
    }

    public static String seizeSign(Long userId, String token) {
        Optional.ofNullable(TokenCacheUtils.onlineUserToken(userId)).ifPresent(model -> TokenCacheUtils.add(model.getToken(), userId));
        TokenCacheUtils.add2OnlinePools(token, userId);
        return IPUtils.get();
    }

    public static String sign(TokenInfo tokenInfo, String secret, boolean writeOnline) {

        Long userId = tokenInfo.getUserId();
        String multiLoginType = null;
        if (writeOnline && StringUtils.equals("1", (multiLoginType = TokenCacheUtils.multiLoginType()))) {
            OnlineUserModel userModel = TokenCacheUtils.onlineUserToken(userId);
            if (ObjectUtils.isNotEmpty(userModel) && tokenValid(userModel)) {
                HttpServletResponse response = ServletUtils.response();
                Cookie cookie_token = new Cookie("MultiLoginError1", models2Json(userModel, false, null));
                cookie_token.setPath("/");
                cookie_token.setPath("/");
                response.addCookie(cookie_token);
                DataEaseException.throwException("MultiLoginError1");
            }
        }
        if (ObjectUtils.isEmpty(expireTime)) {
            expireTime = CommonBeanFactory.getBean(Environment.class).getProperty("dataease.login_timeout", Long.class, 480L);
        }
        long expireTimeMillis = expireTime * 60000L;
        Date date = new Date(System.currentTimeMillis() + expireTimeMillis);
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Builder builder = JWT.create()
                .withClaim("username", tokenInfo.getUsername())
                .withClaim("userId", userId);
        String sign = builder.withExpiresAt(date).sign(algorithm);

        if (StringUtils.equals("2", multiLoginType)) {
            OnlineUserModel userModel = TokenCacheUtils.onlineUserToken(userId);
            if (ObjectUtils.isNotEmpty(userModel) && tokenValid(userModel)) {
                HttpServletResponse response = ServletUtils.response();
                Cookie cookie_token = new Cookie("MultiLoginError2", models2Json(userModel, true, sign));
                cookie_token.setPath("/");
                response.addCookie(cookie_token);
                DataEaseException.throwException("MultiLoginError");
            }
        }
        if (writeOnline && !StringUtils.equals("0", multiLoginType)) {
            TokenCacheUtils.add2OnlinePools(sign, userId);
        }
        return sign;
    }


    public static String signLink(String resourceId, Long userId, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        if (userId == null) {
            return JWT.create().withClaim("resourceId", resourceId).sign(algorithm);
        } else {
            return JWT.create().withClaim("resourceId", resourceId).withClaim("userId", userId).sign(algorithm);
        }
    }

    public static boolean verifyLink(String token, String resourceId, Long userId, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier;
        if (userId == null) {
            verifier = JWT.require(algorithm).withClaim("resourceId", resourceId).build();
        } else {
            verifier = JWT.require(algorithm).withClaim("resourceId", resourceId).withClaim("userId", userId).build();
        }
        try {
            verifier.verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
