package io.dataease.auth.config;

import io.dataease.auth.entity.JWTToken;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.util.JWTUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.Set;
import java.util.stream.Collectors;



@Component
public class F2CRealm extends AuthorizingRealm {

    @Resource
    private AuthUserService authUserService;


    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }

    //验证资源权限
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = JWTUtils.getUsername(principals.toString());
        SysUserEntity user = authUserService.getUser(username);
        Long userId = user.getUserId();
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        Set<String> role = authUserService.roles(userId).stream().collect(Collectors.toSet());
        simpleAuthorizationInfo.addRoles(role);
        Set<String> permission = authUserService.permissions(userId).stream().collect(Collectors.toSet());
        simpleAuthorizationInfo.addStringPermissions(permission);
        return simpleAuthorizationInfo;
    }

    //验证登录权限
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
        String token = (String) auth.getCredentials();
        // 解密获得username，用于和数据库进行对比
        String username = JWTUtils.getUsername(token);
        if (username == null) {
            throw new AuthenticationException("token invalid");
        }

        SysUserEntity user = authUserService.getUser(username);
        if (user == null) {
            throw new AuthenticationException("User didn't existed!");
        }
        String pass = null;
        try {
            /*pass = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, userBean.getPassword());*/
            pass = user.getPassword();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (! JWTUtils.verify(token, username, pass)) {
            throw new AuthenticationException("Username or password error");
        }
        return new SimpleAuthenticationInfo(token, token, "f2cReam");
    }
}
