package io.dataease.auth.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.dataease.auth.util.JWTUtils;
import io.dataease.auth.util.LinkUtil;
import io.dataease.commons.utils.LogUtil;
import io.dataease.plugins.common.base.domain.PanelLink;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.web.filter.authc.AnonymousFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class F2CLinkFilter extends AnonymousFilter {

    public static final String LINK_TOKEN_KEY = "LINK-PWD-TOKEN";

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
        try {
            HttpServletRequest req = (HttpServletRequest) request;
            String linkToken = req.getHeader(LINK_TOKEN_KEY);
            DecodedJWT jwt = JWT.decode(linkToken);
            String resourceId = jwt.getClaim("resourceId").asString();
            Long userId = jwt.getClaim("userId").asLong();
            PanelLink panelLink = LinkUtil.queryLink(resourceId, userId);
            if (ObjectUtils.isEmpty(panelLink)) return false;
            String pwd;
            if (!panelLink.getEnablePwd()) {
                panelLink.setPwd("dataease");
                pwd = panelLink.getPwd();
            } else {
                pwd = panelLink.getPwd();
            }
            return JWTUtils.verifyLink(linkToken, resourceId, userId, pwd);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return false;

    }


}
