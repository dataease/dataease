package io.dataease.license.config;

import io.dataease.exception.DEException;
import io.dataease.license.utils.LicenseUtil;
import jakarta.servlet.*;

import java.io.IOException;

public class LicenseFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (LicenseUtil.validate()) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        DEException.throwException("lic error");
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
