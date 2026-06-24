package io.dataease;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import io.dataease.listener.EhCacheStartListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {QuartzAutoConfiguration.class})
@EnableCaching
@EnableScheduling
@EnableJpaRepositories(basePackages = "io.dataease")
@EnableEncryptableProperties
public class CoreApplication {

    public static void main(String[] args) {
        System.setProperty("AMAZON_REDSHIFT_JDBC_INI_FILE", "null");
        System.setProperty("user.home", "null");
        System.setProperty("java.io.tmpdir", "null");
        SpringApplication context = new SpringApplication(CoreApplication.class);
        context.addInitializers(new EhCacheStartListener());
        context.run(args);
    }
}
