package com.fit2cloud;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.PropertySource;


@EnableCaching
@SpringBootApplication
@PropertySource(value = {"file:/opt/dataease/conf/dataease.properties"}, encoding = "UTF-8", ignoreResourceNotFound = true)
public class SystemApp {

    public static void main(String[] args) {
        SpringApplication.run(SystemApp.class);
    }


}
