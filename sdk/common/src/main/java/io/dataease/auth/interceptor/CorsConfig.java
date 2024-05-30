package io.dataease.auth.interceptor;

import io.dataease.constant.AuthConstant;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Resource(name = "deCorsInterceptor")
    private CorsInterceptor corsInterceptor;

    @Value("#{'${dataease.origin-list:http://127.0.0.1:8100}'.split(',')}")
    private List<String> originList;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        corsInterceptor.addOriginList(originList);
        registry.addInterceptor(corsInterceptor).addPathPatterns("/**");
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(AuthConstant.DE_API_PREFIX, c -> c.isAnnotationPresent(RestController.class) && c.getPackageName().startsWith("io.dataease"));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowCredentials(true)
                .allowedOriginPatterns("*")
                .allowedHeaders("*")
                .maxAge(3600)
                .allowedMethods("*");
    }
}
