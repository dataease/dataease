package io.dataease.auth.filter;

import io.dataease.auth.bo.TokenUserBO;
import io.dataease.constant.AuthConstant;
import io.dataease.utils.*;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TokenFilter implements Filter {

    private static final String headName = "DE-GATEWAY-FLAG";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String requestURI = request.getRequestURI();

        if (ModelUtils.isDesktop()) {
            UserUtils.setDesktopUser();
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (WhitelistUtils.match(requestURI)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        if (StringUtils.equalsIgnoreCase("OPTIONS", ServletUtils.request().getMethod())) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String executeVersion = null;
        if (StringUtils.isNotBlank(executeVersion = VersionUtil.getRandomVersion())) {
            Objects.requireNonNull(ServletUtils.response()).addHeader(AuthConstant.DE_EXECUTE_VERSION, executeVersion);
        }
        String linkToken = ServletUtils.getHead(AuthConstant.LINK_TOKEN_KEY);
        if (StringUtils.isNotBlank(linkToken)) {
            TokenUserBO tokenUserBO = TokenUtils.validateLinkToken(linkToken);
            UserUtils.setUserInfo(tokenUserBO);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        String token = ServletUtils.getToken();
        TokenUserBO userBO = null;
        try {
            userBO = TokenUtils.validate(token);
            UserUtils.setUserInfo(userBO);
        } catch (Exception e) {
            HttpServletResponse res = (HttpServletResponse) servletResponse;
            LogUtil.error(e.getMessage(), e);
            HttpHeaders headers = new HttpHeaders();
            String msg = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8).replace("+", "%20");
            headers.add(headName, msg);
            sendResponseEntity(res, new ResponseEntity<>(e.getMessage(), headers, HttpStatus.UNAUTHORIZED));
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
    }

    private void sendResponseEntity(HttpServletResponse httpResponse, ResponseEntity<String> responseEntity) throws IOException {
        HttpHeaders headers = responseEntity.getHeaders();
        HttpStatusCode statusCode = responseEntity.getStatusCode();

        // 设置状态码
        httpResponse.setStatus(statusCode.value());

        // 设置响应头
        if (headers != null) {
            for (String name : headers.keySet()) {
                httpResponse.setHeader(name, headers.getFirst(name));
            }
        }

        // 设置响应体
        httpResponse.getWriter().write(responseEntity.getBody());
    }
}
