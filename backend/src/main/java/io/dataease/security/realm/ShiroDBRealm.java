package io.dataease.security.realm;


import io.dataease.base.domain.Role;
import io.dataease.commons.constants.UserSource;
import io.dataease.commons.user.SessionUser;
import io.dataease.commons.utils.SessionUtils;
import io.dataease.dto.UserDTO;
import io.dataease.i18n.Translator;
import io.dataease.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 自定义Realm 注入service 可能会导致在 service的aop 失效，例如@Transactional,
 * 解决方法：
 * <p>
 * 1. 这里改成注入mapper，这样mapper 中的事务失效<br/>
 * 2. 这里仍然注入service，在配置ShiroConfig 的时候不去set realm, 等到spring 初始化完成之后
 * set realm
 * </p>
 */
public class ShiroDBRealm extends AuthorizingRealm {

    private Logger logger = LoggerFactory.getLogger(ShiroDBRealm.class);
    @Resource
    private UserService userService;

    @Value("${run.mode:release}")
    private String runMode;

    @Override
    public String getName() {
        return "LOCAL";
    }

    /**
     * 权限认证
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String userId = (String) principals.getPrimaryPrincipal();
        return getAuthorizationInfo(userId, userService);
    }

    public static AuthorizationInfo getAuthorizationInfo(String userId, UserService userService) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        // roles 内容填充
        UserDTO userDTO = userService.getUserDTO(userId);
        Set<String> roles = userDTO.getRoles().stream().map(Role::getId).collect(Collectors.toSet());
        authorizationInfo.setRoles(roles);
        return authorizationInfo;
    }

    /**
     * 登录认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String login = (String) SecurityUtils.getSubject().getSession().getAttribute("authenticate");

        String userId = token.getUsername();
        String password = String.valueOf(token.getPassword());

        if (StringUtils.equals("local", runMode)) {
            UserDTO user = getUserWithOutAuthenticate(userId);
            userId = user.getId();
            SessionUser sessionUser = SessionUser.fromUser(user);
            SessionUtils.putUser(sessionUser);
            return new SimpleAuthenticationInfo(userId, password, getName());
        }

        if (StringUtils.equals(login, UserSource.LOCAL.name())) {
            return loginLocalMode(userId, password);
        }

        UserDTO user = getUserWithOutAuthenticate(userId);
        userId = user.getId();
        SessionUser sessionUser = SessionUser.fromUser(user);
        SessionUtils.putUser(sessionUser);
        return new SimpleAuthenticationInfo(userId, password, getName());

    }

    private UserDTO getUserWithOutAuthenticate(String userId) {
        UserDTO user = userService.getUserDTO(userId);
        String msg;
        if (user == null) {
            user = userService.getUserDTOByEmail(userId);
            if (user == null) {
                msg = "The user does not exist: " + userId;
                logger.warn(msg);
                throw new UnknownAccountException(Translator.get("password_is_incorrect"));
            }
        }
        return user;
    }

    private AuthenticationInfo loginLocalMode(String userId, String password) {
        UserDTO user = userService.getLoginUser(userId, Collections.singletonList(UserSource.LOCAL.name()));
        String msg;
        if (user == null) {
            user = userService.getUserDTOByEmail(userId, UserSource.LOCAL.name());
            if (user == null) {
                msg = "The user does not exist: " + userId;
                logger.warn(msg);
                throw new UnknownAccountException(Translator.get("password_is_incorrect"));
            }
            userId = user.getId();
        }
        // 密码验证
        if (!userService.checkUserPassword(userId, password)) {
            throw new IncorrectCredentialsException(Translator.get("password_is_incorrect"));
        }
        SessionUser sessionUser = SessionUser.fromUser(user);
        SessionUtils.putUser(sessionUser);
        return new SimpleAuthenticationInfo(userId, password, getName());
    }

    @Override
    public boolean isPermitted(PrincipalCollection principals, String permission) {
        return true;
    }
}
