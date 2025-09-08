package io.dataease.utils;

import org.apache.commons.lang3.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BeanUtils {

    public static <T> T copyBean(T target, Object source) {
        try {
            org.springframework.beans.BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy object: ", e);
        }
    }

    public static <T> T copyBean(T target, Object source, String... ignoreProperties) {
        try {
            org.springframework.beans.BeanUtils.copyProperties(source, target, ignoreProperties);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Failed to copy object: ", e);
        }
    }

    public static Object getFieldValueByName(String fieldName, Object bean) {
        try {
            if (StringUtils.isBlank(fieldName)) {
                return null;
            }
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = bean.getClass().getMethod(getter);
            return method.invoke(bean);
        } catch (Exception e) {
            LogUtil.error("failed to getFieldValueByName. ", e);
            return null;
        }
    }

    public static void setFieldValueByName(Object bean, String fieldName, Object value, Class<?> type) {
        try {
            if (StringUtils.isBlank(fieldName)) {
                return;
            }
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String setter = "set" + firstLetter + fieldName.substring(1);
            Method method = bean.getClass().getMethod(setter, type);
            method.invoke(bean, value);
        } catch (Exception e) {
            LogUtil.error("failed to setFieldValueByName. ", e);
        }
    }

    public static Method getMethod(Object bean, String fieldName, Class<?> type) {
        try {
            if (StringUtils.isBlank(fieldName)) {
                return null;
            }
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String setter = "set" + firstLetter + fieldName.substring(1);
            return bean.getClass().getMethod(setter, type);
        } catch (Exception e) {
            return null;
        }
    }

    public static List<String> getFieldNames(Class<?> clazz) {
        List<String> fieldNames = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }
        return fieldNames;
    }

    private static String underscoreToCamel(String underscore) {
        StringBuilder result = new StringBuilder();
        String[] parts = underscore.split("_");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;
            if (i == 0) {
                result.append(part);
            } else {
                result.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }


    public static <T> T mapToBean(Map<String, Object> map, Class<T> clazz) {
        try {
            T bean = clazz.getDeclaredConstructor().newInstance();
            PropertyDescriptor[] descriptors = org.springframework.beans.BeanUtils.getPropertyDescriptors(clazz);

            for (PropertyDescriptor descriptor : descriptors) {
                String propertyName = descriptor.getName();
                if ("class".equals(propertyName)) continue;

                // 查找Map中对应的key（支持驼峰和下划线）
                Object value = findValueInMap(map, propertyName);

                // 只有当值存在且有写入方法时才设置属性
                if (value != null && descriptor.getWriteMethod() != null) {
                    try {
                        // 可选：添加类型转换逻辑来处理类型不匹配的情况
                        Object convertedValue = convertTypeIfNeeded(value, descriptor.getPropertyType());
                        descriptor.getWriteMethod().invoke(bean, convertedValue);
                    } catch (IllegalArgumentException e) {
                        // 类型不匹配时跳过该属性，而不是抛出异常
                        System.err.println("类型不匹配跳过属性: " + propertyName + ", 期望类型: " +
                                descriptor.getPropertyType() + ", 实际类型: " + value.getClass());
                    }
                }
            }
            return bean;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Map转Bean失败", e);
        }
    }

    private static Object convertTypeIfNeeded(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        // 如果类型匹配，直接返回
        if (targetType.isInstance(value)) {
            return value;
        }

        // 添加常见的类型转换逻辑
        try {
            if (targetType == String.class) {
                return value.toString();
            } else if (targetType == Integer.class || targetType == int.class) {
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                } else {
                    return Integer.parseInt(value.toString());
                }
            } else if (targetType == Long.class || targetType == long.class) {
                if (value instanceof Number) {
                    return ((Number) value).longValue();
                } else {
                    return Long.parseLong(value.toString());
                }
            } else if (targetType == Double.class || targetType == double.class) {
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                } else {
                    return Double.parseDouble(value.toString());
                }
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                if (value instanceof Boolean) {
                    return value;
                } else {
                    return Boolean.parseBoolean(value.toString());
                }
            }
            // 可以继续添加其他类型的转换...
        } catch (Exception e) {
            // 转换失败时返回原值，让后续逻辑处理
            System.err.println("类型转换失败: " + value + " to " + targetType);
        }

        return value; // 无法转换时返回原值
    }

    private static Object findValueInMap(Map<String, Object> map, String propertyName) {
        Object value = map.get(propertyName);
        if (value != null) return value;

        String underscore = camelToUnderscore(propertyName);
        value = map.get(underscore);
        if (value != null) return value;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String camelKey = underscoreToCamel(entry.getKey());
            if (propertyName.equals(camelKey)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private static String camelToUnderscore(String camel) {
        return camel.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
}
