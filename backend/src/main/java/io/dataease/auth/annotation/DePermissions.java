package io.dataease.auth.annotation;


import org.apache.shiro.authz.annotation.Logical;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DePermissions {

    DePermission[] value();

    Logical logical() default Logical.AND;



}
