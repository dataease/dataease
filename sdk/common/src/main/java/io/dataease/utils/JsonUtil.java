package io.dataease.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

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

    public static <T> List<T> parseList(String json, Class<T> classOfT) {
        List<T> t = null;
        try {
            t = objectMapper.readValue(json, new TypeReference<List<T>>() {
            });
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
