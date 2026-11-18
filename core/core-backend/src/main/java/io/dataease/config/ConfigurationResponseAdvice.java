package io.dataease.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.auth.ConfigurationSecurity;
import io.dataease.result.PageResult;
import io.dataease.utils.ConfigurationCredentials;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import java.util.List;

/** Only explicitly marked DTO properties are copied and masked; business payloads retain their types. */
@RestControllerAdvice
@Order(-100)
public class ConfigurationResponseAdvice implements ResponseBodyAdvice<Object> {
    @Resource
    private ObjectMapper json;

    private ConfigurationSecurity security(MethodParameter parameter) {
        return parameter.getMethod() == null ? null : AnnotatedElementUtils.findMergedAnnotation(
                parameter.getMethod(), ConfigurationSecurity.class);
    }

    @Override
    public boolean supports(MethodParameter parameter, Class<? extends HttpMessageConverter<?>> converter) {
        ConfigurationSecurity marker = security(parameter);
        return marker != null && marker.maskedResponse().length > 0
                && MappingJackson2HttpMessageConverter.class.isAssignableFrom(converter);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter parameter, MediaType mediaType,
            Class<? extends HttpMessageConverter<?>> converter, ServerHttpRequest request, ServerHttpResponse response) {
        ConfigurationSecurity marker = security(parameter);
        if (body == null || marker == null || marker.maskedResponse().length == 0) return body;
        response.getHeaders().setCacheControl("no-store");
        return copy(body, marker.maskedResponse());
    }

    private Object copy(Object value, String[] paths) {
        if (value == null) return null;
        if (value instanceof List<?> list) return list.stream().map(item -> copy(item, paths)).toList();
        if (value instanceof PageResult<?> page) {
            PageResult<Object> result = new PageResult<>();
            BeanUtils.copyProperties(page, result);
            result.setRecords(page.getRecords().stream().map(item -> copy(item, paths)).toList());
            if (page.getOriginalPage() != null) {
                result.setOriginalPage(page.getOriginalPage().map(item -> copy(item, paths)));
            }
            return result;
        }
        Object result = BeanUtils.instantiateClass(value.getClass());
        BeanUtils.copyProperties(value, result);
        for (String path : paths) maskProperty(result, path);
        return result;
    }

    private void maskProperty(Object bean, String path) {
        BeanWrapperImpl wrapper = new BeanWrapperImpl(bean);
        int dot = path.indexOf('.');
        String name = dot < 0 ? path : path.substring(0, dot);
        if (!wrapper.isReadableProperty(name) || !wrapper.isWritableProperty(name)) return;
        Object value = wrapper.getPropertyValue(name);
        if (value == null) return;
        if (dot >= 0) {
            wrapper.setPropertyValue(name, copy(value, new String[]{path.substring(dot + 1)}));
        } else {
            var field = json.createObjectNode();
            field.set(name, json.valueToTree(value));
            JsonNode masked = ConfigurationCredentials.mask(field).get(name);
            wrapper.setPropertyValue(name, json.convertValue(masked, value.getClass()));
        }
    }
}
