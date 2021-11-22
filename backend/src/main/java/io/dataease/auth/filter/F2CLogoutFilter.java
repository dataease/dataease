package io.dataease.auth.filter;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class F2CLogoutFilter extends LogoutFilter {

    private static final Logger logger = LoggerFactory.getLogger(F2CLogoutFilter.class);


    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        Subject subject = getSubject(request, response);
        try {
            subject.logout();
        } catch (Exception ex) {
            logger.error("退出登录错误", ex);
        }
        return true;
    }
}
