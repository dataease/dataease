package io.dataease.config;

import io.dataease.constant.AuthConstant;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static io.dataease.constant.StaticResourceConstants.*;
import static io.dataease.utils.StaticResourceUtils.ensureBoth;
import static io.dataease.utils.StaticResourceUtils.ensureSuffix;
@Configuration
public class DeMvcConfig implements WebMvcConfigurer {


    /**
     * Configuring static resource path
     *
     * @param registry registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String workDir = FILE_PROTOCOL + ensureSuffix(WORK_DIR, FILE_SEPARATOR);
        String uploadUrlPattern = ensureBoth(UPLOAD_URL_PREFIX, URL_SEPARATOR) + "**";
        registry.addResourceHandler(uploadUrlPattern)
                .addResourceLocations(workDir);
        // map
        String mapDir = FILE_PROTOCOL + ensureSuffix(MAP_DIR, FILE_SEPARATOR);
        String mapUrlPattern = ensureBoth(MAP_URL, AuthConstant.DE_API_PREFIX, URL_SEPARATOR) + "**";
        registry.addResourceHandler(mapUrlPattern)
                .addResourceLocations(mapDir);
    }
}
