package io.dataease.auth.service.impl;

import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.entity.AccountLockStatus;
import io.dataease.auth.entity.SysUserEntity;
import io.dataease.commons.utils.CodingUtil;
import io.dataease.exception.DataEaseException;
import io.dataease.ext.*;
import io.dataease.auth.service.AuthUserService;
import io.dataease.commons.constants.AuthConstants;
import io.dataease.commons.utils.LogUtil;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.SysLoginLimit;
import io.dataease.plugins.common.base.domain.SysLoginLimitExample;
import io.dataease.plugins.common.base.domain.SysUser;
import io.dataease.plugins.common.base.mapper.SysLoginLimitMapper;
import io.dataease.plugins.common.base.mapper.SysUserMapper;
import io.dataease.plugins.common.service.PluginCommonService;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.util.PluginUtils;
import io.dataease.plugins.xpack.cas.service.CasXpackService;
import io.dataease.plugins.xpack.dingtalk.service.DingtalkXpackService;
import io.dataease.plugins.xpack.lark.service.LarkXpackService;
import io.dataease.plugins.xpack.ldap.service.LdapXpackService;
import io.dataease.plugins.xpack.loginlimit.dto.response.LoginLimitInfo;
import io.dataease.plugins.xpack.loginlimit.service.LoginLimitXpackService;
import io.dataease.plugins.xpack.oidc.service.OidcXpackService;

