package io.dataease.auth.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DeRateLimiter {

    long DEFAULT_REQUEST = 2;

    @AliasFor("max") long value() default DEFAULT_REQUEST;

    @AliasFor("value") long max() default DEFAULT_REQUEST;

    String key() default "";

    long timeout() default 500;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}
