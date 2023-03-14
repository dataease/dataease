package io.dataease.auth.server;

import io.dataease.auth.api.AuthApi;
import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.api.dto.LoginDto;
import io.dataease.auth.api.dto.SeizeLoginDto;
import io.dataease.auth.config.RsaProperties;
import io.dataease.auth.entity.AccountLockStatus;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.auth.entity.TokenInfo;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.util.JWTUtils;
import io.dataease.auth.util.RsaUtil;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.*;
import io.dataease.controller.sys.request.LdapAddRequest;
import io.dataease.exception.DataEaseException;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.entity.XpackLdapUserEntity;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.util.PluginUtils;
import io.dataease.plugins.xpack.cas.service.CasXpackService;
import io.dataease.plugins.xpack.ldap.dto.request.LdapValidateRequest;
import io.dataease.plugins.xpack.ldap.dto.response.ValidateResult;
import io.dataease.plugins.xpack.ldap.service.LdapXpackService;
import io.dataease.plugins.xpack.oidc.service.OidcXpackService;
import io.dataease.service.sys.SysUserService;

import io.dataease.service.system.SystemParameterService;
import io.dataease.websocket.entity.WsMessage;
import io.dataease.websocket.service.WsService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
public class AuthServer implements AuthApi {

    private static final String LDAP_EMAIL_SUFFIX = "@ldap.com";
    @Value("${dataease.init_password:DataEase123..}")
    private String DEFAULT_PWD;

    @Autowired
    private AuthUserService authUserService;

    @Autowired
    private SysUserService sysUserService;

    @Resource
    private SystemParameterService systemParameterService;

    @Autowired
    private WsService wsService;

