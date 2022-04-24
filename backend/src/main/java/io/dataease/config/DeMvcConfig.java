package io.dataease.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static io.dataease.commons.utils.StaticResourceUtils.ensureBoth;
import static io.dataease.commons.utils.StaticResourceUtils.ensureSuffix;

/**
 * Author: wangjiahao
 * Date: 2022/4/24
 * Description:
 */
@Configuration
public class DeMvcConfig implements WebMvcConfigurer {
    private static final String FILE_PROTOCOL = "file://";
    public static final String FILE_SEPARATOR = File.separator;
    public static final String USER_HOME = "/opt/dataease/data";

    private static String WORK_DIR = ensureSuffix(USER_HOME, FILE_SEPARATOR) + "static-resource" + FILE_SEPARATOR;


    /**
     * Upload prefix.
     */
    private final static String UPLOAD_URL_PREFIX = "static-resource";


    /**
     * url separator.
     */
    public static final String URL_SEPARATOR = "/";

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

    }
}
