package io.dataease.i18n;

import jakarta.annotation.Resource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;

@Component
public class DynamicI18nUtils {


    private static ReloadableResourceBundleMessageSource messageSource;

    @Resource
    public void setMessageSource(ReloadableResourceBundleMessageSource messageSource) {
        DynamicI18nUtils.messageSource = messageSource;
    }

    public static void addOrUpdate(String baseName) {
        messageSource.addBasenames(baseName);
        messageSource.setCacheSeconds(0);
        messageSource.clearCache();
    }
}
