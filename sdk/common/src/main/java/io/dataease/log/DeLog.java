package io.dataease.log;

import io.dataease.constant.LogOT;
import io.dataease.constant.LogST;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DeLog {
    String id() default "";

    String pid() default "";

    LogST st() default LogST.PANEL;

    LogOT ot();

    String stExp() default "";
}
