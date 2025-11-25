package io.dataease.extensions.sync.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author jianneng
 * @date 2025/11/13 17:36
 **/
public class JsonMapUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * 解析为 Map（保持字段顺序）
     */
    public static Map<String, Object> parse(String json) throws IOException {
        return MAPPER.readValue(json, new TypeReference<LinkedHashMap<String, Object>>() {
        });
    }

    /**
     * 将 Map 序列化为 JSON 字符串
     */
    public static String toJson(Map<String, Object> map) throws IOException {
        return MAPPER.writeValueAsString(map);
    }

    /**
     * 读取路径上的值：get(root, "Doris", "doris.config", "format")
     */
    public static Object get(Map<String, Object> root, String... path) {
        Objects.requireNonNull(root, "root");
        Map<String, Object> cur = root;
        for (int i = 0; i < path.length; i++) {
            String key = path[i];
            Object val = cur.get(key);
            if (i == path.length - 1) {
                return val;
            }
            if (!(val instanceof Map)) {
                return null;
            }
            //noinspection unchecked
            cur = (Map<String, Object>) val;
        }
        return null;
    }

    /**
     * 设置路径上的值：set(root, "new_label", "Doris", "sink.label-prefix")
     */
    public static void set(Map<String, Object> root, Object value, String... path) {
        Objects.requireNonNull(root, "root");
        if (path.length == 0) {
            throw new IllegalArgumentException("path is empty");
        }
        Map<String, Object> cur = root;
        for (int i = 0; i < path.length - 1; i++) {
            String key = path[i];
            Object next = cur.get(key);
            if (!(next instanceof Map)) {
                LinkedHashMap<String, Object> created = new LinkedHashMap<>();
                cur.put(key, created);
                cur = created;
            } else {
                //noinspection unchecked
                cur = (Map<String, Object>) next;
            }
        }
        cur.put(path[path.length - 1], value);
    }

    /**
     * 删除路径上的值：remove(root, "Doris", "doris.config", "de_backends")
     */
    public static Object remove(Map<String, Object> root, String... path) {
        Objects.requireNonNull(root, "root");
        if (path.length == 0) {
            return null;
        }
        Map<String, Object> cur = root;
        for (int i = 0; i < path.length - 1; i++) {
            Object next = cur.get(path[i]);
            if (!(next instanceof Map)) {
                return null;
            }
            //noinspection unchecked
            cur = (Map<String, Object>) next;
        }
        return cur.remove(path[path.length - 1]);
    }

}
