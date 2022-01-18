package io.dataease.commons.pool;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties(prefix = "detask", ignoreInvalidFields = true)
@Data
@Component
public class PriorityThreadPoolProperties {

    private int corePoolSize = 2;
    private int maximumPoolSize = 100;
    private int keepAliveTime = 60;

}
