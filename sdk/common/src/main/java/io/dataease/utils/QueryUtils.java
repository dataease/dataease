package io.dataease.utils;

import com.querydsl.core.types.Expression;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jianneng
 * @date 2025/6/10 14:27
 **/
public class QueryUtils {

    /**
     * 缓存Q类的所有字段及其对应的Expression对象
     * 避免每次调用getAllFields时都进行反射操作
     */
    private static final Map<Class<?>, Map<String, Expression<?>>> CACHE = new ConcurrentHashMap<>();

    /**
     * 获取Q类的所有字段及其对应的Expression对象
     * @param qClassInstance Q类的实例
     * @return
     */
    public static <T> Map<String, Expression<?>> getAllFields(T qClassInstance) {
        return CACHE.computeIfAbsent(qClassInstance.getClass(), clazz -> {
            Map<String, Expression<?>> fieldMap = new HashMap<>();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(qClassInstance);
                    if (value instanceof Expression<?>) {
                        fieldMap.put(field.getName(), (Expression<?>) value);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(field.getName(), e);
                }
            }

            return fieldMap;
        });
    }

}
