package io.dataease.auth.interceptor;

import io.dataease.constant.AuthConstant;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${dataease.cors-strict:false}")
    private boolean corsStrict;


    @Value("#{'${dataease.origin-list:http://127.0.0.1:8100}'.split(',')}")
    private List<String> originList;

    private CorsRegistration operateCorsRegistration;

    private List<String> mergedOrigins = List.of();

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(AuthConstant.DE_API_PREFIX, c -> c.isAnnotationPresent(RestController.class) && c.getPackageName().startsWith("io.dataease"));
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        operateCorsRegistration = registry.addMapping("/**")
                .allowCredentials(false)
                .allowedHeaders("*")
                .maxAge(3600)
                .allowedMethods("GET", "POST", "DELETE");
        if (corsStrict) {
            operateCorsRegistration.allowedOrigins(originList.toArray(new String[0]));
            return;
        }
        operateCorsRegistration.allowedOrigins("*");
    }

    public void addAllowedOrigins(List<String> origins) {
        mergedOrigins = origins == null ? List.of() : origins.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::strip)
                .toList();

        if (!corsStrict || CollectionUtils.isEmpty(origins)) {
            return;
        }
        List<String> newOrigins = new ArrayList<>(origins);
        newOrigins.addAll(originList);
        operateCorsRegistration.allowedOrigins(newOrigins.stream().distinct().toArray(String[]::new));
    }

    public List<String> getAllOrigins() {
        List<String> result = new ArrayList<>(originList);
        result.addAll(mergedOrigins);
        return result.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::strip)
                .distinct()
                .toList();
    }
}
