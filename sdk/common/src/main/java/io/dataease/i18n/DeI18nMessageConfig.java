package io.dataease.i18n;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Arrays;

@Configuration
public class DeI18nMessageConfig {

    @Value("${spring.messages.basename}")
    private String messageBaseName;

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new DeReloadableResourceBundleMessageSource();
        messageSource.setResourceLoader(new AnnotationConfigServletWebServerApplicationContext());
        Arrays.stream(messageBaseName.split(",")).map(item -> "classpath:" + item).forEach(messageSource::addBasenames);
        return messageSource;
    }
}
