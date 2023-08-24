package io.dataease.auth.aop;

import io.dataease.auth.annotation.DeCleaner;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.utils.AopUtils;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.LogUtil;
import io.dataease.service.decatch.DeCatchProcess;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
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
                paramValue = AopUtils.getParamValue(arg, key, 0);
            }


            switch (type.name()) {
                case "DATASOURCE":
                    catchProcess().cleanDataSource(paramValue);
                    break;
                case "DATASET":
                    catchProcess().cleanDataSet(paramValue);
                    break;
                default:
                    catchProcess().cleanPanel(paramValue);
                    break;
            }
        } catch (Throwable e) {
            LogUtil.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public DeCatchProcess catchProcess() {
        return CommonBeanFactory.getBean(DeCatchProcess.class);
    }



}
