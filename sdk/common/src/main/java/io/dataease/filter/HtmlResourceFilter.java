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

import io.dataease.auth.interceptor.CorsConfig;
import jakarta.annotation.Resource;

import java.io.IOException;
import java.util.List;

@Component
public class HtmlResourceFilter implements Filter, Ordered {

    /**
     * 在线地图脚本来源列表
     */
    private static final String ONLINE_MAP_SCRIPT_SOURCES = String.join(" ",
            "https://*.amap.com",
            "https://*.tianditu.gov.cn",
            "https://map.qq.com",
            "https://*.map.qq.com"
    );

    private String buildCsp() {
        List<String> origins = corsConfig.getAllOrigins();
        String originStr = String.join(" ", origins);
        String frameAncestors = "frame-ancestors 'self'" + (originStr.isEmpty() ? "" : " " + originStr);

        return String.join(" ",
                "default-src 'self' *;",
                "script-src 'self' 'unsafe-inline' 'unsafe-eval'",
                "https://g.alicdn.com",
                "https://lf-package-cn.feishucdn.com",
                "https://lf-package-us.larksuitecdn.com",
                "https://lf1-cdn-tos.bytegoofy.com",
                "https://wwcdn.weixin.qq.com",
                ONLINE_MAP_SCRIPT_SOURCES + ";",
                "worker-src 'self' blob:",
                ONLINE_MAP_SCRIPT_SOURCES + ";",
                "style-src 'self' 'unsafe-inline' *;",
                "img-src * data: blob:;",
                "font-src * data:;",
                "connect-src *;",
                frameAncestors
        );
    }

    @Value("${dataease.http.cache:false}")
    private Boolean httpCache;

    @Resource
    private CorsConfig corsConfig;

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
        httpResponse.setHeader("Content-Security-Policy", buildCsp());
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
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
