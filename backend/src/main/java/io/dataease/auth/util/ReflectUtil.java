package io.dataease.auth.util;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class ReflectUtil {

    public static Object getFieldValue(Object o, String fieldName) throws Exception {
        Class<?> aClass = o.getClass();
        while (null != aClass.getSuperclass()) {
            Field[] declaredFields = aClass.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                Field field = declaredFields[i];
                String name = field.getName();
                if (StringUtils.equals(name, fieldName)) {
                    field.setAccessible(true);
                    return field.get(o);
                }
            }
            aClass = aClass.getSuperclass();
        }
        throw new NoSuchFieldException(fieldName);
    }

    private  final static String[] wrapClasies = {
            "java.lang.Boolean",
            "java.lang.Character",
            "java.lang.Integer",
            "java.lang.Byte",
            "java.lang.Short",
            "java.lang.Long",
            "java.lang.Float",
            "java.lang.Double",
    };

    public static Boolean isString(Class clz) {
        return StringUtils.equals("java.lang.String", clz.getName());
    }

    public static Boolean isArray(Class clz) {
        return clz.isArray();
    }

    public static Boolean isCollection(Class clz) {
        return Collection.class.isAssignableFrom(clz);
    }

    public static Boolean isMap(Class clz) {
        return Map.class.isAssignableFrom(clz);
    }

    public static Boolean isWrapClass(Class clz) {
        return Arrays.stream(wrapClasies).anyMatch(item -> StringUtils.equals(item, clz.getName()));
    }
}
