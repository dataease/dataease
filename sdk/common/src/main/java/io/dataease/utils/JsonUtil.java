package io.dataease.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Collections;
import java.util.List;

public class JsonUtil {

    private static final ObjectMapper objectMapper;
    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T parse(String json, Class<T> classOfT) {
        T t = null;
        try {
            t = objectMapper.readValue(json, new TypeReference<T>() {
            });
        } catch (JsonProcessingException e) {
            LogUtil.error(e.getMessage(), e);
        }
        return t;
    }

    public static <T> T parseObject(String json, Class<T> classOfT) {
        if (json == null) return null;
        T t = null;
        try {
            t = objectMapper.readValue(json, classOfT);
        } catch (JsonProcessingException e) {
            LogUtil.error(e.getMessage(), e);
        }
        return t;
    }

    public static <T> List<T> parseList(String json, TypeReference<List<T>> classOfT) {
        if (ObjectUtils.isEmpty(json)) return Collections.emptyList();
        List<T> t = null;
        try {
            t = objectMapper.readValue(json, classOfT);
        } catch (JsonProcessingException e) {
            LogUtil.error(e.getMessage(), e);
        }
        return t;
    }

    public static Object toJSONString(Object o) {

        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            LogUtil.error(e.getMessage(), e);
            return null;
        }
    }

}
