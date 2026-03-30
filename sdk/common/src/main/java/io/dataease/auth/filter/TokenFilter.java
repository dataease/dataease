package io.dataease.auth.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.constant.AuthConstant;
import io.dataease.exception.DEException;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.result.ResultMessage;
import io.dataease.utils.*;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TokenFilter implements Filter {
    private static final String headName = "DE-GATEWAY-FLAG";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String method = request.getMethod();
        if (!StringUtils.equalsAny(method, "GET", "POST", "OPTIONS", "DELETE")) {
            HttpServletResponse res = (HttpServletResponse) servletResponse;
            res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        if (StringUtils.equalsIgnoreCase("OPTIONS", method)) {
            String origin = request.getHeader("Origin");
            if (StringUtils.isBlank(origin)) {
                HttpServletResponse res = (HttpServletResponse) servletResponse;
                res.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String requestURI = request.getRequestURI();

        boolean match = false;
        try {
            match = WhitelistUtils.match(requestURI);
        } catch (DEException e) {
            HttpServletResponse res = (HttpServletResponse) servletResponse;
            ResultMessage resultMessage = new ResultMessage(e.getCode(), e.getMessage());
            ResponseEntity<ResultMessage> entity = new ResponseEntity<>(resultMessage, HttpStatus.UNAUTHORIZED);
            sendResponseEntity(res, entity);
            LogUtil.error(e.getMessage(), e);
            return;
        }
        if (match) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        try {
            if (ModelUtils.isDesktop()) {
                UserUtils.setDesktopUser();
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            String executeVersion = null;
            if (StringUtils.isNotBlank(executeVersion = VersionUtil.getRandomVersion())) {
                Objects.requireNonNull(ServletUtils.response()).addHeader(AuthConstant.DE_EXECUTE_VERSION, executeVersion);
            }
            String linkToken = ServletUtils.getHead(AuthConstant.LINK_TOKEN_KEY);
            if (StringUtils.isNotBlank(linkToken)) {
                if (StringUtils.length(linkToken) < 100) {
                    DEException.throwException("token is invalid");
                }
                DecodedJWT jwt = JWT.decode(linkToken);
                Long userId = jwt.getClaim("uid").asLong();
                Long oid = jwt.getClaim("oid").asLong();
                Long resourceId = jwt.getClaim("resourceId").asLong();
                if (ObjectUtils.isEmpty(userId)) {
                    DEException.throwException("link token格式错误！");
                }

                Object shareSecretManage = CommonBeanFactory.getBean("shareSecretManage");
                Method getSecretMethod = DeReflectUtil.findMethod(shareSecretManage.getClass(), "getSecret");
                Object pwdObj = ReflectionUtils.invokeMethod(getSecretMethod, shareSecretManage, resourceId, userId);
                String linkSecret = pwdObj.toString();

                Algorithm algorithm = Algorithm.HMAC256(linkSecret);
                Verification verification = JWT.require(algorithm);
                JWTVerifier verifier = verification.build();
                DecodedJWT decode = JWT.decode(linkToken);
                algorithm.verify(decode);
                verifier.verify(linkToken);

                UserUtils.setUserInfo(new TokenUserBO(userId, oid));
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            String token = ServletUtils.getToken();
            TokenUserBO userBO = TokenUtils.validate(token);
            UserUtils.setUserInfo(userBO);
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception e) {
            if (!LicenseUtil.licenseValid()) {
                HttpServletResponse res = (HttpServletResponse) servletResponse;
                ResultMessage resultMessage = new ResultMessage(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
                HttpHeaders headers = new HttpHeaders();
                String msg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8).replace("+", "%20");
                headers.add(headName, msg);
                ResponseEntity<ResultMessage> entity = new ResponseEntity<>(resultMessage, headers, HttpStatus.UNAUTHORIZED);
                sendResponseEntity(res, entity);
                LogUtil.error(e.getMessage(), e);
            } else {
                throw e;
            }
        } finally {
            UserUtils.removeUser();
        }
    }

    private void sendResponseEntity(HttpServletResponse httpResponse, ResponseEntity<ResultMessage> responseEntity) throws IOException {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        httpResponse.setStatus(statusCode.value());
        httpResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        HttpHeaders headers = responseEntity.getHeaders();
        if (ObjectUtils.isNotEmpty(headers)) {
            headers.forEach((key, value) -> httpResponse.addHeader(key, value.toString()));
        }
        httpResponse.getWriter().write(Objects.requireNonNull(JsonUtil.toJSONString(responseEntity.getBody()).toString()));
    }

}
