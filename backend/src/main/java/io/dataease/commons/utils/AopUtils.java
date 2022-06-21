package io.dataease.commons.utils;

import io.dataease.auth.util.ReflectUtil;
import org.apache.commons.lang3.ObjectUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

public class AopUtils {

    public static Object getParamValue(Object arg, String key, int layer) throws Exception{
        if (ObjectUtils.isEmpty(arg)) return null;
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
