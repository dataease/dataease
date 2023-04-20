package io.dataease.auth.filter;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.ModelUtils;
import io.dataease.utils.TokenUtils;
import jakarta.servlet.*;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;


public class TokenFilter implements Filter {
    public static List<String> WHITE_PATH = Lists.newArrayList("/login/localLogin","/apisix/check", "/dekey");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String requestURI = request.getRequestURI();
        if(WHITE_PATH.contains(requestURI)){
            filterChain.doFilter(servletRequest,servletResponse);
            return;
        }
        String token = request.getHeader("Authorization");
        if (StrUtil.isNotBlank(token))
            check(token);
        filterChain.doFilter(servletRequest,servletResponse);
    }

    private void check(String token) {
        TokenUserBO tokenUserBO = null;
        if (ModelUtils.isDesktop()) {
            tokenUserBO = new TokenUserBO();
            tokenUserBO.setUserId(1L);
            tokenUserBO.setDefaultOid(1L);
        } else {
            tokenUserBO = TokenUtils.userBOByToken(token);
        }
        AuthUtils.setUser(tokenUserBO);
    }

    @Override
    public void destroy() {
    }
}
