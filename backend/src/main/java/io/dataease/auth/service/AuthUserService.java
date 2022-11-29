package io.dataease.auth.service;

import io.dataease.auth.api.dto.CurrentRoleDto;
import io.dataease.auth.entity.AccountLockStatus;
import io.dataease.auth.entity.SysUserEntity;

import java.util.List;

public interface AuthUserService {


    SysUserEntity getUserById(Long userId);

    SysUserEntity getUserByName(String username);

    SysUserEntity getLdapUserByName(String username);

    SysUserEntity getCasUserByName(String username);

    SysUserEntity getUserBySub(String sub, Integer from);

    SysUserEntity getUserByWecomId(String weComId);

    SysUserEntity getUserByDingtalkId(String dingtalkId);

    SysUserEntity getUserByLarkId(String larkId);

    SysUserEntity getUserByLarksuiteId(String larksuiteId);

    List<String> roles(Long userId);

    List<String> permissions(Long userId);

    List<CurrentRoleDto> roleInfos(Long userId);

    void clearCache(Long userId);

    boolean supportLdap();

    Boolean supportOidc();

    Boolean supportCas();

    Boolean supportWecom();

    Boolean supportDingtalk();

    Boolean supportLark();

    Boolean supportLarksuite();

    Boolean supportLoginLimit();

    Boolean pluginLoaded();

    void checkAdmin(String uname, String pwd);

    AccountLockStatus recordLoginFail(String username, Integer logintype);

    void unlockAccount(String username, Integer logintype);

    AccountLockStatus lockStatus(String username, Integer logintype);

    void clearAllLock();

    Boolean checkScanCreateLimit();


}
