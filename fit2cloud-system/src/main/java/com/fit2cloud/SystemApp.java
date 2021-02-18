package com.fit2cloud;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@EnableCaching
@SpringBootApplication
public class SystemApp {

    public static void main(String[] args) {
        SpringApplication.run(SystemApp.class);
    }


}
