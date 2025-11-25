package io.dataease.extensions.sync.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class JsonUtil {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        // 配置更大的 StreamReadConstraints 限制
        objectMapper.getFactory().setStreamReadConstraints(
                StreamReadConstraints.builder()
                        .maxStringLength(50000000)
                        .build()
        );
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T parse(String json, Class<T> classOfT) {
        T t = null;
        try {
            t = objectMapper.readValue(json, new TypeReference<T>() {
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T> T parseObject(String json, Class<T> classOfT) {
        if (json == null) return null;
        T t = null;
        try {
            t = objectMapper.readValue(json, classOfT);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        if (json == null) return null;
        T t = null;
        try {
            t = objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T> List<T> parseList(String json, TypeReference<List<T>> classOfT) {
        if (ObjectUtils.isEmpty(json)) return Collections.emptyList();
        List<T> t = null;
        try {
            t = objectMapper.readValue(json, classOfT);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return t;
    }

    public static Object toJSONString(Object o) {

        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询SQL操作 去掉注释内容，然后替换特定字符为空格，使得查询语句在配置文件中仅显示成一行
     * 注释符号: -- 单行、# 单行、/* 多行
     * 特定字符: \n 换行符、\t 制表符、\r 回车符
     *
     * @param input 输入字符串
     * @return 替换后的字符串
     */
    public static String replaceAndEscape(String input) {
        if (input == null) {
            return null;
        }
        return SqlUtil.removeSqlComments(input).replaceAll("[\n\t\r]", " ");
    }

    public static String writeValueAsString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
