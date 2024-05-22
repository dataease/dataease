package io.dataease.filter;

import io.dataease.result.ResultMessage;
import io.dataease.utils.JsonUtil;
import jakarta.servlet.*;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HtmlResourceFilter implements Filter, Ordered {

    @Value("${dataease.http.cache:false}")
    private Boolean httpCache;

    @Override
    public int getOrder() {
        return 99;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        if(httpCache == null || !httpCache){
            // 禁用缓存
            httpResponse.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
            httpResponse.setHeader("Cache", "no-cache");
            httpResponse.setHeader(HttpHeaders.PRAGMA, "no-cache");
            httpResponse.setHeader(HttpHeaders.EXPIRES, "0");
        }
        // 继续执行过滤器链
        try {
            filterChain.doFilter(servletRequest, httpResponse);
        }catch (Exception e){
            httpResponse.setContentType("application/json");
            httpResponse.setCharacterEncoding("UTF-8");
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.getWriter().write(JsonUtil.toJSONString(new ResultMessage(HttpServletResponse.SC_BAD_REQUEST,e.getMessage())).toString());
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
