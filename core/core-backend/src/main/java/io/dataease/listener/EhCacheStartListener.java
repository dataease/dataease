package io.dataease.listener;


import io.dataease.utils.ConfigUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Objects;

public class EhCacheStartListener implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Resource resource = new ClassPathResource("application.yml");
        YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
        factory.setResources(resource);
        String property = Objects.requireNonNull(factory.getObject()).getProperty("dataease.login_timeout", "480");
        System.setProperty("dataease.login_timeout", property);

        String ehcache = ConfigUtils.getConfig("dataease.path.ehcache", "/opt/dataease2.0/cache");
        System.setProperty("dataease.path.ehcache", ehcache);
    }
}
