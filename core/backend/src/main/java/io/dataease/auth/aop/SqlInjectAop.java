package io.dataease.auth.aop;

import io.dataease.auth.annotation.SqlInjectValidator;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.request.KeywordRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Aspect
public class SqlInjectAop {

    @Around(value = "@annotation(io.dataease.auth.annotation.SqlInjectValidator)")
    public Object around(ProceedingJoinPoint point) {

        try {
            Signature signature = point.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            Method method = methodSignature.getMethod();
            Object[] args = point.getArgs();
            if (args == null || args.length == 0) {
                return point.proceed();
            }
            KeywordRequest request = null;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof KeywordRequest) {
                    request = (KeywordRequest) args[i];
                    break;
                }
            }
            if (request == null) return point.proceed(args);
            SqlInjectValidator annotation = AnnotationUtils.findAnnotation(method, SqlInjectValidator.class);
            String[] value = annotation.value();
            boolean illegal = isIllegal(value, request.getOrders());
            if (illegal) {
                DataEaseException.throwException("Illegal sort exp");
            }
            return point.proceed(args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isIllegal(String[] value, List<String> orderList) {
        if (CollectionUtils.isEmpty(orderList) || ArrayUtils.isEmpty(value)) return false;
        Set<String> wordList = Arrays.stream(value).collect(Collectors.toSet());
        wordList.add("asc");
        wordList.add("desc");

        return orderList.stream().anyMatch(exp ->
                Arrays.stream(exp.toLowerCase().split(",")).anyMatch(word ->
                        Arrays.stream(word.split(" ")).anyMatch(item ->
                                StringUtils.isNotBlank(item.trim()) && !wordList.contains(item.trim()))));
    }
}
