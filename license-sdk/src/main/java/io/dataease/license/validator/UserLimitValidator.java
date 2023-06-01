package io.dataease.license.validator;

import io.dataease.license.config.LicenseValidator;
import io.dataease.utils.CommonBeanFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

@Component("2-userLimitValidator")
public class UserLimitValidator implements LicenseValidator {

    private static final int USER_LIMIT = 10;

    private static final String USER_API_CLASS = "io.dataease.api.permissions.user.api.UserApi";
    @Override
    public boolean validate() {
        try {
            Object userApiObject = CommonBeanFactory.getBean(Class.forName(USER_API_CLASS));
            Object userCount = executeReflext(userApiObject, "userCount", null);
            int count = (int) userCount;
            return USER_LIMIT >= count;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private Object executeReflext(Object target, String methodName, Object... args) {
        Class<?> aClass = target.getClass();
        Method method = ReflectionUtils.findMethod(aClass, methodName, null);
        ReflectionUtils.makeAccessible(method);
        return ReflectionUtils.invokeMethod(method, target, args);
    }
}
