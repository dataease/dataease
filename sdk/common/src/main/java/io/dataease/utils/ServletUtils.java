package io.dataease.utils;

import io.dataease.constant.AuthConstant;
import io.dataease.result.ResultMessage;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;

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

    public static String getHead(String key) {
        HttpServletRequest request = request();
        return request.getHeader(key);
    }

    public static String getToken() {
        return getHead(AuthConstant.TOKEN_KEY);
    }

    public static String getXUserinfo() {
        return getHead(AuthConstant.OIDC_X_USER);
    }
    public static String getCasUser() {
        return getHead(AuthConstant.CAS_X_USER);
    }

    public static boolean apisixCheck() {
        return true;
    }

    public static void writeResult(ResultMessage resultMessage) {
        HttpServletResponse response = response();

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        ServletOutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            outputStream.print(JsonUtil.toJSONString(resultMessage).toString());
        } catch (IOException ex) {
            LogUtil.error(ex.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
