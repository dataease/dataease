package io.dataease.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        /*SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);*/
        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.ALWAYS);
        return objectMapper;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customJackson() {
        return jacksonObjectMapperBuilder -> {
            // 增大最大字符串长度限制到 350000000（或根据需要设置）
            StreamReadConstraints constraints = StreamReadConstraints.builder()
                    .maxStringLength(35000000)  // 设置更大的限制值
                    .build();

            jacksonObjectMapperBuilder.postConfigurer(objectMapper ->
                    objectMapper.getFactory().setStreamReadConstraints(constraints)
            );
        };
    }

}
