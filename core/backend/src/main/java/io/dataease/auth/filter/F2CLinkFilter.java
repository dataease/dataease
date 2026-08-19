package io.dataease.auth.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.dataease.auth.util.JWTUtils;
import io.dataease.auth.util.LinkSecretProvider;
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
            // 分享已关闭或已过期则拒绝访问
            if (!Boolean.TRUE.equals(panelLink.getValid())) return false;
            Long overTime = panelLink.getOverTime();
            if (overTime != null && System.currentTimeMillis() > overTime) return false;
            // 未启用密码的分享使用服务端随机密钥（不可伪造），启用密码的使用面板真实密码
            String pwd = panelLink.getEnablePwd() ? panelLink.getPwd() : LinkSecretProvider.linkSecret();
            return JWTUtils.verifyLink(linkToken, resourceId, userId, pwd);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return false;

    }


}
