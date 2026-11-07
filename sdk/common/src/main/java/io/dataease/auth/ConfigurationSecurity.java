package io.dataease.auth;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Explicit HTTP configuration boundary; does not apply to internal Java calls. */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigurationSecurity {
    boolean encryptedRequest() default false;
    String[] maskedResponse() default {};
}
