package io.dataease.commons.utils;

import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EntityConverter {
    /**
     * 单个实体转换
     */
    public static <S, T> T convert(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("实体转换失败", e);
        }
    }

    /**
     * 列表实体转换
     */
    public static <S, T> List<T> convertList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null) {
            return Collections.emptyList();
        }
        return sourceList.stream()
                .map(source -> convert(source, targetClass))
                .collect(Collectors.toList());
    }
}
