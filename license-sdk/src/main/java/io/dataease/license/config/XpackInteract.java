package io.dataease.license.config;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface XpackInteract {

    String value();

    boolean before() default true;

    boolean replace() default false;
}
