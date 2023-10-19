package io.dataease.license.config;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ReflectUtil;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.license.bo.F2CLicResult;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.result.ResultCode;
import io.dataease.result.ResultMessage;
import io.dataease.utils.RsaUtils;
import io.dataease.utils.ServletUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class XpackHandler {


    @Around(value = "@within(io.dataease.license.config.XpackResource) || @annotation(io.dataease.license.config.XpackResource)")
    public Object PermissionsAround(ProceedingJoinPoint point) throws Throwable {
        MethodSignature ms = (MethodSignature) point.getSignature();
        Method method = ms.getMethod();
        String methodName = method.getName();
        Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass.isAnnotationPresent(XpackResource.class)) {
            XpackResource xpackResource = declaringClass.getAnnotation(XpackResource.class);
            String[] excludes = xpackResource.excludes();
            if (ArrayUtil.isNotEmpty(excludes) && ArrayUtil.contains(excludes, methodName)) {
                if (methodName.equals("localLogin")) {
                    F2CLicResult licResult = LicenseUtil.get();
                    if (licResult.getStatus() != F2CLicResult.Status.valid) {
                        Object[] args = point.getArgs();
                        Object arg = args[0];
                        Object name = ReflectUtil.getFieldValue(arg, "name");
                        String nameText = RsaUtils.decryptStr(name.toString());
                        if (!StringUtils.equals(nameText, "admin")) {
                            DEException.throwException(Translator.get("i18n_nolic_user_limit"));
                        }
                    }
                }
                return point.proceed(point.getArgs());
            }
        }
        F2CLicResult licResult = LicenseUtil.get();
        if (ObjectUtils.isNotEmpty(licResult) && licResult.getStatus() == F2CLicResult.Status.valid) {
            return point.proceed(point.getArgs());
        }
        ServletUtils.writeResult(new ResultMessage(ResultCode.INTERFACE_FORBID_VISIT.code(), ObjectUtils.isNotEmpty(licResult) ? licResult.getMessage() : "lic error"));
        return null;
    }
}
