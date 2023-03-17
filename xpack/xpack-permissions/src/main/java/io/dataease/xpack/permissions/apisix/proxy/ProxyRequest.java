package io.dataease.xpack.permissions.apisix.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyRequest implements InvocationHandler {

    private HttpServletRequest request;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String path = request.getHeader("X-Forwarded-Uri");
        String sourceHttpMethod = request.getHeader("X-Forwarded-Method");
        if (StringUtils.equals(method.getName(), "getRequestURI")) {
            return path;
        }
        if (StringUtils.equals(method.getName(), "getServletPath")) {
            return path;
        }
        if (StringUtils.equals(method.getName(), "getRequestURI")) {
            return path;
        }
        if (StringUtils.isNotBlank(sourceHttpMethod) && StringUtils.equals(method.getName(), "getMethod")) {
            return sourceHttpMethod;
        }
        return method.invoke(request, args);
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public ProxyRequest(HttpServletRequest request) {
        this.request = request;
    }

    public ProxyRequest() {
    }

}
