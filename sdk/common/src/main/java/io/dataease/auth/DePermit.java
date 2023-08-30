package io.dataease.auth;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DePermit {

    /**
     * 鉴权el表达式数组
     * DE产品中仅考虑权限的&运算，不考虑||
     * @return
     */
    String[] value() default {};

    String busiFlag() default "";
}
