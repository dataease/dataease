package io.dataease.auth.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import io.dataease.auth.annotation.DePermissionProxy;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.dto.PermissionProxy;
import io.dataease.exception.DataEaseException;

@Aspect
@Component
@Order(0)
public class DePermissionProxyHandler {

    @Around(value = "@annotation(io.dataease.auth.annotation.DePermissionProxy)")
    public Object proxyAround(ProceedingJoinPoint point) {

        try {
            MethodSignature ms = (MethodSignature) point.getSignature();
            Method method = ms.getMethod();
            DePermissionProxy annotation = method.getAnnotation(DePermissionProxy.class);
            Object[] args = point.getArgs();
            if (null == args || args.length == 0) {
                return point.proceed(args);
            }
            Object arg = point.getArgs()[annotation.paramIndex()];
            PermissionProxy proxy = getProxy(arg, annotation, 0);
            if (null != proxy && null != proxy.getUserId()) {
                AuthUtils.setProxyUser(proxy.getUserId());
            }
            return point.proceed(args);
        } catch (Throwable throwable) {
            LogUtil.error(throwable.getMessage(), throwable);
            DataEaseException.throwException(throwable);
        } finally {
            AuthUtils.cleanProxyUser();
        }
        return null;

    }

    private PermissionProxy getProxy(Object arg, DePermissionProxy annotation, int layer) throws Exception {
        if (null == arg)
            return null;
        String value = annotation.value();
        Class<?> parameterType = arg.getClass();
        if (arg instanceof PermissionProxy) {
            return (PermissionProxy) arg;
        } else if (isArray(parameterType)) {
            return null;
        } else if (isCollection(parameterType)) {
            return null;
        } else if (isMap(parameterType)) {
            Map<String, Object> argMap = (Map) arg;
            String[] values = value.split(".");
            Object o = argMap.get(values[layer]);
            return getProxy(o, annotation, ++layer);
        } else {
            // 当作自定义类处理
            String[] values = value.split("\\.");
            String fieldName = values[layer];
            Object fieldValue = getFieldValue(arg, fieldName);
            return getProxy(fieldValue, annotation, ++layer);
        }

    }

    private Object getFieldValue(Object o, String fieldName) throws Exception {
        Class<?> aClass = o.getClass();
        while (null != aClass.getSuperclass()) {
            Field[] declaredFields = aClass.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                Field field = declaredFields[i];
                String name = field.getName();
                if (StringUtils.equals(name, fieldName)) {
                    field.setAccessible(true);
                    return field.get(o);
                }
            }
            aClass = aClass.getSuperclass();
        }
        throw new NoSuchFieldException(fieldName);
    }

    private final static String[] wrapClasies = {
            "java.lang.Boolean",
            "java.lang.Character",
            "java.lang.Integer",
            "java.lang.Byte",
            "java.lang.Short",
            "java.lang.Long",
            "java.lang.Float",
            "java.lang.Double",
    };

    private Boolean isString(Class clz) {
        return StringUtils.equals("java.lang.String", clz.getName());
    }

    private Boolean isArray(Class clz) {
        return clz.isArray();
    }

    private Boolean isCollection(Class clz) {
        return Collection.class.isAssignableFrom(clz);
    }

    private Boolean isMap(Class clz) {
        return Map.class.isAssignableFrom(clz);
    }

    private Boolean isWrapClass(Class clz) {
        return Arrays.stream(wrapClasies).anyMatch(item -> StringUtils.equals(item, clz.getName()));
    }

}
