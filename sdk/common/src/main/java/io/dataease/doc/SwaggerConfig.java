package io.dataease.doc;

import cn.hutool.core.util.RandomUtil;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
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

    /*@Bean
    public GlobalOpenApiCustomizer orderGlobalOpenApiCustomizer() {
        return openApi -> {

            if (openApi.getTags() != null) {
                openApi.getTags().forEach(tag -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("x-order", RandomUtil.randomInt(0, 100));
                    tag.setExtensions(map);
                });
            }
            if (openApi.getPaths() != null) {
                openApi.addExtension("x-test123", "333");
                openApi.getPaths().addExtension("x-abb", RandomUtil.randomInt(1, 100));
            }
        };
    }*/

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
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder().group("系统管理").packagesToScan("io.dataease.xpack.permissions.auth").build();
    }
    @Bean
    public GroupedOpenApi visualizationApi() {
        return GroupedOpenApi.builder().group("可视化管理").packagesToScan("io.dataease.visualization").build();
    }
    @Bean
    public GroupedOpenApi chartApi() {
        return GroupedOpenApi.builder().group("视图管理").packagesToScan("io.dataease.chart").build();
    }
    @Bean
    public GroupedOpenApi datasetApi() {
        return GroupedOpenApi.builder().group("数据集管理").packagesToScan("io.dataease.dataset").build();
    }
    @Bean
    public GroupedOpenApi dsApi() {
        return GroupedOpenApi.builder().group("数据源管理").packagesToScan("io.dataease.datasource").build();
    }
}
