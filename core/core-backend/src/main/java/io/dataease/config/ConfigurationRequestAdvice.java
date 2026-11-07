package io.dataease.config;

import jakarta.annotation.Resource;
import io.dataease.auth.ConfigurationSecurity;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

@RestControllerAdvice
public class ConfigurationRequestAdvice extends RequestBodyAdviceAdapter {
    @Resource
    private ConfigurationTransport transport;

    @Override
    public boolean supports(MethodParameter parameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        ConfigurationSecurity security = parameter.getMethod() == null ? null
                : AnnotatedElementUtils.findMergedAnnotation(parameter.getMethod(), ConfigurationSecurity.class);
        return security != null && security.encryptedRequest();
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage input, MethodParameter parameter,
            Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) return input;
        var request = attributes.getRequest();
        String path = org.springframework.web.util.UrlPathHelper.defaultInstance.getPathWithinApplication(request);
        path = configurationPath(path);
        boolean authorizationCheck = "/apisix/check".equals(path);
        if (authorizationCheck) {
            String forwarded = request.getHeader("X-Forwarded-Uri");
            if (forwarded == null) return input;
            path = java.net.URI.create(forwarded).getPath();
            if (path.startsWith("/casbi/")) path = path.substring("/casbi".length());
            if (path.startsWith("/oidcbi/")) path = path.substring("/oidcbi".length());
            String context = request.getContextPath();
            if (!context.isEmpty() && path.startsWith(context + "/")) path = path.substring(context.length());
            path = configurationPath(path);
        }
        byte[] body = input.getBody().readNBytes(16 * 1024 * 1024 + 1);
        if (body.length > 16 * 1024 * 1024) throw new IOException("Configuration request too large");
        byte[] plain = transport.decrypt(body, path, !authorizationCheck);
        HttpHeaders headers = new HttpHeaders();
        headers.putAll(input.getHeaders());
        headers.setContentLength(plain.length);
        return new HttpInputMessage() {
            public HttpHeaders getHeaders() { return headers; }
            public InputStream getBody() { return new ByteArrayInputStream(plain); }
        };
    }
    private String configurationPath(String path) {
        path = path.replaceAll(";[^/]*", "");
        // CorsConfig adds these as MVC route prefixes, not servlet context paths.
        for (String prefix : java.util.List.of("/casbi/de2api", "/oidcbi/de2api", "/de2api")) {
            if (path.startsWith(prefix + "/")) {
                path = path.substring(prefix.length());
                break;
            }
        }
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

}
