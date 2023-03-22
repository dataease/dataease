package io.dataease.result;

import io.dataease.i18n.I18n;
import io.dataease.i18n.Translator;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(value = {"io.dataease"})
public class ResultResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> converterType) {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType) || StringHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> converterType, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        // 处理空值
        if (o == null && StringHttpMessageConverter.class.isAssignableFrom(converterType)) {
            return null;
        }
        //if true, need to translate
        if (methodParameter.hasMethodAnnotation(I18n.class)) {
            I18n i18n = methodParameter.getMethodAnnotation(I18n.class);
            o = translate(o, i18n.value());
        }

        if (!(o instanceof ResultHolder)) {
            return ResultHolder.success(o);
        }
        return o;
    }


    // i18n
    private Object translate(Object obj, String type) {
        return Translator.translateObject(obj);
    }


}
