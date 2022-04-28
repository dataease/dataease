package io.dataease.auth.aop;

import io.dataease.auth.annotation.DePermission;
import io.dataease.auth.annotation.DePermissions;
import io.dataease.auth.entity.AuthItem;
import io.dataease.auth.util.ReflectUtil;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.LogUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.authz.annotation.Logical;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Aspect
@Component
public class DePermissionAnnotationHandler {

    @Around(value = "@annotation(io.dataease.auth.annotation.DePermissions)")
    public Object PermissionsAround(ProceedingJoinPoint point) throws Throwable{

        if (AuthUtils.getUser().getIsAdmin()) {
            return point.proceed(point.getArgs());
        }
        Boolean access = false;
        try {

            MethodSignature ms = (MethodSignature) point.getSignature();
            Method method = ms.getMethod();
            DePermissions annotation = method.getAnnotation(DePermissions.class);
            Logical logical = annotation.logical();
            DePermission[] dePermissions = annotation.value();
            Object[] args = point.getArgs();
            if (logical == Logical.AND) {
                access = true;
                for (int i = 0; i < dePermissions.length; i++) {
                    DePermission permission = dePermissions[i];
                    boolean currentAccess = access(args[permission.paramIndex()], permission, 0);
                    if (!currentAccess) {
                        access = false;
                        break;
                    }
                }
            } else {
                List<Exception> exceptions = new ArrayList<>();
                for (int i = 0; i < dePermissions.length; i++) {
                    DePermission permission = dePermissions[i];
                    try {
                        boolean currentAccess = access(args[permission.paramIndex()], permission, 0);
                        if (currentAccess) {
                            access = true;
                            break;
                        }
                    } catch (Exception e) {
                        exceptions.add(e);
                    }
                }
                if (!access && exceptions.size() > 0) {
                    throw exceptions.get(0);
                }
            }

        } catch (Throwable throwable) {
            LogUtil.error(throwable.getMessage(), throwable);
            throw new RuntimeException(throwable.getMessage());
        }

        return access ? point.proceed(point.getArgs()) : null;
    }

    @Around(value = "@annotation(io.dataease.auth.annotation.DePermission)")
    public Object PermissionAround(ProceedingJoinPoint point) throws Throwable{
        Boolean access = false;
        try {
            if (AuthUtils.getUser().getIsAdmin()) {
                return point.proceed(point.getArgs());
            }
            MethodSignature ms = (MethodSignature) point.getSignature();
            Method method = ms.getMethod();

            DePermission annotation = method.getAnnotation(DePermission.class);
            Object arg = point.getArgs()[annotation.paramIndex()];
            if (access(arg, annotation, 0)) {
                access = true;
            }
        } catch (Throwable throwable) {
            LogUtil.error(throwable.getMessage(), throwable);
            throw new RuntimeException(throwable.getMessage());
        }

        return access ? point.proceed(point.getArgs()) : null;
    }

    private Boolean access(Object arg, DePermission annotation, int layer) throws Exception {
        if (ObjectUtils.isEmpty(arg))
            return true;
        String type = annotation.type().name().toLowerCase();
        String value = annotation.value();
        Integer requireLevel = annotation.level().getLevel();

        Set<String> resourceIds = AuthUtils.permissionByType(type).stream().filter(
                item -> item.getLevel() >= requireLevel).map(AuthItem::getAuthSource).collect(Collectors.toSet());

        Class<?> parameterType = arg.getClass();
        if (parameterType.isPrimitive() || ReflectUtil.isWrapClass(parameterType) || ReflectUtil.isString(parameterType)) {
            boolean permissionValid = resourceIds.contains(arg);
            if (permissionValid)
                return true;
            throw new UnauthorizedException("Subject does not have permission[" + annotation.level().name() + ":"
                    + annotation.type() + ":" + arg + "]");
        } else if (ReflectUtil.isArray(parameterType)) {
            for (int i = 0; i < Array.getLength(arg); i++) {
                Object o = Array.get(arg, i);
                if (!access(o, annotation, layer)) {
                    return false;
                }
            }

        } else if (ReflectUtil.isCollection(parameterType)) {
            Object[] array = ((Collection) arg).toArray();
            for (int i = 0; i < array.length; i++) {
                Object o = array[i];
                if (!access(o, annotation, layer)) {
                    return false;
                }
            }
        } else if (ReflectUtil.isMap(parameterType)) {
            Map<String, Object> argMap = (Map) arg;
            String[] values = value.split(".");
            Object o = argMap.get(values[layer]);
            return access(o, annotation, ++layer);
        } else {
            // 当作自定义类处理
            String[] values = value.split("\\.");
            String fieldName = values[layer];

            Object fieldValue = ReflectUtil.getFieldValue(arg, fieldName);
            return access(fieldValue, annotation, ++layer);

        }
        return true;
    }



}
