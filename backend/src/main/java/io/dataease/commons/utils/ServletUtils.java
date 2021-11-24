package io.dataease.commons.utils;

import io.dataease.commons.constants.AuthConstants;
import io.dataease.plugins.config.SpringContextUtil;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ServletUtils {

    public static HttpServletRequest request() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        return request;
    }

    public static HttpServletResponse response() {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = servletRequestAttributes.getResponse();
        return response;
    }


    public static void setToken(String token) {
        HttpServletResponse httpServletResponse = response();
        httpServletResponse.addHeader("Access-Control-Expose-Headers", "Authorization");
        httpServletResponse.setHeader(AuthConstants.TOKEN_KEY, token);
    }

    public static String getToken() {
        HttpServletRequest request = request();
        String token = request.getHeader(AuthConstants.TOKEN_KEY);
        return token;
    }

    public static String domain() {
        InetAddress ip;
        String hostAddress = "";
        try {
            ip = InetAddress.getLocalHost();
            hostAddress = ip.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Environment environment = SpringContextUtil.getBean(Environment.class);
        Integer port = environment.getProperty("server.port", Integer.class);
        return "http://" + hostAddress + ":" + port;
    }


}
