package io.dataease.commons.utils;

import io.dataease.commons.constants.AuthConstants;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletUtils {

    public static HttpServletRequest request(){
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return request;
    }

    public static HttpServletResponse response(){
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        return response;
    }


    public static void setToken(String token){
        HttpServletResponse httpServletResponse = response();
        httpServletResponse.addHeader("Access-Control-Expose-Headers", "Authorization");
        httpServletResponse.setHeader(AuthConstants.TOKEN_KEY, token);
    }

    public static String getToken(){
        HttpServletRequest request = request();
        String token = request.getHeader(AuthConstants.TOKEN_KEY);
        return token;
    }




}
