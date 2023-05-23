package io.dataease.auth.filter;

import io.dataease.auth.entity.ASKToken;
import io.dataease.auth.entity.JWTToken;
import io.dataease.auth.handler.ApiKeyHandler;
import io.dataease.commons.license.DefaultLicenseService;
import io.dataease.commons.license.F2CLicenseResponse;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.LogUtil;
import io.dataease.commons.utils.TokenCacheUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class JWTFilter extends BasicHttpAuthenticationFilter {


    public final static String expireMessage = "Login token is expire.";
    public final static String licMessage = "license invalid";


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


        if (ApiKeyHandler.isApiKeyCall(httpServletRequest)) {

            DefaultLicenseService licenseService = CommonBeanFactory.getBean(DefaultLicenseService.class);
            F2CLicenseResponse licenseResponse = null;
            try {
                licenseResponse = licenseService.validateLicense();
            } catch (Exception e) {
                throw new AuthenticationException(licMessage);
            }
            if (licenseResponse.getStatus() != F2CLicenseResponse.Status.valid) {
                throw new AuthenticationException(licMessage);
            }

            ASKToken askToken = ApiKeyHandler.buildToken(httpServletRequest);

            getSubject(request, response).login(askToken);
            return true;
        }

        String authorization = httpServletRequest.getHeader("Authorization");
        if (StringUtils.startsWith(authorization, "Basic")) {
            return false;
        }
        if (TokenCacheUtils.invalid(authorization)) {
            throw new AuthenticationException(expireMessage);
        }

        JWTToken token = new JWTToken(authorization);
        Subject subject = getSubject(request, response);
        // 提交给realm进行登入，如果错误他会抛出异常并被捕获
        subject.login(token);
        return true;
    }


    /**
     *
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 先判断是不是api调用
        HttpServletRequest hRequest = (HttpServletRequest) request;

        if (isLoginAttempt(request, response) || ApiKeyHandler.isApiKeyCall(hRequest)) {
            try {
                boolean loginSuccess = executeLogin(request, response);
                return loginSuccess;
            } catch (Exception e) {
                LogUtil.error(e);
                if (e instanceof AuthenticationException && StringUtils.equals(e.getMessage(), expireMessage)) {
                    responseExpire(request, response, e);
                } else if (StringUtils.equals(licMessage, e.getMessage())) {
                    responseLicError(request, response, e);
                } else {
                    tokenError(request, response, e);
                }
            }
        }
        return false;
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


    private void tokenError(ServletRequest req, ServletResponse resp, Exception e1) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) resp;
        httpServletResponse.addHeader("Access-Control-Expose-Headers", "authentication-status");
        httpServletResponse.setHeader("authentication-status", "invalid");
    }

    private void responseExpire(ServletRequest req, ServletResponse resp, Exception e1) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) resp;
        httpServletResponse.addHeader("Access-Control-Expose-Headers", "authentication-status");
        httpServletResponse.setHeader("authentication-status", "login_expire");
    }

    private void responseLicError(ServletRequest req, ServletResponse resp, Exception e1) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) resp;
        httpServletResponse.addHeader("Access-Control-Expose-Headers", "authentication-status");
        httpServletResponse.setHeader("authentication-status", licMessage);
    }

}
