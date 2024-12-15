package io.dataease;

import io.dataease.listener.EhCacheStartListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {QuartzAutoConfiguration.class})
@EnableCaching
@EnableScheduling
public class CoreApplication {

    public static void main(String[] args) {
        SpringApplication context = new SpringApplication(CoreApplication.class);
        context.addInitializers(new EhCacheStartListener());
        context.run(args);
    }
}
