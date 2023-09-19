package io.dataease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.ldap.LdapAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.UnsupportedEncodingException;

public class Application {
    public static void main(String[] args) throws UnsupportedEncodingException {
        String s = new String("鍓嶆璇锋眰 AMapUI 澶辫触".getBytes("GBK"),"utf-8");
        System.out.println(s);

    }
}
