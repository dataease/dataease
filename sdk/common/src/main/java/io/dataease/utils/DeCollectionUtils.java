package io.dataease.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DeCollectionUtils {
    public static <K, V> Map<K, List<V>> groupBy(List<V> list,
                                                 Function<V, K> keyExtractor) {
        if (list == null || list.isEmpty()) {
            return new HashMap<>();
        }
        return list.stream().collect(Collectors.groupingBy(keyExtractor));
    }
}
