package io.dataease.utils;

import org.springframework.core.env.Environment;

public class VersionUtil {


    public static String getRandomVersion() {
        Environment environment = CommonBeanFactory.getBean(Environment.class);
        assert environment != null;
        return environment.getProperty("dataease.version", "2.0.0");
    }

}
