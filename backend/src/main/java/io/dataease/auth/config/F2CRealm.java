package io.dataease.auth.config;

import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.entity.JWTToken;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.util.JWTUtils;
import io.dataease.commons.utils.BeanUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;



@Component
public class F2CRealm extends AuthorizingRealm {

    @Autowired
    @Lazy //shiro组件加载过早 让authUserService等一等再注入 否则 注入的可能不是代理对象
    private AuthUserService authUserService;


    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken;
    }

    //验证资源权限
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        Long userId = JWTUtils.tokenInfoByToken(principals.toString()).getUserId();
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
        TokenInfo tokenInfo = JWTUtils.tokenInfoByToken(token);
        Long userId = tokenInfo.getUserId();
        String username = tokenInfo.getUsername();
        if (username == null) {
            throw new AuthenticationException("token invalid");
        }
        // 使用缓存
        SysUserEntity user = authUserService.getUserById(userId);
        if (user == null) {
            throw new AuthenticationException("User didn't existed!");
        }
        if (user.getEnabled()==0) {
            throw new AuthenticationException("User is valid!");
        }
        String pass = null;
        try {
            pass = user.getPassword();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (! JWTUtils.verify(token, tokenInfo, pass)) {
            throw new AuthenticationException("Username or password error");
        }
        // 使用缓存
        List<CurrentRoleDto> currentRoleDtos = authUserService.roleInfos(user.getUserId());
        // 使用缓存
        List<String> permissions = authUserService.permissions(user.getUserId());
        CurrentUserDto currentUserDto = BeanUtils.copyBean(new CurrentUserDto(), user);
        currentUserDto.setRoles(currentRoleDtos);
        currentUserDto.setPermissions(permissions);
        return new SimpleAuthenticationInfo(currentUserDto, token, "f2cReam");
    }
}
