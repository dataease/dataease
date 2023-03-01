package io.dataease.i18n;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface I18n {
    String value() default I18nConstants.LANG_COOKIE_NAME;
}