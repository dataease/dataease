package io.dataease.license.config;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface XpackResource {
    String[] excludes() default {};
}
