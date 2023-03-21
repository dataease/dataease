package io.dataease.i18n;

import io.dataease.utils.BeanUtils;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class Translator {

    private static final String JSON_SYMBOL = "\":";

    private static final HashSet<String> IGNORE_KEYS = new HashSet<>(Arrays.asList("id", "password", "passwd"));

    private static MessageSource messageSource;

    @Resource
    public void setMessageSource(MessageSource messageSource) {
        Translator.messageSource = messageSource;
    }

    /**
     * 单Key翻译
     */
    public static String get(String key) {
        return messageSource.getMessage(key, null, key, LocaleContextHolder.getLocale());
    }

    private static Object translateRawString(String key, String rawString) {
        if (StringUtils.isBlank(rawString)) {
            return rawString;
        }
        for (String ignoreKey : IGNORE_KEYS) {
            if (StringUtils.containsIgnoreCase(key, ignoreKey)) {
                return rawString;
            }
        }

        if (key != null) {
            String desc = get(rawString);
            if (StringUtils.isNotBlank(desc)) {
                return desc;
            }
        }
        return rawString;
    }

    public static Object translateObject(Object javaObject) {
        if (javaObject == null) {
            return null;
        }
        try {
            if (javaObject instanceof String) {
                String rawString = javaObject.toString();
                if (StringUtils.contains(rawString, JSON_SYMBOL)) {
                    try {
                        Object jsonObject = JsonUtil.parse(rawString, Object.class);
                        return JsonUtil.toJSONString(translateObject(jsonObject));
                    } catch (Exception e) {
                        LogUtil.error("Failed to translate object: " + rawString, e);
                        e.printStackTrace();
                        LogUtil.warn("Failed to translate object " + rawString + ". Error: " + ExceptionUtils.getStackTrace(e));
                        return translateRawString(null, rawString);
                    }

                } else {
                    return translateRawString(null, rawString);
                }
            }
            if (javaObject instanceof Map) {
                Map<Object, Object> map = (Map<Object, Object>) javaObject;
                for (Map.Entry<Object, Object> entry : map.entrySet()) {
                    if (entry.getValue() != null) {
                        if (entry.getValue() instanceof String) {
                            if (StringUtils.contains(entry.getValue().toString(), JSON_SYMBOL)) {
                                map.put(entry.getKey(), translateObject(entry.getValue()));
                            } else {
                                map.put(entry.getKey(), translateRawString(entry.getKey().toString(), entry.getValue().toString()));
                            }
                        } else {
                            translateObject(entry.getValue());
                        }
                    }
                }

            }

            if (javaObject instanceof Collection) {
                Collection<Object> collection = (Collection<Object>) javaObject;
                for (Object item : collection) {
                    translateObject(item);
                }
            }

            if (javaObject.getClass().isArray()) {
                for (int i = 0; i < Array.getLength(javaObject); ++i) {
                    Object item = Array.get(javaObject, i);
                    Array.set(javaObject, i, translateObject(item));
                }
            }

            Class<?> objectClass = javaObject.getClass();
            String packageName = objectClass.getPackageName();
            if (StringUtils.startsWith(packageName, "io.dataease")) {
                try {
                    Field[] declaredFields = objectClass.getDeclaredFields();
                    for (Field field : declaredFields) {
                        field.setAccessible(true);
                        Object v = field.get(javaObject);
                        if (ObjectUtils.isEmpty(v)) continue;
                        if (field.getType() == String.class) {
                            String fieldName = field.getName();
                            if (StringUtils.contains(v.toString(), JSON_SYMBOL)) {
                                BeanUtils.setFieldValueByName(javaObject, fieldName, translateObject(v), String.class);
                            } else {
                                BeanUtils.setFieldValueByName(javaObject, fieldName, translateRawString(fieldName, v.toString()), String.class);
                            }
                        } else {
                            translateObject(v);
                        }
                    }
                } catch (Exception e) {

                }

            }
            return javaObject;
        } catch (StackOverflowError stackOverflowError) {
            return javaObject;
        }
    }
}
