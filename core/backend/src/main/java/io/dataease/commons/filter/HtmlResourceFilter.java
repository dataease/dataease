package io.dataease.commons.filter;

import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class HtmlResourceFilter implements Filter, Ordered {

    @Override
    public int getOrder() {
        return 99;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        // 禁用缓存
        httpResponse.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        httpResponse.setHeader("Cache", "no-cache");
        httpResponse.setHeader(HttpHeaders.PRAGMA, "no-cache");
        httpResponse.setHeader(HttpHeaders.EXPIRES, "0");
        // 继续执行过滤器链
        filterChain.doFilter(servletRequest, httpResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
