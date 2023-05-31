package io.dataease.xpack.permissions.apisix.manage;

import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.feign.DeFeign;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringValueResolver;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPatternParser;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

@Component
public class AuthHandlerMethodMapping<T> extends RequestMappingHandlerMapping {

    private RequestMappingInfo.BuilderConfiguration config = new RequestMappingInfo.BuilderConfiguration();

    private boolean defaultPatternParser = true;

    private boolean useSuffixPatternMatch = false;

    private boolean useRegisteredSuffixPatternMatch = false;

    private StringValueResolver embeddedValueResolver;

    private Map<String, Predicate<Class<?>>> pathPrefixes = Collections.emptyMap();

    @Override
    protected boolean isHandler(Class beanType) {
        boolean match = AnnotatedElementUtils.hasAnnotation(beanType, DeApiPath.class);
        return match;
    }

    @Override
    public RequestMappingInfo getMappingForMethod(Method method, Class handlerType) {
        if (!method.isAnnotationPresent(DePermit.class)) return null;
        RequestMappingInfo info = createRequestMappingInfo(method);
        if (info != null) {
            RequestMappingInfo typeInfo = createRequestMappingInfo(handlerType);
            if (typeInfo != null) {
                info = typeInfo.combine(info);
            }
            String prefix = getPathPrefix(handlerType);
            if (prefix != null) {
                info = RequestMappingInfo.paths(prefix).options(this.config).build().combine(info);
            }
        }
        return info;
    }


    @Override
    protected Comparator getMappingComparator(HttpServletRequest request) {
        return null;
    }

    public HandlerMethod getHandlerMethod(HttpServletRequest request) throws Exception {
        HandlerMethod handlerInternal = getHandlerInternal(request);
        return handlerInternal;
    }

    @Override
    protected RequestMappingInfo getMatchingMapping(RequestMappingInfo info, HttpServletRequest request) {
        return super.getMatchingMapping(info, request);
    }

    @Override
    protected String initLookupPath(HttpServletRequest request) {
        return request.getHeader("X-Forwarded-Uri");
    }

    @Override
    protected void detectHandlerMethods(Object handler) {
        Class<?> handlerType = (handler instanceof String beanName ?
                obtainApplicationContext().getType(beanName) : handler.getClass());

        if (handlerType != null) {
            Class<?> userType = ClassUtils.getUserClass(handlerType);
            if (userType.isAnnotationPresent(DeFeign.class)) return;
            Map<Method, RequestMappingInfo> methods = MethodIntrospector.selectMethods(userType,
                    (MethodIntrospector.MetadataLookup<RequestMappingInfo>) method -> {
                        try {
                            return getMappingForMethod(method, userType);
                        } catch (Throwable ex) {
                            throw new IllegalStateException("Invalid mapping on handler class [" +
                                    userType.getName() + "]: " + method, ex);
                        }
                    });

            methods.forEach((method, mapping) -> {
                Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
                registerHandlerMethod(handler, invocableMethod, mapping);
            });
        }
    }

    @Nullable
    private RequestMappingInfo createRequestMappingInfo(AnnotatedElement element) {
        if (element instanceof Method) {
            RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(element, RequestMapping.class);
            RequestCondition<?> condition = getCustomMethodCondition((Method) element);
            return (requestMapping != null ? createRequestMappingInfo(requestMapping, condition) : null);
        }

        DeApiPath deApiPath = AnnotatedElementUtils.findMergedAnnotation(element, DeApiPath.class);
        RequestCondition<?> condition = getCustomTypeCondition((Class<?>) element);
        return deApiPath != null ? createRequestMappingInfo(deApiPath, condition) : null;
    }

