package io.dataease.i18n;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JavaBeanSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.exception.DataEaseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

public class Translator {
    private static MessageSource messageSource;
    private static final String JSON_SYMBOL = "\":";
    private static final HashSet<String> IGNORE_KEYS = new HashSet<>(Arrays.asList("id", "password", "passwd"));


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
                        Object jsonObject = JSON.parse(rawString);
                        Object a = translateObject(jsonObject);
                        return JSON.toJSONString(a);
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

            ObjectSerializer serializer = SerializeConfig.globalInstance.getObjectWriter(javaObject.getClass());
            if (serializer instanceof JavaBeanSerializer) {
                JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer) serializer;

                try {
                    Map<String, Object> values = javaBeanSerializer.getFieldValuesMap(javaObject);
                    for (Map.Entry<String, Object> entry : values.entrySet()) {
                        if (entry.getValue() != null) {
                            if (entry.getValue() instanceof String) {
                                if (StringUtils.contains(entry.getValue().toString(), JSON_SYMBOL)) {
                                    BeanUtils.setFieldValueByName(javaObject, entry.getKey(), translateObject(entry.getValue()), String.class);
                                } else {
                                    BeanUtils.setFieldValueByName(javaObject, entry.getKey(), translateRawString(entry.getKey(), entry.getValue().toString()), String.class);
                                }
                            } else {
                                translateObject(entry.getValue());
                            }
                        }
                    }
                } catch (Exception e) {
                    DataEaseException.throwException(e);
                }
            }

            return javaObject;
        } catch (StackOverflowError stackOverflowError) {
            try {
                return JSON.parseObject(translateRawString(null, JSON.toJSONString(javaObject)).toString(), javaObject.getClass());
            } catch (Exception e) {
                LogUtil.error("Failed to translate object " + javaObject.toString(), e);
                return javaObject;
            }
        }
    }

}
