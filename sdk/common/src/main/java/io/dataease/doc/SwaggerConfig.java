package io.dataease.doc;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.apache.commons.lang3.RandomUtils;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SwaggerConfig {

    @Value("${dataease.version}")
    private String version;

    @Bean
    public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getTags() != null) {
                openApi.getTags().forEach(tag -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("x-order", RandomUtils.nextInt(0, 100));
                    tag.setExtensions(map);
                });
            }
        };
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Contact contact = new Contact();
        contact.setName("DataEase");
        contact.setUrl("https://www.fit2cloud.com/dataease/index.html");
        contact.setEmail("dataease@fit2cloud.com");
        return new OpenAPI()
                .info(new Info()
                        .title("DataEaseAPI")
                        .description("人人可用的开源数据可视化分析工具")
                        .termsOfService("https://dataease.io")
                        .contact(contact)
                        .version(version));
    }


    @Bean
    public GroupedOpenApi visualizationApi() {
        return GroupedOpenApi.builder().group("1-visualization").displayName("可视化管理").packagesToScan("io.dataease.visualization").build();
    }

    @Bean
    public GroupedOpenApi chartApi() {
        return GroupedOpenApi.builder().group("2-view").displayName("图表管理").packagesToScan("io.dataease.chart").build();
    }

    @Bean
    public GroupedOpenApi datasetApi() {
        return GroupedOpenApi.builder().group("3-dataset").displayName("数据集管理").packagesToScan("io.dataease.dataset").build();
    }

    @Bean
    public GroupedOpenApi dsApi() {
        return GroupedOpenApi.builder().group("4-datasource").displayName("数据源管理").packagesToScan("io.dataease.datasource").build();
    }

    @Bean
    public GroupedOpenApi basicSettingApi() {
        String[] packageArray = {
                "io.dataease.system",
                "io.dataease.map",
        };
        return GroupedOpenApi.builder().group("5-xpackpermission").displayName("系统设置").packagesToScan(packageArray).build();
    }

    @Bean
    public GroupedOpenApi baseXpackApi() {
        return GroupedOpenApi.builder().group("6-xpackbase").displayName("基础xpack").packagesToScan("io.dataease.xpack.base").build();
    }

    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder().group("7-xpackpermission").displayName("权限相关xpack").packagesToScan("io.dataease.xpack.permissions").build();
    }


}
