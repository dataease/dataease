package io.dataease.auth;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface DeApiPath {

    String value() default "/";
}
