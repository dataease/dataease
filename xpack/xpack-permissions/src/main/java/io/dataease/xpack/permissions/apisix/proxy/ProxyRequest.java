package io.dataease.xpack.permissions.apisix.proxy;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.RequestPath;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyRequest implements InvocationHandler {

    private HttpServletRequest request;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String path = request.getHeader("X-Forwarded-Uri");
        // Object attribute = request.getAttribute("org.springframework.web.util.ServletRequestPathUtils.PATH");
        // PathContainer instance = instance(path);
        // RequestPath requestPath = ServletRequestPathUtils.parseAndCache(request);
        // request.setAttribute("org.springframework.web.util.ServletRequestPathUtils.PATH", requestPath);
        if (StringUtils.equals(method.getName(), "getRequestURI")) {
            return path;
        }
        if (StringUtils.equals(method.getName(), "getServletPath")) {
            return path;
        }
        if (StringUtils.equals(method.getName(), "getRequestURI")) {

            return path;
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

    private RequestPath instance(String path) throws Exception{

        Class<?> aClass = Class.forName("org.springframework.web.util.ServletRequestPathUtils.ServletRequestPath");
        Constructor<?> constructor = aClass.getConstructor(String.class, String.class, String.class);
        Object o = constructor.newInstance(path, null, null);
        return (RequestPath) o;
    }
}
