package io.dataease.config;

import com.github.xiaoymin.knife4j.spring.extension.OpenApiExtensionResolver;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.RequestHandler;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.ArrayList;
import java.util.List;


@EnableOpenApi
@Configuration
@Import(BeanValidatorPluginsConfiguration.class)
public class Knife4jConfiguration implements BeanPostProcessor {

    private static final String splitor = ",";

    private final OpenApiExtensionResolver openApiExtensionResolver;

    @Value("${version}")
    private String version;


    @Autowired
    public Knife4jConfiguration(OpenApiExtensionResolver openApiExtensionResolver) {
        this.openApiExtensionResolver = openApiExtensionResolver;
    }

    @Bean(value = "authApi")
    public Docket authApi() {
        return defaultApi("登录管理", "io.dataease.auth");
    }

    @Bean(value = "chartApi")
    public Docket chartApi() {
        return defaultApi("视图管理", "io.dataease.controller.chart");
    }

    @Bean(value = "datasetApi")
    public Docket datasetApi() {
        return defaultApi("数据集管理", "io.dataease.controller.dataset");
    }

    @Bean(value = "panelApi")
    public Docket panelApi() {
        return defaultApi("仪表板管理", "io.dataease.controller.panel");
    }

    @Bean(value = "datasourceApi")
    public Docket datasourceApi() {
        return defaultApi("数据源管理", "io.dataease.controller.datasource");
    }

    @Bean(value = "sysApi")
    public Docket sysApi() {
        return defaultApi("系统管理", "io.dataease.controller.sys,io.dataease.plugins.server");
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("DataEase")
                .description("人人可用的开源数据可视化分析工具")
                .termsOfServiceUrl("https://dataease.io")
                .contact(new Contact("Dataease", "https://www.fit2cloud.com/dataease/index.html", "dataease@fit2cloud.com"))
                .version(version)
                .build();
    }

    private Docket defaultApi(String groupName, String packageName) {
        List<SecurityScheme> securitySchemes = new ArrayList<>();
        securitySchemes.add(accessKey());
        securitySchemes.add(signature());

        List<SecurityContext> securityContexts = new ArrayList<>();
        securityContexts.add(securityContext());
        Docket docket = new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .groupName(groupName)
                .select()
                /*.apis(RequestHandlerSelectors.basePackage(packageName))*/
                .apis(basePackage(packageName))
                .paths(PathSelectors.any())
                .build()
                .securityContexts(securityContexts).securitySchemes(securitySchemes)
                .extensions(openApiExtensionResolver.buildExtensions(groupName));
        return docket;
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex("/.*"))
                .build();
    }

    private ApiKey accessKey() {
        return new ApiKey("accessKey", "accessKey", "header");
    }

    private ApiKey signature() {
        return new ApiKey("signature", "signature", "header");
    }


    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;

        List<SecurityReference> results = new ArrayList<>();
        results.add(new SecurityReference("accessKey", authorizationScopes));
        results.add(new SecurityReference("signature", authorizationScopes));

        return results;
    }

    public static Predicate<RequestHandler> basePackage(final String basePackage) {
        return input -> declaringClass(input).transform(handlerPackage(basePackage)).or(true);
    }

    private static Function<Class<?>, Boolean> handlerPackage(final String basePackage) {
        return input -> {
            // 循环判断匹配
            for (String strPackage : basePackage.split(splitor)) {
                boolean isMatch = input.getPackage().getName().startsWith(strPackage);
                if (isMatch) {
                    return true;
                }
            }
            return false;
        };
    }

    private static Optional<? extends Class<?>> declaringClass(RequestHandler input) {
        return Optional.fromNullable(input.declaringClass());
    }

}
