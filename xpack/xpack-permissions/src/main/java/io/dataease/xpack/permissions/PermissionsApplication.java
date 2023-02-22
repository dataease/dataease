package io.dataease.xpack.permissions;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("io.dataease")
@MapperScan("io.dataease")
public class PermissionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PermissionsApplication.class, args);
    }
}
