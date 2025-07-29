package io.dataease.utils;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.*;

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
     *
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

    /**
     * TODO 概念方法
     * 通过Q类的简单类名获取Q类对象
     *
     * @param qClassSimpleName Q类的简单类名
     * @param packages         可选的包名数组，如果不提供则默认使用 io.dataease.entity 包
     * @return Q类对象
     */
    public static EntityPath<?> getQClassInstance(String qClassSimpleName, String packages) {
        String fullClassName = packages + "." + qClassSimpleName;
        try {
            Class<?> clazz = Class.forName(fullClassName);
            // 通常Q类有 public static final QXXX qXXX = new QXXX("xxx");
            for (java.lang.reflect.Field field : clazz.getFields()) {
                if (EntityPath.class.isAssignableFrom(field.getType())) {
                    Object value = field.get(null);
                    if (value != null) {
                        return (EntityPath<?>) value;
                    }
                }
            }
            throw new IllegalArgumentException("未找到Q类: " + qClassSimpleName);
        } catch (Exception e) {
            throw new RuntimeException("获取Q类对象失败: " + qClassSimpleName, e);
        }
    }

    /**
     * TODO 概念方法
     * 通过字段名称获取字段Path对象
     *
     * @param qClassInstance Q类实例
     * @param fieldName      字段名称
     * @return Path对象
     */
    public static Path<?> getFieldPath(EntityPath<?> qClassInstance, String fieldName) {
        try {
            Field field = qClassInstance.getClass().getField(fieldName);
            Object pathObject = field.get(qClassInstance);
            return switch (pathObject) {
                case StringPath stringPath -> stringPath;
                case NumberPath<?> numberPath -> numberPath;
                case BooleanPath booleanPath -> booleanPath;
                case DatePath<?> datePath -> datePath;
                case DateTimePath<?> dateTimePath -> dateTimePath;
                case TimePath<?> timePath -> timePath;
                case EnumPath<?> enumPath -> enumPath;
                case ArrayPath<?, ?> arrayPath -> arrayPath;
                case null, default -> (Path<?>) pathObject;
            };
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("无法获取字段: " + fieldName, e);
        }
    }

}
