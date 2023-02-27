package io.dataease.auth.filter;

import cn.hutool.core.util.ArrayUtil;
import com.auth0.jwt.algorithms.Algorithm;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.util.JWTUtils;
import io.dataease.commons.license.DefaultLicenseService;
import io.dataease.commons.license.F2CLicenseResponse;
import io.dataease.commons.utils.CommonBeanFactory;
import io.dataease.commons.utils.LogUtil;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.filter.AccessControlFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

import static io.dataease.commons.license.F2CLicenseResponse.Status;

public class F2CDocFilter extends AccessControlFilter {

    private static final String RESULT_URI_KEY = "result_uri_key";
    private static final String NOLIC_PAGE = "nolic.html";
    private static final String NO_LOGIN_PAGE = "/nologin.html";
    private static final String DEFAULT_FAILED_PAGE = "/";


    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        try {
            DefaultLicenseService defaultLicenseService = CommonBeanFactory.getBean(DefaultLicenseService.class);
            F2CLicenseResponse f2CLicenseResponse = defaultLicenseService.validateLicense();
            Status status = f2CLicenseResponse.getStatus();
            if (status != Status.valid) {
                request.setAttribute(RESULT_URI_KEY, NOLIC_PAGE);
                return false;
            }
        } catch (Exception e) {
            request.setAttribute(RESULT_URI_KEY, NOLIC_PAGE);
            LogUtil.error(e.getMessage(), e);
            return false;
        }

        try {
            Boolean isLogin = validateLogin(request);
            if (!isLogin) {
                request.setAttribute(RESULT_URI_KEY, NO_LOGIN_PAGE);
                return false;
            }
        } catch (Exception e) {
            request.setAttribute(RESULT_URI_KEY, NO_LOGIN_PAGE);
            LogUtil.error(e.getMessage(), e);
            return false;
        }

        return true;
    }

    private Boolean validateLogin(HttpServletRequest request) throws Exception {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isBlank(authorization)) {
            Cookie[] cookies = request.getCookies();
            if (ArrayUtil.isNotEmpty(cookies)) {
                Cookie cookie = Arrays.stream(cookies).filter(item -> StringUtils.equals(item.getName(), "Authorization")).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(cookie) && StringUtils.isNotBlank(cookie.getValue())) {
                    authorization = cookie.getValue();
                }
            }
        }
        if (StringUtils.isBlank(authorization)) {
            return false;
        }

        TokenInfo tokenInfo = JWTUtils.tokenInfoByToken(authorization);
        AuthUserService authUserService = CommonBeanFactory.getBean(AuthUserService.class);
        SysUserEntity user = authUserService.getUserById(tokenInfo.getUserId());
        if (user == null) {
            return false;
        }
        String password = user.getPassword();
        boolean verify = JWTUtils.verify(authorization, tokenInfo, password);
        return verify;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest req, ServletResponse res) throws Exception {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        Object attribute = request.getAttribute(RESULT_URI_KEY);
        String path = ObjectUtils.isNotEmpty(attribute) ? attribute.toString() : DEFAULT_FAILED_PAGE;
        path += ("?_t" + System.currentTimeMillis());
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Expires", "0");
        request.getRequestDispatcher(path).forward(request, response);
        return false;
    }
}
