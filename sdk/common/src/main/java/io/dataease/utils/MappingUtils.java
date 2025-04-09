package io.dataease.utils;

import java.util.HashMap;
import java.util.Map;

public class MappingUtils {

    public static Map<String, String> mapNestedUserData(Map<String, Object> userMap, Map<String, String> mappingMap) {

        Map<String, String> resultMap = new HashMap<>();
        mappingMap.forEach((targetKey, sourcePath) -> {
            Object value = getNestedValue(userMap, sourcePath);
            if (value != null) {
                resultMap.put(targetKey, value.toString());
            }
        });

        return resultMap;
    }

    private static Object getNestedValue(Map<String, Object> sourceMap, String path) {
        String[] keys = path.split("\\.");
        Object current = sourceMap;

        for (String key : keys) {
            if (!(current instanceof Map)) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> currentMap = (Map<String, Object>) current;
            current = currentMap.get(key);

            if (current == null) {
                return null;
            }
        }

        return current;
    }

}
