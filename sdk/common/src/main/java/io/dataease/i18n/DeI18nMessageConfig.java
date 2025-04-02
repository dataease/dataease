package io.dataease.i18n;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.Locale;

@Configuration
public class DeI18nMessageConfig {

    @Value("${spring.messages.basename}")
    private String messageBaseName;

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.CHINA);
        return localeResolver;
    }

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new DeReloadableResourceBundleMessageSource();
        messageSource.setResourceLoader(new AnnotationConfigServletWebServerApplicationContext());
        Arrays.stream(messageBaseName.split(",")).map(item -> "classpath:" + item).forEach(messageSource::addBasenames);
        return messageSource;
    }
}
