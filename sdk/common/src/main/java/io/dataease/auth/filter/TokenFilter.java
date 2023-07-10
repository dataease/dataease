package io.dataease.auth.filter;

import cn.hutool.core.collection.ListUtil;
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
    public static List<String> WHITE_PATH = ListUtil.of(
            "/login/localLogin",
            "/apisix/check",
            "/dekey",
            "/index.html",
            "/model",
            "/demo.html",
            "/swagger-resources",
            "/doc.html",
            "/panel.html",
            "/");

    private static final String contextPath = "/de2api";
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String requestURI = request.getRequestURI();
        if (StringUtils.startsWith(requestURI, contextPath)) {
            requestURI = requestURI.replaceFirst(contextPath, "");
        }
        if (ModelUtils.isDesktop()) {
            UserUtils.setDesktopUser();
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (WHITE_PATH.contains(requestURI)
                || StringUtils.endsWithAny(requestURI, ".ico", "js", ".css", "svg", "png", "js.map")
                || StringUtils.startsWithAny(requestURI, "data:image")
                || StringUtils.startsWithAny(requestURI, "/v3/")
                || StringUtils.startsWithAny(requestURI, "/login/platformLogin/")
                || StringUtils.startsWithAny(requestURI, "/static-resource/")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        if (!ServletUtils.apisixCheck()) {
            DEException.throwException("Prohibit direct access application, request must be from apisix");
        }
        String token = null;
        String xUserStr = null;
        if(StringUtils.isBlank(token = ServletUtils.getToken()) && StringUtils.isNotBlank(xUserStr = ServletUtils.getXUserinfo())) {
            LogUtil.info("--------------------");
            LogUtil.info(xUserStr);
            LogUtil.info("--------------------");
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        TokenUserBO userBO = TokenUtils.validate(token);
        UserUtils.setUserInfo(userBO);
        filterChain.doFilter(servletRequest, servletResponse);
    }


    @Override
    public void destroy() {
    }
}
