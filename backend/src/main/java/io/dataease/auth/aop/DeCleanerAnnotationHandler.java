package io.dataease.auth.aop;

import io.dataease.auth.annotation.DeCleaner;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.util.ReflectUtil;
import io.dataease.commons.constants.AuthConstants;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.model.AuthURD;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.listener.util.CacheUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Aspect
@Component
public class DeCleanerAnnotationHandler {

    @AfterReturning(value = "@annotation(io.dataease.auth.annotation.DeCleaner)")
    public void CleanerAround(JoinPoint point) {
        try {
            MethodSignature ms = (MethodSignature) point.getSignature();
            Method method = ms.getMethod();
            DeCleaner deCleaner = method.getAnnotation(DeCleaner.class);
            DePermissionType type = deCleaner.value();
            String key = deCleaner.key();
            Object[] args = point.getArgs();
            Object paramValue = null;
            if (ObjectUtils.isNotEmpty(key) && ArrayUtils.isNotEmpty(args)) {
                int pi = deCleaner.paramIndex();
                Object arg = point.getArgs()[pi];
                paramValue = getParamValue(arg, key, 0);
            }

            switch (type.name()) {
                case "DATASOURCE":
                    cleanDataSource(paramValue);
                    break;
                case "DATASET":
                    cleanDataSet(paramValue);
                    break;
                default:
                    cleanPanel(paramValue);
                    break;
            }
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }



    private void cleanCacheParent(String pid, String type) {
        if (StringUtils.isBlank(pid) || StringUtils.isBlank(type)) {
            return;
        }
        CurrentUserDto user = AuthUtils.getUser();
        List<String> resourceIds = AuthUtils.parentResources(pid.toString(), type);
        if (CollectionUtils.isEmpty(resourceIds))return;
        resourceIds.forEach(resourceId -> {
            AuthURD authURD = AuthUtils.authURDR(resourceId);
            Optional.ofNullable(authURD.getUserIds()).ifPresent(ids -> {
                ids.forEach(id -> {
                    CacheUtils.remove("user_"+type, "user" + id);
                });
            });
            Optional.ofNullable(authURD.getRoleIds()).ifPresent(ids -> {
                ids.forEach(id -> {
                    CacheUtils.remove("role_"+type, "role" + id);
                });
            });
            Optional.ofNullable(authURD.getDeptIds()).ifPresent(ids -> {
                ids.forEach(id -> {
                    List<String> depts = AuthUtils.getAuthModels(id.toString(), "dept", user.getUserId(), user.getIsAdmin());
                    depts.forEach(deptId -> {
                        CacheUtils.remove("dept_"+type, "dept" + deptId);
                    });
                });
            });
        });
    }

    public void cleanPanel(Object pid) {
        CurrentUserDto user = AuthUtils.getUser();
        CacheUtils.remove(AuthConstants.USER_PANEL_NAME, "user" + user.getUserId());
        CacheUtils.remove(AuthConstants.DEPT_PANEL_NAME, "dept" + user.getDeptId());
        user.getRoles().forEach(role -> {
            CacheUtils.remove(AuthConstants.ROLE_PANEL_NAME, "role" + role.getId());
        });

        Optional.ofNullable(pid).ifPresent(resourceId -> {
            cleanCacheParent(resourceId.toString(), "panel");
        });


    }

    public void cleanDataSet(Object pid) {
        CurrentUserDto user = AuthUtils.getUser();
        CacheUtils.remove(AuthConstants.USER_DATASET_NAME, "user" + user.getUserId());
        CacheUtils.remove(AuthConstants.DEPT_DATASET_NAME, "dept" + user.getDeptId());
        user.getRoles().forEach(role -> {
            CacheUtils.remove(AuthConstants.ROLE_DATASET_NAME, "role" + role.getId());
        });

        Optional.ofNullable(pid).ifPresent(resourceId -> {
            cleanCacheParent(resourceId.toString(), "dataset");
        });
    }

    public void cleanDataSource(Object pid) {
        CurrentUserDto user = AuthUtils.getUser();
        CacheUtils.remove(AuthConstants.USER_LINK_NAME, "user" + user.getUserId());
        CacheUtils.remove(AuthConstants.DEPT_LINK_NAME, "dept" + user.getDeptId());
        user.getRoles().forEach(role -> {
            CacheUtils.remove(AuthConstants.ROLE_LINK_NAME, "role" + role.getId());
        });

        Optional.ofNullable(pid).ifPresent(resourceId -> {
            cleanCacheParent(resourceId.toString(), "link");
        });
    }

    private Object getParamValue(Object arg, String key, int layer) throws Exception{
        if (ObjectUtils.isNotEmpty(arg)) return null;
        Class<?> parameterType = arg.getClass();
        if (parameterType.isPrimitive() || ReflectUtil.isWrapClass(parameterType) || ReflectUtil.isString(parameterType)) {
            return arg;
        } else if (ReflectUtil.isArray(parameterType)) {
            Object result;
            for (int i = 0; i < Array.getLength(arg); i++) {
                Object o = Array.get(arg, i);

                if (ObjectUtils.isNotEmpty((result = getParamValue(o, key, layer)))) {
                    return result;
                }
            }
            return null;
        } else if (ReflectUtil.isCollection(parameterType)) {
            Object[] array = ((Collection) arg).toArray();
            Object result;
            for (int i = 0; i < array.length; i++) {
                Object o = array[i];
                if (ObjectUtils.isNotEmpty((result = getParamValue(o, key, layer)))) {
                    return result;
                }
            }
            return null;
        } else if (ReflectUtil.isMap(parameterType)) {
            Map<String, Object> argMap = (Map) arg;
            String[] values = key.split("\\.");
            Object o = argMap.get(values[layer]);
            return getParamValue(o, key, ++layer);
        } else {
            // 当作自定义类处理
            String[] values = key.split("\\.");
            String fieldName = values[layer];

            Object fieldValue = ReflectUtil.getFieldValue(arg, values[layer]);
            return getParamValue(fieldValue, key, ++layer);

        }
    }
}
