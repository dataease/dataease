package io.dataease.auth;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DeLinkPermit {

    String value() default "";

    /**
     * 子资源鉴权模式。
     * <p>
     * 默认 false：要求参数解析出的 id 与 link token 绑定的 resourceId 严格相等。
     * <p>
     * true：用于分页组件等场景，token 绑定的是父画布，而参数 id 是内嵌的子画布。
     * 此时校验子画布是否确实内嵌于 token 对应的父画布中（防止越权枚举任意资源）。
     */
    boolean subResource() default false;

}
