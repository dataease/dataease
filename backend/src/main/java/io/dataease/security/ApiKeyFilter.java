package io.dataease.security;

import io.dataease.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class ApiKeyFilter extends AnonymousFilter {

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
        try {
            if (!SecurityUtils.getSubject().isAuthenticated()) {
                String userId = ApiKeyHandler.getUser(WebUtils.toHttp(request));
                if (StringUtils.isNotBlank(userId)) {
                    if (LogUtil.getLogger().isDebugEnabled()) {
                        LogUtil.getLogger().debug("user auth: " + userId);
                    }
                    SecurityUtils.getSubject().login(new MsUserToken(userId, ApiKeySessionHandler.random, "LOCAL"));
                }
            } else {
                if (ApiKeyHandler.isApiKeyCall(WebUtils.toHttp(request))) {
                    String userId = ApiKeyHandler.getUser(WebUtils.toHttp(request));
                    SecurityUtils.getSubject().login(new MsUserToken(userId, ApiKeySessionHandler.random, "LOCAL"));
                }
            }

            if (!SecurityUtils.getSubject().isAuthenticated()) {
                ((HttpServletResponse) response).setHeader("Authentication-Status", "invalid");
            }
        } catch (Exception e) {
            if (ApiKeyHandler.isApiKeyCall(WebUtils.toHttp(request))) {
                throw e;
            }
            LogUtil.getLogger().error("failed to handle single sign on..", e);
        }

        return true;
    }
}
