package io.dataease.xpack.permissions.apisix.manage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.validation.Validator;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewRequestBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.JsonViewResponseBodyAdvice;

import java.util.Collections;

@Configuration
public class AuthHandlerAdapterConfig extends WebMvcConfigurationSupport {

    @Bean
    public AuthMappingHandlerAdapter authMappingHandlerAdapter(
            @Qualifier("mvcContentNegotiationManager") ContentNegotiationManager contentNegotiationManager,
            @Qualifier("mvcConversionService") FormattingConversionService conversionService,
            @Qualifier("mvcValidator") Validator validator) {

        AuthMappingHandlerAdapter adapter = new AuthMappingHandlerAdapter();
        adapter.setContentNegotiationManager(contentNegotiationManager);
        adapter.setMessageConverters(getMessageConverters());
        adapter.setWebBindingInitializer(getConfigurableWebBindingInitializer(conversionService, validator));
        adapter.setCustomArgumentResolvers(getArgumentResolvers());
        adapter.setCustomReturnValueHandlers(getReturnValueHandlers());

        /*if (jackson2Present) {
            adapter.setRequestBodyAdvice(Collections.singletonList(new JsonViewRequestBodyAdvice()));
            adapter.setResponseBodyAdvice(Collections.singletonList(new JsonViewResponseBodyAdvice()));
        }*/
        adapter.setRequestBodyAdvice(Collections.singletonList(new JsonViewRequestBodyAdvice()));
        adapter.setResponseBodyAdvice(Collections.singletonList(new JsonViewResponseBodyAdvice()));

        //AsyncSupportConfigurer configurer = getAsyncSupportConfigurer();
        /*if (configurer.getTaskExecutor() != null) {
            adapter.setTaskExecutor(configurer.getTaskExecutor());
        }
        if (configurer.getTimeout() != null) {
            adapter.setAsyncRequestTimeout(configurer.getTimeout());
        }*/
        /*adapter.setCallableInterceptors(configurer.getCallableInterceptors());
        adapter.setDeferredResultInterceptors(configurer.getDeferredResultInterceptors());*/

        return adapter;
    }
}
