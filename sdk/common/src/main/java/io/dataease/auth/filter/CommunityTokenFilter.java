package io.dataease.auth.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.auth.config.SubstituleLoginConfig;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.utils.*;
import jakarta.servlet.*;
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

public class CommunityTokenFilter implements Filter {

    private static final String headName = "DE-GATEWAY-FLAG";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Long userId = null;
        String token = ServletUtils.getToken();
        TokenUserBO userBO = null;
        if (StringUtils.isNotBlank(token) && ObjectUtils.isNotEmpty(userBO = AuthUtils.getUser()) && ObjectUtils.isNotEmpty(userId = userBO.getUserId()) && !LicenseUtil.licenseValid()) {
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
            try {
                Algorithm algorithm = Algorithm.HMAC256(secret);
                Verification verification = JWT.require(algorithm).withClaim("uid", userId).withClaim("oid", userBO.getDefaultOid());
                JWTVerifier verifier = verification.build();
                DecodedJWT decode = JWT.decode(token);
                algorithm.verify(decode);
                verifier.verify(token);
            } catch (Exception e) {
                HttpServletResponse res = (HttpServletResponse) servletResponse;
                LogUtil.error(e.getMessage(), e);
                HttpHeaders headers = new HttpHeaders();
                String msg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8).replace("+", "%20");
                headers.add(headName, msg);
                sendResponseEntity(res, new ResponseEntity<>(e.getMessage(), headers, HttpStatus.UNAUTHORIZED));
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void sendResponseEntity(HttpServletResponse httpResponse, ResponseEntity<String> responseEntity) throws IOException {
        HttpHeaders headers = responseEntity.getHeaders();
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        httpResponse.setStatus(statusCode.value());
        for (String name : headers.keySet()) {
            httpResponse.setHeader(name, headers.getFirst(name));
        }
        httpResponse.getWriter().write(Objects.requireNonNull(responseEntity.getBody()));
    }
}
