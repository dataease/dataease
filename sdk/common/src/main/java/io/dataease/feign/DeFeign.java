package io.dataease.feign;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 包装FeignClient
 * 定义与实现分离
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface DeFeign {
    @AliasFor("name")
    String value() default "";

    /**
     * This will be used as the bean name instead of name if present, but will not be used
     * as a service id.
     * @return bean name instead of name if present
     */
    String contextId() default "";

    /**
     * @return The service id with optional protocol prefix. Synonym for {@link #value()
     * value}.
     */
    @AliasFor("value")
    String name() default "";

    /**
     * @return the <code>@Qualifiers</code> value for the feign client.
     */
    String[] qualifiers() default {};

    /**
     * @return an absolute URL or resolvable hostname (the protocol is optional).
     */
    String url() default "";

    /**
     * @return whether 404s should be decoded instead of throwing FeignExceptions
     */
    boolean dismiss404() default false;


    Class<?>[] configuration() default {};


    Class<?> fallback() default void.class;


    Class<?> fallbackFactory() default void.class;

    /**
     * @return path prefix to be used by all method-level mappings.
     */
    String path() default "";

    /**
     * @return whether to mark the feign proxy as a primary bean. Defaults to true.
     */
    boolean primary() default true;
}
