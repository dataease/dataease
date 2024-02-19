package io.dataease.utils;

public class DeClassUtils {

    public static boolean isPrimitiveOrWrapper(Object obj) {
        if (obj == null) {
            return false;
        }

        Class<?> objClass = obj.getClass();
        for (Class<?> primitiveWrapper : primitiveWrappers) {
            if (primitiveWrapper.isAssignableFrom(objClass)) {
                return true;
            }
        }

        return isPrimitive(objClass);
    }

    private static boolean isPrimitive(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return true;
        }

        String name = clazz.getName();
        for (String primitiveTypeName : primitiveTypeNames) {
            if (name.equals(primitiveTypeName)) {
                return true;
            }
        }

        return false;
    }

    private static final Class<?>[] primitiveWrappers = {
            Boolean.class, Character.class, Byte.class, Short.class,
            Integer.class, Long.class, Float.class, Double.class
    };

    private static final String[] primitiveTypeNames = {
            "boolean", "char", "byte", "short",
            "int", "long", "float", "double"
    };

}
