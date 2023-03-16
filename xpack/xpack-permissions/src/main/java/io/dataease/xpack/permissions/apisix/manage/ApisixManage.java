package io.dataease.xpack.permissions.apisix.manage;

import io.dataease.auth.DePermit;
import io.dataease.utils.LogUtil;
import io.dataease.utils.ServletUtils;
import io.dataease.xpack.permissions.apisix.proxy.ProxyRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.server.RequestPath;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.util.ServletRequestPathUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

@Service
public class ApisixManage {

    @Resource
    private AuthHandlerMethodMapping authHandlerMethodMapping;

    public void parseRequest() {
        HttpServletRequest request = ServletUtils.request();
        try {

            ProxyRequest proxyRequest =  new ProxyRequest(request);
            HttpServletRequest proxy = (HttpServletRequest) Proxy.newProxyInstance(request.getClass().getClassLoader(), request.getClass().getInterfaces(), proxyRequest);
            RequestPath requestPath = ServletRequestPathUtils.parseAndCache(proxy);
            proxy.setAttribute("org.springframework.web.util.ServletRequestPathUtils.PATH", requestPath);
            HandlerMethod handlerMethod = authHandlerMethodMapping.getHandlerMethod(proxy);
            if (ObjectUtils.isEmpty(handlerMethod) || !handlerMethod.hasMethodAnnotation(DePermit.class)) return;
            DePermit dePermit = handlerMethod.getMethodAnnotation(DePermit.class);
            String perExp = dePermit.value();
            LogUtil.info(perExp);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
