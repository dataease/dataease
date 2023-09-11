package io.dataease.commons.condition;

import io.dataease.commons.utils.CommonBeanFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RedisStatusCondition implements Condition {

    private static final String DEFAULT_TYPE = "ehcache";
    private static final String TARGET_TYPE = "redis";
    private static final String TYPE_KEY = "spring.cache.type";

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        String ehcacheType = environment.getProperty(TYPE_KEY, String.class, DEFAULT_TYPE);

        return StringUtils.equals(TARGET_TYPE, ehcacheType);
    }
}
