package io.dataease.traffic;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DeTraffic {
    int value() default 2;

    String api();
}
