package io.dataease.auth.server;

import io.dataease.auth.api.AuthApi;
import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.api.dto.LoginDto;
import io.dataease.auth.config.RsaProperties;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.util.JWTUtils;
import io.dataease.auth.util.RsaUtil;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.CodingUtil;
import io.dataease.commons.utils.LogUtil;
import io.dataease.commons.utils.ServletUtils;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.util.PluginUtils;
import io.dataease.plugins.xpack.ldap.dto.request.LdapValidateRequest;
import io.dataease.plugins.xpack.ldap.dto.response.ValidateResult;
import io.dataease.plugins.xpack.ldap.service.LdapXpackService;
import io.dataease.plugins.xpack.oidc.service.OidcXpackService;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AuthServer implements AuthApi {

    @Autowired
    private AuthUserService authUserService;




    @Override
    public Object login(@RequestBody LoginDto loginDto) throws Exception {
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();


        String pwd = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, password);
        // 增加ldap登录方式
        Integer loginType = loginDto.getLoginType();
        boolean isSupportLdap = authUserService.supportLdap();
        if (loginType == 1 && isSupportLdap) {
            LdapXpackService ldapXpackService = SpringContextUtil.getBean(LdapXpackService.class);
            LdapValidateRequest request = LdapValidateRequest.builder().userName(username).password(pwd).build();
            ValidateResult validateResult = ldapXpackService.login(request);
            if (!validateResult.isSuccess()) {
                DataEaseException.throwException(validateResult.getMsg());
            }
            username = validateResult.getUserName();
        }
        // 增加ldap登录方式

        SysUserEntity user = authUserService.getUserByName(username);

        if (ObjectUtils.isEmpty(user)) {
            DataEaseException.throwException(Translator.get("i18n_id_or_pwd_error"));
        }
        if (user.getEnabled() == 0) {
            DataEaseException.throwException(Translator.get("i18n_id_or_pwd_error"));
        }
        String realPwd = user.getPassword();

        // 普通登录需要验证密码
        if (loginType == 0 || !isSupportLdap) {
            //私钥解密

            //md5加密
            pwd = CodingUtil.md5(pwd);

            if (!StringUtils.equals(pwd, realPwd)) {
                DataEaseException.throwException(Translator.get("i18n_id_or_pwd_error"));
            }
        }

        Map<String, Object> result = new HashMap<>();
        TokenInfo tokenInfo = TokenInfo.builder().userId(user.getUserId()).username(username).build();
        String token = JWTUtils.sign(tokenInfo, realPwd);
        // 记录token操作时间
        result.put("token", token);
        ServletUtils.setToken(token);
        return result;
    }

    @Override
    public CurrentUserDto userInfo() {
        CurrentUserDto userDto = (CurrentUserDto) SecurityUtils.getSubject().getPrincipal();
        if (ObjectUtils.isEmpty(userDto)) {
            String token = ServletUtils.getToken();
            Long userId = JWTUtils.tokenInfoByToken(token).getUserId();
            SysUserEntity user = authUserService.getUserById(userId);
            CurrentUserDto currentUserDto = BeanUtils.copyBean(new CurrentUserDto(), user);
            List<CurrentRoleDto> currentRoleDtos = authUserService.roleInfos(user.getUserId());
            List<String> permissions = authUserService.permissions(user.getUserId());
            currentUserDto.setRoles(currentRoleDtos);
            currentUserDto.setPermissions(permissions);
            return currentUserDto;
        }
        return userDto;
    }

    @Override
    public String logout() {
        String token = ServletUtils.getToken();
        if (isOpenOidc()) {
            OidcXpackService oidcXpackService = SpringContextUtil.getBean(OidcXpackService.class);
            TokenInfo tokenInfo = JWTUtils.tokenInfoByToken(token);
            String idToken = tokenInfo.getIdToken();
            oidcXpackService.logout(idToken);
        }
        // String token = ServletUtils.getToken();
        if (StringUtils.isEmpty(token) || StringUtils.equals("null", token) || StringUtils.equals("undefined", token)) {
            return "success";
        }
        try{
            Long userId = JWTUtils.tokenInfoByToken(token).getUserId();
            authUserService.clearCache(userId);
        }catch (Exception e) {
            LogUtil.error(e);
            return "fail";
        }

        return "success";
    }

    @Override
    public Boolean validateName(@RequestBody Map<String, String> nameDto) {
        String userName = nameDto.get("userName");
        if (StringUtils.isEmpty(userName)) return false;
        SysUserEntity userEntity = authUserService.getUserByName(userName);
        if (ObjectUtils.isEmpty(userEntity)) return false;
        return true;
    }

    @Override
    public boolean isOpenLdap() {
        Boolean licValid = PluginUtils.licValid();
        if(!licValid) return false;
        boolean open = authUserService.supportLdap();
        return open;
    }

    @Override
    public boolean isOpenOidc() {
        Boolean licValid = PluginUtils.licValid();
        if(!licValid) return false;
        return authUserService.supportOidc();
    }

    

    /*@Override
    public Boolean isLogin() {
        return null;
    }*/


}
