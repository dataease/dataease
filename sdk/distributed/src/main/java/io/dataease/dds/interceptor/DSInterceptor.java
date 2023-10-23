package io.dataease.dds.interceptor;

import io.dataease.dds.DynamicContextHolder;
import io.dataease.dds.constant.DataSourceConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 不同租户不同二级域名
 * nginx下把租户ID以及租户创建时间设置在请求头
 * 获取请求头部租户ID和创建时间
 * 根据租户ID和创建时间获取租户数据源名称
 * 切换数据源
 */
public class DSInterceptor implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String tenant = request.getHeader("tenant");
        String time = request.getHeader("time");
        DynamicContextHolder.push(getTenantDsKey(tenant, time));
        return true;
    }

    private String getTenantDsKey(String tenant, String time) {
        if (null == tenant || null == time) return "official-ds";
        return String.format(DataSourceConstant.DS_NAME_PREFIX, tenant, time);
    }
}
