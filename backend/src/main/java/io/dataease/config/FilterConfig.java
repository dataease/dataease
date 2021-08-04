package io.dataease.config;

import io.dataease.commons.filter.SqlFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean registration(){
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new SqlFilter());//实例化Filter类
        filterRegistrationBean.addUrlPatterns("/*");//设置匹配模式,这里设置为所有，可以按需求设置为"/hello"等等
        filterRegistrationBean.setName("SqlFilter");//设置过滤器名称
        filterRegistrationBean.setOrder(1);//设置执行顺序
        return filterRegistrationBean;
    }

}
