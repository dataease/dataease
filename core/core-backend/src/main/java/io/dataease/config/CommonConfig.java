package io.dataease.config;

import com.fit2cloud.autoconfigure.QuartzAutoConfiguration;
import io.dataease.commons.utils.CommonThreadPool;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@AutoConfigureBefore(QuartzAutoConfiguration.class)
public class CommonConfig {

    @Bean(destroyMethod = "shutdown")
    public CommonThreadPool resourcePoolThreadPool() {
        CommonThreadPool commonThreadPool = new CommonThreadPool();
        commonThreadPool.setCorePoolSize(20);
        commonThreadPool.setMaxQueueSize(100);
        commonThreadPool.setKeepAliveSeconds(3600);
        return commonThreadPool;
    }
}
