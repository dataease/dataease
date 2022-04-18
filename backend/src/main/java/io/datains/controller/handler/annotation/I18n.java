package io.datains.controller.handler.annotation;


import io.datains.commons.constants.I18nConstants;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface I18n {
    String value() default I18nConstants.LANG_COOKIE_NAME;
}
