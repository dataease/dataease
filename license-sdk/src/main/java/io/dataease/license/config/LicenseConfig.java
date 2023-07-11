package io.dataease.license.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LicenseConfig {

    @Bean
    public FilterRegistrationBean licenseOrderFilter() {
        FilterRegistrationBean filter = new FilterRegistrationBean<>();
        filter.setName("licenseFilter");
        filter.setFilter(new LicenseFilter());
        filter.addUrlPatterns("/*");
        filter.setOrder(2);
        return filter;
    }
}
