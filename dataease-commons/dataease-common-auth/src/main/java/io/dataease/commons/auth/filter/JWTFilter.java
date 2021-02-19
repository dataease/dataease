package io.dataease.commons.auth.filter;

import io.dataease.commons.auth.config.JWTToken;
import io.dataease.commons.auth.util.JWTUtil;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class JWTFilter extends BasicHttpAuthenticationFilter {


    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());


    /**
     * 判断用户是否想要登入。
     * 检测header里面是否包含Authorization字段即可
     */
    @Override
    protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
        HttpServletRequest req = (HttpServletRequest) request;
        String authorization = req.getHeader("Authorization");
        return authorization != null;
    }

    /**
     *
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String authorization = httpServletRequest.getHeader("Authorization");

        if (JWTUtil.needRefresh(authorization)){
            authorization = refreshToken(request, response);
        }
        JWTToken token = new JWTToken(authorization);
        Subject subject = getSubject(request, response);
        // 提交给realm进行登入，如果错误他会抛出异常并被捕获
        subject.login(token);
        return true;
    }



    /**
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (isLoginAttempt(request, response)) {
            try {
                boolean loginSuccess = executeLogin(request, response);
                return loginSuccess;
            } catch (Exception e) {
                response401(request, response);
            }
        }
        return false;
    }

    private String refreshToken(ServletRequest request, ServletResponse response) {
        // 获取AccessToken(Shiro中getAuthzHeader方法已经实现)
        String token = this.getAuthzHeader(request);
        // 获取当前Token的帐号信息
        String username = JWTUtil.getUsername(token);
        String password = JWTUtil.getPassword(username);
        try {
            String newToken = JWTUtil.sign(username, password);
            JWTToken jwtToken = new JWTToken(newToken);
            this.getSubject(request, response).login(jwtToken);
            // 设置响应的Header头新Token
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.addHeader("Access-Control-Expose-Headers", "Authorization");
            httpServletResponse.setHeader("Authorization", newToken);
            return newToken;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 对跨域提供支持
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个option请求，这里我们给option请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }

    /**
     * 将非法请求跳转到 /401
     */
    private void response401(ServletRequest req, ServletResponse resp) {
        try {
            HttpServletResponse httpServletResponse = (HttpServletResponse) resp;
            httpServletResponse.sendRedirect("/401");
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }
}
