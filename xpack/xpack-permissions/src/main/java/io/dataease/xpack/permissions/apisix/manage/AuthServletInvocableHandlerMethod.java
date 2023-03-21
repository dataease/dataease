package io.dataease.xpack.permissions.apisix.manage;

import org.springframework.context.MessageSource;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import java.lang.reflect.Method;

public class AuthServletInvocableHandlerMethod extends ServletInvocableHandlerMethod {

    public AuthServletInvocableHandlerMethod(Object handler, Method method) {
        super(handler, method);
    }

    public AuthServletInvocableHandlerMethod(Object handler, Method method, MessageSource messageSource) {
        super(handler, method, messageSource);
    }

    public AuthServletInvocableHandlerMethod(HandlerMethod handlerMethod) {
        super(handlerMethod);
    }

    @Override
    public Object[] getMethodArgumentValues(NativeWebRequest request, ModelAndViewContainer mavContainer, Object... providedArgs) throws Exception {
        return super.getMethodArgumentValues(request, mavContainer, providedArgs);
    }
}
