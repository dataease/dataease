package io.dataease.auth.filter;

import cn.hutool.core.util.ObjectUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.dataease.auth.config.RsaProperties;
import io.dataease.auth.util.JWTUtils;
import io.dataease.auth.util.LinkUtil;
import io.dataease.auth.util.RsaUtil;
import io.dataease.base.domain.PanelLink;
import io.dataease.commons.utils.LogUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class F2CLinkFilter extends AnonymousFilter {

    private static final Logger logger = LoggerFactory.getLogger(F2CLogoutFilter.class);

    private static final String LINK_TOKEN_KEY = "LINK-PWD-TOKEN";

    @Override
    protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
        try{
            HttpServletRequest req = (HttpServletRequest) request;
            String link_token = req.getHeader(LINK_TOKEN_KEY);
            DecodedJWT jwt = JWT.decode(link_token);
            Claim resourceId = jwt.getClaim("resourceId");
            String id = resourceId.asString();
            PanelLink panelLink = LinkUtil.queryLink(id);
            if (ObjectUtil.isEmpty(panelLink)) return false;
            String pwd;
            if (!panelLink.getEnablePwd()) {
                panelLink.setPwd("dataease");
                pwd = panelLink.getPwd();
            }else {
                pwd = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, panelLink.getPwd());
            }
            return JWTUtils.verifyLink(link_token, id, pwd);
        }catch (Exception e) {
            LogUtil.error(e);
        }
        return false;

    }




}
