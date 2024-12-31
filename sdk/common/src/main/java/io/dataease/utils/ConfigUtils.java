package io.dataease.utils;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.Objects;

/**
 * @Author Junjun
 */
public class ConfigUtils {

    public static String configPath = "opt" + File.separator + "dataease2.0" + File.separator + "config" + File.separator + "application.yml";

    public static String getConfig(String key, String defaultValue) {
        try {
            String filePath = System.getProperty("user.home");
            filePath = filePath.replace("file:", "");
            Resource resource = new FileSystemResource(filePath + File.separator + configPath);
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource);

            String basePath = Objects.requireNonNull(factory.getObject()).getProperty("base-path", "");
            basePath = basePath.replaceAll("\\$\\{user.home}", System.getProperty("user.home").replaceAll("\\\\", "/"));

            String property = Objects.requireNonNull(factory.getObject()).getProperty(key, defaultValue);
            return property.replaceAll("\\$\\{base-path}", basePath);
        } catch (Exception e) {
        }
        return defaultValue;
    }
}