    @Override
    public Object mobileLogin(@RequestBody LoginDto loginDto) throws Exception {
        String username = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, loginDto.getUsername());
        String pwd = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, loginDto.getPassword());
        AccountLockStatus accountLockStatus = authUserService.lockStatus(username, 0);
        if (accountLockStatus.getLocked()) {
            String msg = Translator.get("I18N_ACCOUNT_LOCKED");
            msg = String.format(msg, username, accountLockStatus.getRelieveTimes().toString());
            DataEaseException.throwException(msg);
        }

        SysUserEntity user = authUserService.getUserByName(username);

        if (ObjectUtils.isEmpty(user)) {
            AccountLockStatus lockStatus = authUserService.recordLoginFail(username, 0);
            DataEaseException.throwException(appendLoginErrorMsg(Translator.get("i18n_id_or_pwd_error"), lockStatus));
        }
        if (user.getEnabled() == 0) {
            AccountLockStatus lockStatus = authUserService.recordLoginFail(username, 0);
            DataEaseException.throwException(appendLoginErrorMsg(Translator.get("i18n_user_is_disable"), lockStatus));
        }
        String realPwd = user.getPassword();
        pwd = CodingUtil.md5(pwd);

        if (!StringUtils.equals(pwd, realPwd)) {
            AccountLockStatus lockStatus = authUserService.recordLoginFail(username, 0);
            DataEaseException.throwException(appendLoginErrorMsg(Translator.get("i18n_id_or_pwd_error"), lockStatus));
        }
        TokenInfo tokenInfo = TokenInfo.builder().userId(user.getUserId()).username(username).build();
        String token = JWTUtils.sign(tokenInfo, realPwd, false);
        // 记录token操作时间
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        ServletUtils.setToken(token);
        DeLogUtils.save(SysLogConstants.OPERATE_TYPE.LOGIN, SysLogConstants.SOURCE_TYPE.USER, user.getUserId(), null, null, null);
        authUserService.unlockAccount(username, 0);
        authUserService.clearCache(user.getUserId());
        return result;
    }

    @Override
    public Object login(@RequestBody LoginDto loginDto) throws Exception {
        Map<String, Object> result = new HashMap<>();
        String username = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, loginDto.getUsername());
        String pwd = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, loginDto.getPassword());

        // 增加ldap登录方式
        Integer loginType = loginDto.getLoginType();
        boolean isSupportLdap = authUserService.supportLdap();
        if (loginType == 1 && isSupportLdap) {
            AccountLockStatus accountLockStatus = authUserService.lockStatus(username, 1);
            if (accountLockStatus.getLocked()) {
                String msg = Translator.get("I18N_ACCOUNT_LOCKED");
                msg = String.format(msg, username, accountLockStatus.getRelieveTimes().toString());
                DataEaseException.throwException(msg);
            }
            LdapXpackService ldapXpackService = SpringContextUtil.getBean(LdapXpackService.class);
            LdapValidateRequest request = LdapValidateRequest.builder().userName(username).password(pwd).build();
            ValidateResult<XpackLdapUserEntity> validateResult = ldapXpackService.login(request);

            if (!validateResult.isSuccess()) {
                AccountLockStatus lockStatus = authUserService.recordLoginFail(username, 1);
                DataEaseException.throwException(appendLoginErrorMsg(validateResult.getMsg(), lockStatus));
            }
            XpackLdapUserEntity ldapUserEntity = validateResult.getData();
            if (StringUtils.isBlank(ldapUserEntity.getEmail())) {
                ldapUserEntity.setEmail(username + LDAP_EMAIL_SUFFIX);
            }
            SysUserEntity user = authUserService.getLdapUserByName(username);
            if (ObjectUtils.isEmpty(user) || ObjectUtils.isEmpty(user.getUserId())) {
                LdapAddRequest ldapAddRequest = new LdapAddRequest();
                ldapAddRequest.setUsers(new ArrayList<XpackLdapUserEntity>() {
                    {
                        add(ldapUserEntity);
                    }
                });
                ldapAddRequest.setEnabled(1L);
                ldapAddRequest.setRoleIds(new ArrayList<Long>() {
                    {
                        add(2L);
                    }
                });
                sysUserService.validateExistUser(ldapUserEntity.getUsername(), ldapUserEntity.getNickname(),
                        ldapUserEntity.getEmail());
                sysUserService.saveLdapUsers(ldapAddRequest);
            }

            username = validateResult.getData().getUsername();
        }
        // 增加ldap登录方式
        AccountLockStatus accountLockStatus = authUserService.lockStatus(username, 0);
        if (accountLockStatus.getLocked()) {
            String msg = Translator.get("I18N_ACCOUNT_LOCKED");
            msg = String.format(msg, username, accountLockStatus.getRelieveTimes().toString());
            DataEaseException.throwException(msg);
        }

        SysUserEntity user = authUserService.getUserByName(username);

        if (ObjectUtils.isEmpty(user)) {
            AccountLockStatus lockStatus = authUserService.recordLoginFail(username, 0);
            DataEaseException.throwException(appendLoginErrorMsg(Translator.get("i18n_id_or_pwd_error"), lockStatus));
        }

        // 验证登录类型是否与用户类型相同
        if (!sysUserService.validateLoginType(user.getFrom(), loginType)) {
            AccountLockStatus lockStatus = authUserService.recordLoginFail(username, 0);
            DataEaseException.throwException(appendLoginErrorMsg(Translator.get("i18n_login_type_error"), lockStatus));
        }

        if (user.getEnabled() == 0) {
            AccountLockStatus lockStatus = authUserService.recordLoginFail(username, 0);
            DataEaseException.throwException(appendLoginErrorMsg(Translator.get("i18n_user_is_disable"), lockStatus));
        }
        String realPwd = user.getPassword();

        // 普通登录需要验证密码
        if (loginType == 0 || !isSupportLdap) {
            // 私钥解密

            // md5加密
            pwd = CodingUtil.md5(pwd);

            if (!StringUtils.equals(pwd, realPwd)) {
                AccountLockStatus lockStatus = authUserService.recordLoginFail(username, 0);
                DataEaseException.throwException(appendLoginErrorMsg(Translator.get("i18n_id_or_pwd_error"), lockStatus));
            }
            if (user.getIsAdmin() && user.getPassword().equals("40b8893ea9ebc2d631c4bb42bb1e8996")) {
                result.put("passwordModified", false);
            }
        }

        TokenInfo tokenInfo = TokenInfo.builder().userId(user.getUserId()).username(username).build();
        String token = JWTUtils.sign(tokenInfo, realPwd);
        // 记录token操作时间
        result.put("token", token);
        ServletUtils.setToken(token);
        DeLogUtils.save(SysLogConstants.OPERATE_TYPE.LOGIN, SysLogConstants.SOURCE_TYPE.USER, user.getUserId(), null, null, null);
        authUserService.unlockAccount(username, ObjectUtils.isEmpty(loginType) ? 0 : loginType);
        authUserService.clearCache(user.getUserId());
        return result;
    }

    @Override
    public Object seizeLogin(@RequestBody SeizeLoginDto loginDto) throws Exception {
        String token = loginDto.getToken();
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        ServletUtils.setToken(token);
        TokenInfo tokenInfo = JWTUtils.tokenInfoByToken(token);
        Long userId = tokenInfo.getUserId();
        JWTUtils.seizeSign(userId, token);
        DeLogUtils.save(SysLogConstants.OPERATE_TYPE.LOGIN, SysLogConstants.SOURCE_TYPE.USER, userId, null, null, null);
        WsMessage message = new WsMessage(userId, "/web-seize-topic", IPUtils.get());
        wsService.releaseMessage(message);
        authUserService.clearCache(userId);
        Thread.sleep(3000L);
        return result;
    }

    private String appendLoginErrorMsg(String msg, AccountLockStatus lockStatus) {
        if (ObjectUtils.isEmpty(lockStatus)) return msg;
        if (ObjectUtils.isNotEmpty(lockStatus.getRemainderTimes())) {
            String i18n = Translator.get("i18n_login_remainder_times");
            msg += String.format(i18n, lockStatus.getRemainderTimes());
        }
        return msg;
    }

    @Override
    public CurrentUserDto userInfo() {
        CurrentUserDto userDto = (CurrentUserDto) SecurityUtils.getSubject().getPrincipal();
        if (ObjectUtils.isEmpty(userDto)) {
            String token = ServletUtils.getToken();
            Long userId = JWTUtils.tokenInfoByToken(token).getUserId();
            SysUserEntity user = authUserService.getUserById(userId);
            CurrentUserDto currentUserDto = BeanUtils.copyBean(new CurrentUserDto(), user, "password");
            List<CurrentRoleDto> currentRoleDtos = authUserService.roleInfos(user.getUserId());
            List<String> permissions = authUserService.permissions(user.getUserId());
            currentUserDto.setRoles(currentRoleDtos);
            currentUserDto.setPermissions(permissions);
            return currentUserDto;
        }
        userDto.setPassword(null);
        return userDto;
    }

    @Override
    public Boolean useInitPwd() {
        CurrentUserDto user = AuthUtils.getUser();
        if (null == user || 0 != user.getFrom()) {
            return false;
        }
        String md5 = CodingUtil.md5(DEFAULT_PWD);
        boolean isInitPwd = StringUtils.equals(AuthUtils.getUser().getPassword(), md5);
        if (isInitPwd) {
            return sysUserService.needPwdNoti(user.getUserId());
        }
        return false;
    }

    @Override
    public void removeNoti() {
        sysUserService.saveUserAssist(false);
    }

    @Override
    public String defaultPwd() {
        return DEFAULT_PWD;
    }

    @Override
    public String deLogout() {
        String token = ServletUtils.getToken();
        if (StringUtils.isEmpty(token) || StringUtils.equals("null", token) || StringUtils.equals("undefined", token)) {
            return "success";
        }
        SecurityUtils.getSubject().logout();
        String result = null;
        Integer defaultLoginType = systemParameterService.defaultLoginType();
        if (defaultLoginType == 3 && isOpenCas()) {
            HttpServletRequest request = ServletUtils.request();
            HttpSession session = request.getSession();
            session.invalidate();
            CasXpackService casXpackService = SpringContextUtil.getBean(CasXpackService.class);
            result = casXpackService.logout();
        }
        try {
            Long userId = JWTUtils.tokenInfoByToken(token).getUserId();
            authUserService.clearCache(userId);
            if (StringUtils.isBlank(result)) {
                result = "success";
            }
            TokenCacheUtils.add(token, userId);
        } catch (Exception e) {
            LogUtil.error(e);
            if (StringUtils.isBlank(result)) {
                result = "fail";
            }
        }
        return result;
    }

    @Override
    public String logout() {
        String token = ServletUtils.getToken();

        if (isOpenOidc()) {
            HttpServletRequest request = ServletUtils.request();
            String idToken = request.getHeader("IdToken");
            if (StringUtils.isNotBlank(idToken)) {
                try {
                    OidcXpackService oidcXpackService = SpringContextUtil.getBean(OidcXpackService.class);
                    oidcXpackService.logout(idToken);
                } catch (Exception e) {
                    LogUtil.error(e.getMessage(), e);
                    DEException.throwException("oidc_logout_error");
                }
            }
        }

        if (StringUtils.isEmpty(token) || StringUtils.equals("null", token) || StringUtils.equals("undefined", token)) {
            return "success";
        }

        SecurityUtils.getSubject().logout();
        String result = null;
        Integer defaultLoginType = systemParameterService.defaultLoginType();
        if (defaultLoginType == 3 && isOpenCas()) {
            try {
                HttpServletRequest request = ServletUtils.request();
                HttpSession session = request.getSession();
                session.invalidate();
                CasXpackService casXpackService = SpringContextUtil.getBean(CasXpackService.class);
                result = casXpackService.logout();
            } catch (Exception e) {
                LogUtil.error(e.getMessage(), e);
                DEException.throwException("cas_logout_error");
            }
        }
        try {
            Long userId = JWTUtils.tokenInfoByToken(token).getUserId();

            authUserService.clearCache(userId);
            if (StringUtils.isBlank(result)) {
                result = "success";
            }
            TokenCacheUtils.add(token, userId);
        } catch (Exception e) {
            LogUtil.error(e);
            if (StringUtils.isBlank(result)) {
                result = "fail";
            }
        }
        return result;
    }

    @Override
    public Boolean validateName(@RequestBody Map<String, String> nameDto) {
        String userName = nameDto.get("userName");
        if (StringUtils.isEmpty(userName))
            return false;
        SysUserEntity userEntity = authUserService.getUserByName(userName);
        return !ObjectUtils.isEmpty(userEntity);
    }

    @Override
    public boolean isOpenLdap() {
        Boolean licValid = PluginUtils.licValid();
        if (!licValid)
            return false;
        return authUserService.supportLdap();
    }

    @Override
    public boolean isOpenOidc() {
        Boolean licValid = PluginUtils.licValid();
        if (!licValid)
            return false;
        return authUserService.supportOidc();
    }


    @Override
    public boolean isOpenCas() {
        Boolean licValid = PluginUtils.licValid();
        if (!licValid)
            return false;

        return authUserService.supportCas();
    }

    @Override
    public boolean isOpenWecom() {
        Boolean licValid = PluginUtils.licValid();
        if (!licValid)
            return false;

        return authUserService.supportWecom();
    }

    @Override
    public boolean isOpenDingtalk() {
        Boolean licValid = PluginUtils.licValid();
        if (!licValid)
            return false;

        return authUserService.supportDingtalk();
    }

    @Override
    public boolean isOpenLark() {
        Boolean licValid = PluginUtils.licValid();
        if (!licValid)
            return false;

        return authUserService.supportLark();
    }

    @Override
    public boolean isOpenLarksuite() {
        Boolean licValid = PluginUtils.licValid();
        if (!licValid)
            return false;
        return authUserService.supportLarksuite();
    }

    @Override
    public boolean isPluginLoaded() {
        Boolean licValid = PluginUtils.licValid();
        if (!licValid)
            return false;
        return authUserService.pluginLoaded();
    }

    @Override
    public String getPublicKey() {
        return RsaProperties.publicKey;
    }

}
