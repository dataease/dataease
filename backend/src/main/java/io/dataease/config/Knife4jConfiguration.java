package io.dataease.config;

import cn.hutool.core.collection.CollectionUtil;
import com.github.xiaoymin.knife4j.spring.extension.OpenApiExtensionResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.*;
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
public class Knife4jConfiguration {

    private final OpenApiExtensionResolver openApiExtensionResolver;

    @Value("${app.version}")
    private String version;


    @Autowired
    public Knife4jConfiguration(OpenApiExtensionResolver openApiExtensionResolver) {
        this.openApiExtensionResolver = openApiExtensionResolver;
    }

    @Bean(value = "authApi")
    public Docket authApi() {
        return defaultApi("权限管理", "io.dataease.auth");
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
        return defaultApi("数据源管理", "io.dataease.datasource");
    }

    @Bean(value = "sysApi")
    public Docket sysApi() {
        return defaultApi("系统管理", "io.dataease.controller.sys");
    }


    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("DataEase")
                .description("人人可用的开源数据可视化分析工具")
                .termsOfServiceUrl("https://dataease.io")
                .contact(new Contact("fit2cloud","https://www.fit2cloud.com/dataease/index.html","dataease@fit2cloud.com"))
                .version(version)
                .build();
    }

    private Docket defaultApi(String groupName, String packageName) {
        List<SecurityScheme> securitySchemes=new ArrayList<>();
        securitySchemes.add(apiKey());
        List<SecurityContext> securityContexts = new ArrayList<>();
        securityContexts.add(securityContext());

        Docket docket=new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                .groupName(groupName)
                .select()
                .apis(RequestHandlerSelectors.basePackage(packageName))
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

    private ApiKey apiKey() {
        return new ApiKey("Authorization", "Authorization", "header");
    }


    List<SecurityReference> defaultAuth() {
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        return CollectionUtil.newArrayList(new SecurityReference("Authorization", authorizationScopes));
    }

}
