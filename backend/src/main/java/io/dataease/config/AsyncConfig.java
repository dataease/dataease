package io.dataease.config;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.dataease.commons.pool.PriorityThreadPoolExecutor;
import io.dataease.commons.pool.PriorityThreadPoolProperties;

@EnableAsync(proxyTargetClass = true)
@Configuration
public class AsyncConfig {

    @Resource
    private PriorityThreadPoolProperties priorityThreadPoolProperties;

    @Bean
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("Async-Executor");
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        return executor;
    }

    @Bean
    public PriorityThreadPoolExecutor priorityExecutor() {
        int corePoolSize = priorityThreadPoolProperties.getCorePoolSize();

        int maximumPoolSize = priorityThreadPoolProperties.getMaximumPoolSize();

        int keepAliveTime = priorityThreadPoolProperties.getKeepAliveTime();

        PriorityThreadPoolExecutor executor = new PriorityThreadPoolExecutor(corePoolSize, maximumPoolSize,
                keepAliveTime, TimeUnit.SECONDS);
        return executor;
    }

}
