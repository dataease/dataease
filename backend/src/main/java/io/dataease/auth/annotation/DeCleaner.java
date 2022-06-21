package io.dataease.auth.annotation;

import io.dataease.commons.constants.DePermissionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DeCleaner {

    DePermissionType value();

    int paramIndex() default 0;

    String key() default "";
}
