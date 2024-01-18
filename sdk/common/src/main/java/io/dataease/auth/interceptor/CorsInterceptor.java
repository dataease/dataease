package io.dataease.auth.interceptor;

import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.DeReflectUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Component("deCorsInterceptor")
public class CorsInterceptor implements HandlerInterceptor {


    private final List<String> originList;

    private final List<String> busiOriginList = new ArrayList<>();

    private Class<?> aClass;

    private Object bean;


    public CorsInterceptor(List<String> originList) {
        this.originList = originList;
    }

    public void addOriginList(List<String> list) {
        List<String> strings = list.stream().filter(item -> !originList.contains(item)).toList();
        originList.addAll(strings);
    }


    public void addOriginList() {
        busiOriginList.clear();
        String className = "io.dataease.api.permissions.embedded.api.EmbeddedApi";
        String methodName = "domainList";
        if (ObjectUtils.isEmpty(aClass)) {
            try {
                aClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                return;
            }
        }
        if (ObjectUtils.isEmpty(bean)) {
            bean = CommonBeanFactory.getBean(aClass);
        }
        if (ObjectUtils.isNotEmpty(bean)) {
            Method method = DeReflectUtil.findMethod(aClass, methodName);
            Object result = ReflectionUtils.invokeMethod(method, bean);
            if (ObjectUtils.isNotEmpty(result)) {
                List<String> list = (List<String>) result;
                if (CollectionUtils.isNotEmpty(list)) {
                    busiOriginList.addAll(list.stream().distinct().toList());
                }
            }
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        addOriginList();
        String origin = request.getHeader("Origin");
        boolean embedded = StringUtils.startsWithAny(request.getRequestURI(), "/assets/", "/js/");
        if ((StringUtils.isNotBlank(origin) && originList.contains(origin)) || busiOriginList.contains(origin) || embedded) {
            response.setHeader("Access-Control-Allow-Origin", embedded ? "*" : origin);
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Max-Age", "3600");
        }

        if (StringUtils.equalsIgnoreCase(request.getMethod(), "options")) {
            response.setStatus(200);
            return false;
        }
        return true;
    }
}
