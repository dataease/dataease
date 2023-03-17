package io.dataease.xpack.permissions.apisix.manage;

import io.dataease.auth.DePermit;
import io.dataease.utils.LogUtil;
import io.dataease.utils.ServletUtils;
import io.dataease.xpack.permissions.apisix.proxy.ProxyRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.util.ServletRequestPathUtils;

import java.lang.reflect.Proxy;

@Service
public class ApisixManage {
    private static final String PATH = "org.springframework.web.util.ServletRequestPathUtils.PATH";
    @Resource
    private AuthHandlerMethodMapping authHandlerMethodMapping;

    public void parseRequest() {
        HttpServletRequest request = ServletUtils.request();

        try {
            Object attribute = request.getAttribute(PATH);
            ProxyRequest proxyRequest = new ProxyRequest(request);
            HttpServletRequest proxy = (HttpServletRequest) Proxy.newProxyInstance(request.getClass().getClassLoader(), request.getClass().getInterfaces(), proxyRequest);
            RequestPath requestPath = ServletRequestPathUtils.parseAndCache(proxy);
            proxy.setAttribute(PATH, requestPath);
            HandlerMethod handlerMethod = authHandlerMethodMapping.getHandlerMethod(proxy);
            if (ObjectUtils.isEmpty(handlerMethod) || !handlerMethod.hasMethodAnnotation(DePermit.class)) return;
            DePermit dePermit = handlerMethod.getMethodAnnotation(DePermit.class);
            String perExp = dePermit.value();
            LogUtil.info("custom mapping is [{} -> {}]", proxy.getServletPath(), handlerMethod.getMethod().getName());
            LogUtil.info("method auth perExp is {}", perExp);
            request.setAttribute(PATH, attribute);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
