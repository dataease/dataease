package io.dataease.utils;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Method;

public class DeReflectUtil {

    public static Method findMethod(Class<?> cla, String methodName) {
        Method[] methods = cla.getMethods();
        if (ArrayUtils.isEmpty(methods)) return null;
        for (Method method : methods) {
            if (method.getName().equals(methodName)){
                return method;
            }
        }
        return null;
    }
}
