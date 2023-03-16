package io.dataease.xpack.permissions.apisix.manage;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.http.server.RequestPath;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.util.ServletRequestPathUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class DeRequestMappingInfo implements MethodInterceptor {

    private RequestMappingInfo requestMappingInfo;

    public DeRequestMappingInfo(RequestMappingInfo requestMappingInfo) {
        this.requestMappingInfo = requestMappingInfo;
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        if (StringUtils.equals(method.getName(), "getMatchingCondition")) {
            Object req = args[1];
            HttpServletRequest request = (HttpServletRequest) req;
            RequestPath requestPath = ServletRequestPathUtils.parseAndCache(request);
            request.setAttribute("org.springframework.web.util.ServletRequestPathUtils.PATH", requestPath);
        }
        return method.invoke(requestMappingInfo, args);
    }
}
