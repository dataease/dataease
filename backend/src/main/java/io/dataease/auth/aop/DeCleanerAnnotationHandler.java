package io.dataease.auth.aop;


import io.dataease.auth.annotation.DeCleaner;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.commons.constants.AuthConstants;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.listener.util.CacheUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class DeCleanerAnnotationHandler {

    @Around(value = "@annotation(io.dataease.auth.annotation.DeCleaner)")
    public Object CleanerAround(ProceedingJoinPoint point) {
        try {
            CurrentUserDto user = AuthUtils.getUser();
            MethodSignature ms = (MethodSignature) point.getSignature();
            Method method = ms.getMethod();
            DeCleaner deCleaner = method.getAnnotation(DeCleaner.class);
            DePermissionType type = deCleaner.value();
            switch (type.name()) {
                case "DATASOURCE":
                    cleanDataSource();
                    break;
                case "DATASET":
                    cleanDataSet();
                    break;
                default:
                    cleanPanel();
                    break;
            }
            return point.proceed(point.getArgs());

        }catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public void cleanPanel() {
        CurrentUserDto user = AuthUtils.getUser();
        CacheUtils.remove(AuthConstants.USER_PANEL_NAME, "user" + user.getUserId());
        CacheUtils.remove(AuthConstants.DEPT_PANEL_NAME, "dept" + user.getDeptId());
        user.getRoles().forEach(role -> {
            CacheUtils.remove(AuthConstants.ROLE_PANEL_NAME, "role" + role.getId());
        });
    }
    public void cleanDataSet() {
        CurrentUserDto user = AuthUtils.getUser();
        CacheUtils.remove(AuthConstants.USER_DATASET_NAME, "user" + user.getUserId());
        CacheUtils.remove(AuthConstants.DEPT_DATASET_NAME, "dept" + user.getDeptId());
        user.getRoles().forEach(role -> {
            CacheUtils.remove(AuthConstants.ROLE_DATASET_NAME, "role" + role.getId());
        });
    }
    public void cleanDataSource() {
        CurrentUserDto user = AuthUtils.getUser();
        CacheUtils.remove(AuthConstants.USER_LINK_NAME, "user" + user.getUserId());
        CacheUtils.remove(AuthConstants.DEPT_LINK_NAME, "dept" + user.getDeptId());
        user.getRoles().forEach(role -> {
            CacheUtils.remove(AuthConstants.ROLE_LINK_NAME, "role" + role.getId());
        });
    }
}
