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

    public static String getConfig(String key, String defaultValue) {
        try {
            String filePath = System.getProperty("user.dir");
            filePath = filePath.replace("file:", "").substring(0, filePath.lastIndexOf("resources"));
            Resource resource = new FileSystemResource(filePath + "resources" + File.separator + "config" + File.separator + "application.yml");
//            Resource resource = new FileSystemResource("D:\\dataease\\electron\\dataease-desktop\\resources\\config\\application.yml");
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource);
            String property = Objects.requireNonNull(factory.getObject()).getProperty(key, defaultValue);
            return property.replaceAll("\\$\\{user.home}", System.getProperty("user.home").replaceAll("\\\\", "/"));
        } catch (Exception e) {
        }
        return defaultValue;
    }
}
