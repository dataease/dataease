package io.dataease.auth.filter;

import io.dataease.auth.bo.TokenUserBO;
import io.dataease.constant.AuthConstant;
import io.dataease.utils.*;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Objects;

public class TokenFilter implements Filter {

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
        /*String refreshToken = null;
        if (StringUtils.isNotBlank(refreshToken = ServletUtils.request().getHeader(AuthConstant.REFRESH_TOKEN_KEY))) {
            ServletUtils.response().addHeader(AuthConstant.REFRESH_TOKEN_KEY, refreshToken);
        }*/
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
    }

    @Override
    public void destroy() {
    }
}
