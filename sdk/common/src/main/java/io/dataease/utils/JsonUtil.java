package io.dataease.utils;

import com.google.gson.Gson;

public class JsonUtil {
    private static Gson gson = new Gson();
    public static <T> T parse(String json, Class<T> classOfT) {
        return gson.fromJson(json, classOfT);
    }

    public static Object parse(String json) {
        return gson.fromJson(json, Object.class);
    }

    public static Object toJSONString(Object o) {
        return gson.toJson(o);
    }
}
