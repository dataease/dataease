package io.dataease.auth.annotation;

import io.dataease.commons.constants.SysLogConstants.OPERATE_TYPE;
import io.dataease.commons.constants.SysLogConstants.SOURCE_TYPE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DeLog {

    OPERATE_TYPE operatetype();


    SOURCE_TYPE sourcetype();


    int positionIndex() default -1;
    String positionKey() default "";


    int sourceIndex() default 0;
    String sourceKey() default "";

    int targetIndex() default -1;
    String targetKey() default "";
    SOURCE_TYPE targetType() default SOURCE_TYPE.USER;

    String value() default "";

}
