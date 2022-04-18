package io.datains.auth.annotation;

import io.datains.commons.constants.DePermissionType;
import io.datains.commons.constants.ResourceAuthLevel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DePermission {

    DePermissionType type();

    ResourceAuthLevel level() default ResourceAuthLevel.COMMON_LEVEL_USE;

    String value() default "";

    int paramIndex() default 0;
}
