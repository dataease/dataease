package io.dataease.utils;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.Objects;
import java.util.Properties;

/**
 * @Author Junjun
 */
public class ConfigUtils {

    public static String configPath = "opt" + File.separator + "dataease3.0" + File.separator + "config" + File.separator + "application.yml";

    public static String getConfig(String key, String defaultValue) {
        try {
            String filePath = System.getProperty("user.home");
            filePath = filePath.replace("file:", "");
            Resource resource = new FileSystemResource(filePath + File.separator + configPath);
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource);
            factory.afterPropertiesSet();
            Properties properties = Objects.requireNonNull(factory.getObject());

            String basePath = properties.getProperty("base-path", "");
            basePath = basePath.replaceAll("\\$\\{user.home}", System.getProperty("user.home").replaceAll("\\\\", "/"));

            String property = properties.getProperty(key, defaultValue);
            return property.replaceAll("\\$\\{base-path}", basePath);
        } catch (Exception e) {
        }
        return defaultValue;
    }
}
