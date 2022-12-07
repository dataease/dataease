package io.dataease.auth.aop;

import cn.hutool.core.util.StrUtil;
import io.dataease.auth.annotation.DeRateLimiter;
import io.dataease.auth.service.DeLimitService;
import io.dataease.commons.utils.IPUtils;
import io.dataease.commons.utils.ServletUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class DeRateLimiterHandler {

    private final static String SEPARATOR = ":";


    @Resource
    private DeLimitService deLimitService;


    @Around(value = "@annotation(io.dataease.auth.annotation.DeRateLimiter)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        DeRateLimiter rateLimiter = AnnotationUtils.findAnnotation(method, DeRateLimiter.class);
        if (rateLimiter != null) {
            String key = rateLimiter.key();
            if (StrUtil.isBlank(key)) {
                key = method.getDeclaringClass().getName() + StrUtil.DOT + method.getName();
            }
            key = key + SEPARATOR + IPUtils.get();

            long max = rateLimiter.max();
            long timeout = rateLimiter.timeout();
            TimeUnit timeUnit = rateLimiter.timeUnit();
            Boolean limited = deLimitService.checkRestricted(key, max, timeout, timeUnit);
            if (limited) {
                String msg = "The current API [%s] is limited, please try again later!";
                String requestURI = ServletUtils.request().getRequestURI();
                throw new RuntimeException(String.format(msg, requestURI));
            }
        }

        return point.proceed();
    }
}
