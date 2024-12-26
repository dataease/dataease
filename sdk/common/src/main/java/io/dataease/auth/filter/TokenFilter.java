package io.dataease.auth.filter;

import io.dataease.auth.bo.TokenUserBO;
import io.dataease.constant.AuthConstant;
import io.dataease.exception.DEException;
import io.dataease.result.ResultMessage;
import io.dataease.utils.*;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class TokenFilter implements Filter {


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
                TokenUserBO tokenUserBO = TokenUtils.validateLinkToken(linkToken);
                UserUtils.setUserInfo(tokenUserBO);
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }
            String token = ServletUtils.getToken();
            TokenUserBO userBO = TokenUtils.validate(token);
            UserUtils.setUserInfo(userBO);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserUtils.removeUser();
        }
    }

    private void sendResponseEntity(HttpServletResponse httpResponse, ResponseEntity<ResultMessage> responseEntity) throws IOException {
        HttpStatusCode statusCode = responseEntity.getStatusCode();
        httpResponse.setStatus(statusCode.value());
        httpResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        httpResponse.getWriter().write(Objects.requireNonNull(JsonUtil.toJSONString(responseEntity.getBody()).toString()));
    }

}
