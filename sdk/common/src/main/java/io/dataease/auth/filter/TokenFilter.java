package io.dataease.auth.filter;

import com.google.common.collect.Lists;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.exception.DEException;
import io.dataease.utils.*;
import jakarta.servlet.*;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;


public class TokenFilter implements Filter {
    public static List<String> WHITE_PATH = Lists.newArrayList("/login/localLogin", "/apisix/check", "/dekey", "/model", "/");

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

        if (WHITE_PATH.contains(requestURI)
                || StringUtils.endsWithAny(requestURI, ".ico", "js", ".css", "svg", "png")
                || StringUtils.startsWithAny(requestURI, "data:image")
                || StringUtils.startsWithAny(requestURI, "/static-resource/")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (!ServletUtils.apisixCheck()) {
            DEException.throwException("Prohibit direct access application, request must be from apisix");
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