import io.dataease.plugins.xpack.wecom.service.WecomXpackService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthUserServiceImpl implements AuthUserService {


    @Resource
    private AuthMapper authMapper;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private DynamicMenuServiceImpl dynamicMenuService;

    @Resource
    private SysLoginLimitMapper sysLoginLimitMapper;

    /**
     * 此处需被F2CRealm登录认证调用 也就是说每次请求都会被调用 所以最好加上缓存
     *
     * @param userId
     * @return
     */
    @Cacheable(value = AuthConstants.USER_CACHE_NAME, key = "'user' + #userId")
    @Override
    public SysUserEntity getUserById(Long userId) {
        return authMapper.findUser(userId);
    }

    public SysUserEntity getUserByIdNoCache(Long userId) {
        return authMapper.findUser(userId);
    }

    @Override
    public SysUserEntity getUserByName(String username) {
        return authMapper.findUserByName(username);
    }


    @Override
    public SysUserEntity getLdapUserByName(String username) {
        return authMapper.findLdapUserByName(username);
    }

    @Override
    public SysUserEntity getCasUserByName(String username) {
        return authMapper.findCasUserByName(username);
    }

    @Override
    public SysUserEntity getUserBySub(String sub, Integer from) {
        return authMapper.findUserBySub(sub, from);
    }

    @Override
    public SysUserEntity getUserByWecomId(String weComId) {
        return authMapper.findWecomUser(weComId);
    }

    @Override
    public SysUserEntity getUserByDingtalkId(String dingtalkId) {
        return authMapper.findDingtalkUser(dingtalkId);
    }

    @Override
    public SysUserEntity getUserByLarkId(String larkId) {
        return authMapper.findLarkUser(larkId);
    }

    @Override
    public List<String> roles(Long userId) {
        return authMapper.roleCodes(userId);
    }

    /**
     * 此处需被F2CRealm登录认证调用 也就是说每次请求都会被调用 所以最好加上缓存
     *
     * @param userId
     * @return
     */
    @Cacheable(value = AuthConstants.USER_PERMISSION_CACHE_NAME, key = "'user' + #userId")
    @Override
    public List<String> permissions(Long userId) {
        try {
            // 用户登录获取菜单权限时同时更新插件菜单表
            dynamicMenuService.syncPluginMenu(PluginUtils.pluginMenus());
        } catch (Exception e) {
            LogUtil.error(e);
            //ignore
        }
        List<String> permissions;
        SysUser sysUser = sysUserMapper.selectByPrimaryKey(userId);
        if (sysUser.getIsAdmin() != null && sysUser.getIsAdmin()) {
            permissions = authMapper.permissionsAll();
        } else {
            permissions = authMapper.permissions(userId);
        }
        return Optional.ofNullable(permissions).orElse(new ArrayList<>()).stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
    }

    /**
     * 此处需被F2CRealm登录认证调用 也就是说每次请求都会被调用 所以最好加上缓存
     *
     * @param userId
     * @return
     */
    @Cacheable(value = AuthConstants.USER_ROLE_CACHE_NAME, key = "'user' + #userId")
    @Override
    public List<CurrentRoleDto> roleInfos(Long userId) {
        return authMapper.roles(userId);
    }


    /**
     * 一波清除3个缓存
     *
     * @param userId
     */
    @Caching(evict = {
            @CacheEvict(value = AuthConstants.USER_CACHE_NAME, key = "'user' + #userId"),
            @CacheEvict(value = AuthConstants.USER_ROLE_CACHE_NAME, key = "'user' + #userId"),
            @CacheEvict(value = AuthConstants.USER_PERMISSION_CACHE_NAME, key = "'user' + #userId")
    })
    @Override
    public void clearCache(Long userId) {
        LogUtil.info("正在清除用户缓存【{}】", userId);
    }

    @Override
    public boolean supportLdap() {
        Map<String, LdapXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((LdapXpackService.class));
        if (beansOfType.keySet().size() == 0) return false;
        LdapXpackService ldapXpackService = SpringContextUtil.getBean(LdapXpackService.class);
        if (ObjectUtils.isEmpty(ldapXpackService)) return false;
        return ldapXpackService.isOpen();
    }

    @Override
    public Boolean supportOidc() {
        Map<String, OidcXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((OidcXpackService.class));
        if (beansOfType.keySet().size() == 0) return false;
        OidcXpackService oidcXpackService = SpringContextUtil.getBean(OidcXpackService.class);
        if (ObjectUtils.isEmpty(oidcXpackService)) return false;
        return oidcXpackService.isSuuportOIDC();
    }

    @Override
    public Boolean supportCas() {
        Map<String, CasXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((CasXpackService.class));
        if (beansOfType.keySet().size() == 0) return false;
        CasXpackService casXpackService = SpringContextUtil.getBean(CasXpackService.class);
        if (ObjectUtils.isEmpty(casXpackService)) return false;
        return casXpackService.suuportCas();
    }

    @Override
    public Boolean supportWecom() {
        Map<String, WecomXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((WecomXpackService.class));
        if (beansOfType.keySet().size() == 0) return false;
        WecomXpackService wecomXpackService = SpringContextUtil.getBean(WecomXpackService.class);
        if (ObjectUtils.isEmpty(wecomXpackService)) return false;
        return wecomXpackService.isOpen();
    }

    @Override
    public Boolean supportDingtalk() {
        Map<String, DingtalkXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((DingtalkXpackService.class));
        if (beansOfType.keySet().size() == 0) return false;
        DingtalkXpackService dingtalkXpackService = SpringContextUtil.getBean(DingtalkXpackService.class);
        if (ObjectUtils.isEmpty(dingtalkXpackService)) return false;
        return dingtalkXpackService.isOpen();
    }

    @Override
    public Boolean supportLark() {
        Map<String, LarkXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((LarkXpackService.class));
        if (beansOfType.keySet().size() == 0) return false;
        LarkXpackService larkXpackService = SpringContextUtil.getBean(LarkXpackService.class);
        if (ObjectUtils.isEmpty(larkXpackService)) return false;
        return larkXpackService.isOpen();
    }

    @Override
    public Boolean supportLoginLimit() {
        Map<String, LoginLimitXpackService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((LoginLimitXpackService.class));
        if (beansOfType.keySet().size() == 0) return false;
        LoginLimitXpackService loginLimitXpackService = SpringContextUtil.getBean(LoginLimitXpackService.class);
        if (ObjectUtils.isEmpty(loginLimitXpackService)) return false;
        return loginLimitXpackService.isOpen();
    }

    @Override
    public Boolean pluginLoaded() {
        Map<String, PluginCommonService> beansOfType = SpringContextUtil.getApplicationContext().getBeansOfType((PluginCommonService.class));
        if (beansOfType.keySet().size() == 0) return false;
        PluginCommonService pluginCommonService = SpringContextUtil.getBean(PluginCommonService.class);
        if (ObjectUtils.isEmpty(pluginCommonService)) return false;
        return pluginCommonService.isPluginLoaded();
    }

    @Override
    public void checkAdmin(String uname, String pwd) {

        SysUserEntity user = getUserByName(uname);
        if (ObjectUtils.isEmpty(user)) {
            DataEaseException.throwException(Translator.get("i18n_user_not_exist"));
        }
        if (!user.getIsAdmin()) {
            DataEaseException.throwException(Translator.get("i18n_not_admin_error"));
        }
        String realPwd = user.getPassword();
        pwd = CodingUtil.md5(pwd);
        if (!StringUtils.equals(pwd, realPwd)) {
            DataEaseException.throwException(Translator.get("i18n_id_or_pwd_error"));
        }
    }

    @Override
    public void recordLoginFail(String username, Integer logintype) {
        if (!supportLoginLimit()) return;
        long now = System.currentTimeMillis();
        SysLoginLimit sysLoginLimit = new SysLoginLimit();
        sysLoginLimit.setUsername(username);
        sysLoginLimit.setLoginType(logintype);
        sysLoginLimit.setRecordTime(now);
        sysLoginLimitMapper.insert(sysLoginLimit);
    }

    @Override
    public void unlockAccount(String username, Integer logintype) {
        SysLoginLimitExample example = new SysLoginLimitExample();
        example.createCriteria().andUsernameEqualTo(username).andLoginTypeEqualTo(logintype);
        sysLoginLimitMapper.deleteByExample(example);
    }

    @Override
    public AccountLockStatus lockStatus(String username, Integer logintype) {
        AccountLockStatus accountLockStatus = new AccountLockStatus();
        accountLockStatus.setUsername(username);
        if (!supportLoginLimit()) return accountLockStatus;

        LoginLimitXpackService loginLimitXpackService = SpringContextUtil.getBean(LoginLimitXpackService.class);
        LoginLimitInfo info = loginLimitXpackService.info();
        Integer limitTimes = info.getLimitTimes();
        Integer relieveTimes = info.getRelieveTimes();

        long now = System.currentTimeMillis();

        long longRelieveTimes = Long.parseLong(relieveTimes.toString());
        long dividingPointTime = now - (longRelieveTimes * 60L * 1000L);
        SysLoginLimitExample example = new SysLoginLimitExample();
        example.createCriteria().andUsernameEqualTo(username).andLoginTypeEqualTo(logintype).andRecordTimeGreaterThan(dividingPointTime);
        List<SysLoginLimit> sysLoginLimits = sysLoginLimitMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(sysLoginLimits)) {
            boolean needLock = sysLoginLimits.size() >= limitTimes;
            accountLockStatus.setLocked(needLock);
            if (needLock) {
                long unlockTime = now + (longRelieveTimes * 60L * 1000L);
                accountLockStatus.setUnlockTime(unlockTime);
                accountLockStatus.setRelieveTimes(relieveTimes);
            }

        }
        example.clear();
        example.createCriteria().andUsernameEqualTo(username).andLoginTypeEqualTo(logintype).andRecordTimeLessThanOrEqualTo(dividingPointTime);
        sysLoginLimitMapper.deleteByExample(example);
        return accountLockStatus;
    }

    @Override
    public void clearAllLock() {
        SysLoginLimitExample example = new SysLoginLimitExample();
        sysLoginLimitMapper.deleteByExample(example);
    }
}
