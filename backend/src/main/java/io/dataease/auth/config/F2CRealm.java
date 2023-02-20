package io.dataease.auth.config;

import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.entity.ASKToken;
import io.dataease.auth.entity.JWTToken;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.handler.ApiKeyHandler;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.util.JWTUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.LogUtil;
import io.dataease.commons.utils.TokenCacheUtils;
import io.dataease.listener.util.CacheUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class F2CRealm extends AuthorizingRealm {

    @Autowired
    @Lazy // shiro组件加载过早 让authUserService等一等再注入 否则 注入的可能不是代理对象
    private AuthUserService authUserService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JWTToken || token instanceof ASKToken;
    }

    // 验证资源权限
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        CurrentUserDto userDto = (CurrentUserDto) principals.getPrimaryPrincipal();
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        Set<String> role = new HashSet<>(
                userDto.getRoles().stream().map(item -> (item.getId() + "")).collect(Collectors.toSet()));
        simpleAuthorizationInfo.addRoles(role);
        Set<String> permission = new HashSet<>(userDto.getPermissions());
        simpleAuthorizationInfo.addStringPermissions(permission);
        return simpleAuthorizationInfo;
    }

    // 验证登录权限

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {

        if (auth instanceof ASKToken) {
            if (!authUserService.pluginLoaded()) {
                throw new AuthenticationException("license error");
            }

            Object accessKey = auth.getPrincipal();
            Object signature = auth.getCredentials();
            Long userId = ApiKeyHandler.getUser(accessKey.toString(), signature.toString());

            SysUserEntity userEntity = userWithId(userId);
            CurrentUserDto currentUserDto = queryCacheUserDto(userEntity);
            return new SimpleAuthenticationInfo(currentUserDto, signature, "f2cReam");
        }

        try {
            CacheUtils.get("lic_info", "lic");
        } catch (Exception e) {
            LogUtil.error(e);
            throw new AuthenticationException("license error");
        }

        TokenInfo tokenInfo;
        String token;
        try {
            token = (String) auth.getCredentials();
            // 解密获得username，用于和数据库进行对比
            tokenInfo = JWTUtils.tokenInfoByToken(token);
            if (TokenCacheUtils.invalid(token)) {
                throw new AuthenticationException("token invalid");
            }
        } catch (Exception e) {
            throw new AuthenticationException(e);
        }

        Long userId = tokenInfo.getUserId();
        String username = tokenInfo.getUsername();
        if (username == null) {
            throw new AuthenticationException("token invalid");
        }

        SysUserEntity user = userWithId(userId);
        String pass = null;
        try {
            pass = user.getPassword();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!JWTUtils.verify(token, tokenInfo, pass)) {
            throw new AuthenticationException("Username or password error");
        }

        CurrentUserDto currentUserDto = queryCacheUserDto(user);
        return new SimpleAuthenticationInfo(currentUserDto, token, "f2cReam");
    }

    public SysUserEntity userWithId(Long userId) {
        SysUserEntity user = authUserService.getUserById(userId);
        if (user == null) {
            throw new AuthenticationException("User didn't existed!");
        }
        if (user.getEnabled() == 0) {
            throw new AuthenticationException("User is valid!");
        }
        return user;
    }

    public CurrentUserDto queryCacheUserDto(SysUserEntity user) {
        // 使用缓存
        List<CurrentRoleDto> currentRoleDtos = authUserService.roleInfos(user.getUserId());
        // 使用缓存
        List<String> permissions = authUserService.permissions(user.getUserId());
        CurrentUserDto currentUserDto = BeanUtils.copyBean(new CurrentUserDto(), user);
        currentUserDto.setRoles(currentRoleDtos);
        currentUserDto.setPermissions(permissions);
        return currentUserDto;
    }
}
