package io.dataease.xpack.permissions.apisix.manage;


import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.async.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite;
import org.springframework.web.method.support.HandlerMethodReturnValueHandlerComposite;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

@Component
public class AuthMappingHandlerAdapter {

    @Nullable
    private HandlerMethodArgumentResolverComposite argumentResolvers;

    @Nullable
    private HandlerMethodReturnValueHandlerComposite returnValueHandlers;

    private ParameterNameDiscoverer parameterNameDiscoverer;

    @Nullable
    private Long asyncRequestTimeout;

    private AsyncTaskExecutor taskExecutor;

    private CallableProcessingInterceptor[] callableInterceptors;

    private DeferredResultProcessingInterceptor[] deferredResultInterceptors;

    @Resource
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;


    public Object[] getParams(HttpServletRequest request,
                              HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
        if (this.argumentResolvers == null) {
            init();
        }
        WebDataBinderFactory binderFactory = getDataBinderFactory(handlerMethod);
        ServletWebRequest webRequest = new ServletWebRequest(request, response);
        ModelFactory modelFactory = getModelFactory(handlerMethod, binderFactory);
        ServletInvocableHandlerMethod invocableMethod = createInvocableHandlerMethod(handlerMethod);
        if (this.argumentResolvers != null) {
            invocableMethod.setHandlerMethodArgumentResolvers(this.argumentResolvers);
        }
        if (this.returnValueHandlers != null) {
            invocableMethod.setHandlerMethodReturnValueHandlers(this.returnValueHandlers);
        }
        invocableMethod.setDataBinderFactory(binderFactory);
        invocableMethod.setParameterNameDiscoverer(this.parameterNameDiscoverer);

        ModelAndViewContainer mavContainer = new ModelAndViewContainer();
        mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request));
        modelFactory.initModel(webRequest, mavContainer, invocableMethod);

        AsyncWebRequest asyncWebRequest = WebAsyncUtils.createAsyncWebRequest(request, response);
        asyncWebRequest.setTimeout(this.asyncRequestTimeout);

        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
        asyncManager.setTaskExecutor(this.taskExecutor);
        asyncManager.setAsyncWebRequest(asyncWebRequest);
        asyncManager.registerCallableInterceptors(this.callableInterceptors);
        asyncManager.registerDeferredResultInterceptors(this.deferredResultInterceptors);

        if (asyncManager.hasConcurrentResult()) {
            Object result = asyncManager.getConcurrentResult();
            mavContainer = (ModelAndViewContainer) asyncManager.getConcurrentResultContext()[0];
            asyncManager.clearConcurrentResult();

            invocableMethod = wrapConcurrentResult(result, invocableMethod);
        }
        Object[] args = getMethodArgumentValues(webRequest, mavContainer, invocableMethod);
        return args;
    }

    protected AuthServletInvocableHandlerMethod createInvocableHandlerMethod(HandlerMethod handlerMethod) {
        return new AuthServletInvocableHandlerMethod(handlerMethod);
    }

    private WebDataBinderFactory getDataBinderFactory(HandlerMethod handlerMethod) throws Exception {
        Object binderFactory = executeReflext(requestMappingHandlerAdapter, "getDataBinderFactory", handlerMethod);
        return ObjectUtils.isEmpty(binderFactory) ? null : (WebDataBinderFactory) binderFactory;
    }

    private ModelFactory getModelFactory(HandlerMethod handlerMethod, WebDataBinderFactory binderFactory) {
        Object modelFactory = executeReflext(requestMappingHandlerAdapter, "getModelFactory", handlerMethod, binderFactory);
        return ObjectUtils.isEmpty(modelFactory) ? null : (ModelFactory) modelFactory;
    }

    ServletInvocableHandlerMethod wrapConcurrentResult(Object result, ServletInvocableHandlerMethod invocableMethod) {
        Object servletInvocableHandlerMethod = executeReflext(invocableMethod, "wrapConcurrentResult", result);
        return ObjectUtils.isEmpty(servletInvocableHandlerMethod) ? null : (ServletInvocableHandlerMethod) servletInvocableHandlerMethod;
    }

    Object[] getMethodArgumentValues(NativeWebRequest request, @Nullable ModelAndViewContainer mavContainer, ServletInvocableHandlerMethod invocableMethod) throws Exception {
        AuthServletInvocableHandlerMethod handlerMethod = (AuthServletInvocableHandlerMethod) invocableMethod;
        return handlerMethod.getMethodArgumentValues(request, mavContainer);
    }

    private void init() {
        String[] fieldNameArray = {"argumentResolvers", "returnValueHandlers", "parameterNameDiscoverer",
                "asyncRequestTimeout", "taskExecutor", "callableInterceptors", "deferredResultInterceptors"};
        Arrays.stream(fieldNameArray).forEach(fieldName -> {
            Field field = ReflectionUtils.findField(RequestMappingHandlerAdapter.class, fieldName);
            ReflectionUtils.makeAccessible(field);
            Field thisField = ReflectionUtils.findField(AuthMappingHandlerAdapter.class, fieldName);
            ReflectionUtils.makeAccessible(thisField);
            try {
                thisField.set(this, field.get(requestMappingHandlerAdapter));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Object executeReflext(Object target, String methodName, Object... args) {
        Class<?> aClass = target.getClass();
        Method method = ReflectionUtils.findMethod(aClass, methodName, null);
        ReflectionUtils.makeAccessible(method);
        return ReflectionUtils.invokeMethod(method, target, args);
    }


}
