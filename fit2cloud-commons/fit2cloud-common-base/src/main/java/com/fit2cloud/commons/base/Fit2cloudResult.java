package com.fit2cloud.commons.base;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.fit2cloud.commons.base.result.ResultEntity;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice({"com.fit2cloud", "springfox.documentation.swagger2.web"})
public class Fit2cloudResult extends ResultEntity implements ResponseBodyAdvice {
    @Override
    public boolean supports(MethodParameter methodParameter, Class converterType) {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType) || StringHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class converterType, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        String path = serverHttpRequest.getURI().getPath();

        if (StrUtil.equalsIgnoreCase(path, "/v2/api-docs")) {
            return o;
        }

        if (!(o instanceof ResultEntity)) {
            if (o instanceof String){
                return JSONUtil.toJsonStr(success(o));
            }
            return success(o);
        }
        return o;
    }
}
