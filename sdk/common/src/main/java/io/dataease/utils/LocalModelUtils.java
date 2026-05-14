package io.dataease.utils;

import io.dataease.model.DeModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalModelUtils {

    private static String modelValue;

    @Value("${spring.profiles.active:standalone}")
    public void setModelValue(String modelValue) {
        LocalModelUtils.modelValue = modelValue;
    }

    public static DeModel get() {
        return DeModel.valueOf(modelValue.toUpperCase());
    }

    public static boolean isDesktop() {
        return get() == DeModel.DESKTOP;
    }
}