    @Nullable
    protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
        return null;
    }

    @Nullable
    protected RequestCondition<?> getCustomMethodCondition(Method method) {
        return null;
    }

    protected RequestMappingInfo createRequestMappingInfo(
            RequestMapping requestMapping, @Nullable RequestCondition<?> customCondition) {

        RequestMappingInfo.Builder builder = RequestMappingInfo
                .paths(resolveEmbeddedValuesInPatterns(requestMapping.path()))
                .methods(requestMapping.method())
                .params(requestMapping.params())
                .headers(requestMapping.headers())
                .consumes(requestMapping.consumes())
                .produces(requestMapping.produces())
                .mappingName(requestMapping.name());
        if (customCondition != null) {
            builder.customCondition(customCondition);
        }
        return builder.options(this.config).build();
    }

    protected RequestMappingInfo createRequestMappingInfo(DeApiPath deApiPath, RequestCondition<?> customCondition) {
        RequestMethod[] method = {};
        String[] emptyArr = {};
        String name = "";
        RequestMappingInfo.Builder builder = RequestMappingInfo
                .paths(resolveEmbeddedValuesInPatterns(deApiPath.value()))
                .methods(method)
                .params(emptyArr)
                .headers(emptyArr)
                .consumes(emptyArr)
                .produces(emptyArr)
                .mappingName(name);
        if (customCondition != null) {
            builder.customCondition(customCondition);
        }
        return builder.options(this.config).build();
    }

    protected String[] resolveEmbeddedValuesInPatterns(String[] patterns) {
        if (this.embeddedValueResolver == null) {
            return patterns;
        } else {
            String[] resolvedPatterns = new String[patterns.length];
            for (int i = 0; i < patterns.length; i++) {
                resolvedPatterns[i] = this.embeddedValueResolver.resolveStringValue(patterns[i]);
            }
            return resolvedPatterns;
        }
    }

    @Nullable
    String getPathPrefix(Class<?> handlerType) {
        for (Map.Entry<String, Predicate<Class<?>>> entry : this.pathPrefixes.entrySet()) {
            if (entry.getValue().test(handlerType)) {
                String prefix = entry.getKey();
                if (this.embeddedValueResolver != null) {
                    prefix = this.embeddedValueResolver.resolveStringValue(prefix);
                }
                return prefix;
            }
        }
        return null;
    }

    public void afterPropertiesSet() {
        this.config = new RequestMappingInfo.BuilderConfiguration();
        this.config.setTrailingSlashMatch(useTrailingSlashMatch());
        this.config.setContentNegotiationManager(getContentNegotiationManager());

        if (getPatternParser() != null && this.defaultPatternParser &&
                (this.useSuffixPatternMatch || this.useRegisteredSuffixPatternMatch)) {

            setPatternParser(null);
        }

        if (getPatternParser() != null) {
            this.config.setPatternParser(getPatternParser());
            Assert.isTrue(!this.useSuffixPatternMatch && !this.useRegisteredSuffixPatternMatch,
                    "Suffix pattern matching not supported with PathPatternParser.");
        } else {
            this.config.setSuffixPatternMatch(useSuffixPatternMatch());
            this.config.setRegisteredSuffixPatternMatch(useRegisteredSuffixPatternMatch());
            this.config.setPathMatcher(getPathMatcher());
        }

        super.afterPropertiesSet();
    }

    @Override
    public void setPatternParser(@Nullable PathPatternParser patternParser) {
        if (patternParser != null) {
            this.defaultPatternParser = false;
        }
        super.setPatternParser(patternParser);
    }

    public void setUseSuffixPatternMatch(boolean useSuffixPatternMatch) {
        this.useSuffixPatternMatch = useSuffixPatternMatch;
    }

    public void setUseRegisteredSuffixPatternMatch(boolean useRegisteredSuffixPatternMatch) {
        this.useRegisteredSuffixPatternMatch = useRegisteredSuffixPatternMatch;
        this.useSuffixPatternMatch = (useRegisteredSuffixPatternMatch || this.useSuffixPatternMatch);
    }

    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.embeddedValueResolver = resolver;
    }

    private String resolveCorsAnnotationValue(String value) {
        if (this.embeddedValueResolver != null) {
            String resolved = this.embeddedValueResolver.resolveStringValue(value);
            return (resolved != null ? resolved : "");
        } else {
            return value;
        }
    }

    public void setPathPrefixes(Map<String, Predicate<Class<?>>> prefixes) {
        this.pathPrefixes = (!prefixes.isEmpty() ?
                Collections.unmodifiableMap(new LinkedHashMap<>(prefixes)) :
                Collections.emptyMap());
    }

    public Map<String, Predicate<Class<?>>> getPathPrefixes() {
        return this.pathPrefixes;
    }

}
