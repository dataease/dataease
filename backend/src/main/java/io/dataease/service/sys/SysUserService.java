package io.dataease.service.sys;

import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.auth.service.AuthUserService;
import io.dataease.auth.service.ExtAuthService;
import io.dataease.commons.exception.DEException;
import io.dataease.controller.sys.request.*;
import io.dataease.ext.ExtSysUserMapper;
import io.dataease.ext.query.GridExample;
import io.dataease.commons.constants.AuthConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.CodingUtil;
import io.dataease.controller.sys.response.SysUserGridResponse;
import io.dataease.controller.sys.response.SysUserRole;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.SysUserAssistMapper;
import io.dataease.plugins.common.base.mapper.SysUserMapper;
import io.dataease.plugins.common.base.mapper.SysUsersRolesMapper;
import io.dataease.plugins.common.entity.XpackLdapUserEntity;
import io.dataease.plugins.xpack.dingtalk.dto.response.DingUserEntity;
import io.dataease.plugins.xpack.lark.dto.entity.LarkUserInfo;
import io.dataease.plugins.xpack.larksuite.dto.entity.UserData;
import io.dataease.plugins.xpack.oidc.dto.SSOUserInfo;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SysUserService {


    @Value("${dataease.init_password:DataEase123..}")
    private String DEFAULT_PWD;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysUsersRolesMapper sysUsersRolesMapper;

    @Resource
    private ExtSysUserMapper extSysUserMapper;

    @Resource
    private ExtAuthService extAuthService;


    @Resource
    private SysUserAssistMapper sysUserAssistMapper;

    @Resource
    private AuthUserService authUserService;


    public List<SysUserGridResponse> query(KeyGridRequest request) {
        String keyWord = request.getKeyWord();
        GridExample gridExample = request.convertExample();
        gridExample.setExtendCondition(keyWord);
        List<SysUserGridResponse> lists = extSysUserMapper.query(gridExample);
        lists.forEach(item -> {
            List<SysUserRole> roles = item.getRoles();
            List<Long> roleIds = roles.stream().filter(ObjectUtils::isNotEmpty).map(SysUserRole::getRoleId).collect(Collectors.toList());
            item.setRoleIds(roleIds);
        });
        return lists;
    }

    @Transactional
    public int save(SysUserCreateRequest request) {
        request.setUsername(request.getUsername().trim());
        checkUsername(request);
        checkEmail(request);
        checkNickName(request);
        SysUser user = BeanUtils.copyBean(new SysUser(), request);
        long now = System.currentTimeMillis();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setIsAdmin(false);
        user.setFrom(0);
        if (ObjectUtils.isEmpty(user.getPassword()) || StringUtils.equals(user.getPassword(), DEFAULT_PWD)) {
            user.setPassword(CodingUtil.md5(DEFAULT_PWD));
        } else {
            user.setPassword(CodingUtil.md5(user.getPassword()));
        }
        if (StringUtils.isEmpty(user.getLanguage())) {
            user.setLanguage("zh_CN");
        }
        int insert = sysUserMapper.insert(user);
        SysUser dbUser = findOne(user);
        Long userId = dbUser.getUserId();
        request.setUserId(userId);
        saveUserRoles(userId, request.getRoleIds());//插入用户角色关联

        SysUserAssist sysUserAssist = request.getSysUserAssist();
        if (ObjectUtils.isNotEmpty(sysUserAssist) && (StringUtils.isNotBlank(sysUserAssist.getWecomId()) || StringUtils.isNotBlank(sysUserAssist.getDingtalkId()) || StringUtils.isNotBlank(sysUserAssist.getLarkId()))) {
            saveAssist(userId, sysUserAssist.getWecomId(), sysUserAssist.getDingtalkId(), sysUserAssist.getLarkId(), sysUserAssist.getLarksuiteId());
        }

        return insert;
    }

    @Transactional
    public void saveOIDCUser(SSOUserInfo ssoUserInfo) {
        long now = System.currentTimeMillis();
        SysUser sysUser = new SysUser();
        sysUser.setUsername(ssoUserInfo.getUsername());
        sysUser.setNickName(ssoUserInfo.getNickName());
        sysUser.setEmail(ssoUserInfo.getEmail());
        sysUser.setPassword(CodingUtil.md5(DEFAULT_PWD));
        sysUser.setCreateTime(now);
        sysUser.setUpdateTime(now);
        sysUser.setEnabled(1L);
        sysUser.setLanguage("zh_CN");
        sysUser.setFrom(2);
        sysUser.setIsAdmin(false);
        sysUser.setSub(ssoUserInfo.getSub());
        sysUserMapper.insert(sysUser);
        SysUser dbUser = findOne(sysUser);
        if (null != dbUser && null != dbUser.getUserId()) {
            // oidc默认角色是普通员工
            List<Long> roleIds = new ArrayList<Long>();
            roleIds.add(2L);
            saveUserRoles(dbUser.getUserId(), roleIds);
        }
    }

    @Transactional
    public void saveWecomCUser(Map<String, Object> userMap, String userId, String email) {
        long now = System.currentTimeMillis();
        SysUser sysUser = new SysUser();

        sysUser.setUsername(userId);
        sysUser.setNickName(userMap.get("name").toString());
        sysUser.setEmail(email);
        sysUser.setPassword(CodingUtil.md5(DEFAULT_PWD));
        sysUser.setCreateTime(now);
        sysUser.setUpdateTime(now);

        sysUser.setEnabled((ObjectUtils.isNotEmpty(userMap.get("status")) && 1 == Integer.parseInt(userMap.get("status").toString().split("\\.")[0])) ? 1L : 0L);
        sysUser.setGender((ObjectUtils.isEmpty(userMap.get("gender")) || "1".equals(userMap.get("gender"))) ? "男" : "女");
        sysUser.setLanguage("zh_CN");
        sysUser.setFrom(4);
        sysUser.setIsAdmin(false);
        sysUser.setSub(userId);
        sysUserMapper.insert(sysUser);
        Optional.ofNullable(findOne(sysUser)).ifPresent(u -> saveAssist(u.getUserId(), u.getUsername(), null, null, null));

    }

    @Transactional
    public void saveDingtalkCUser(DingUserEntity dingUserEntity, String email) {
        long now = System.currentTimeMillis();
        SysUser sysUser = new SysUser();

        sysUser.setUsername(dingUserEntity.getUserid());
        sysUser.setNickName(dingUserEntity.getName());
        sysUser.setEmail(email);
        sysUser.setPassword(CodingUtil.md5(DEFAULT_PWD));
        sysUser.setCreateTime(now);
        sysUser.setUpdateTime(now);

        sysUser.setEnabled(1L);
        sysUser.setLanguage("zh_CN");
        sysUser.setFrom(5);
        sysUser.setIsAdmin(false);
        sysUser.setSub(dingUserEntity.getUnionid());
        sysUser.setPhone(dingUserEntity.getMobile());
        sysUserMapper.insert(sysUser);
        Optional.ofNullable(findOne(sysUser)).ifPresent(u -> saveAssist(u.getUserId(), null, u.getUsername(), null, null));
    }

    @Transactional
    public void saveLarkCUser(LarkUserInfo larkUserInfo, String email) {
        long now = System.currentTimeMillis();
        SysUser sysUser = new SysUser();

        sysUser.setUsername(larkUserInfo.getUser_id());
        sysUser.setNickName(larkUserInfo.getName());
        sysUser.setEmail(email);
        sysUser.setPassword(CodingUtil.md5(DEFAULT_PWD));
        sysUser.setCreateTime(now);
        sysUser.setUpdateTime(now);

        sysUser.setEnabled(1L);
        sysUser.setLanguage("zh_CN");
        sysUser.setFrom(6);
        sysUser.setIsAdmin(false);
        sysUser.setSub(larkUserInfo.getSub());
        sysUser.setPhone(larkUserInfo.getMobile());
        sysUserMapper.insert(sysUser);
        Optional.ofNullable(findOne(sysUser)).ifPresent(u -> saveAssist(u.getUserId(), null, null, u.getUsername(), null));
    }

    @Transactional
    public void saveLarksuiteCUser(UserData larkUserInfo, String email) {
        long now = System.currentTimeMillis();
        SysUser sysUser = new SysUser();

        sysUser.setUsername(larkUserInfo.getUser_id());
        sysUser.setNickName(larkUserInfo.getName());
        sysUser.setEmail(email);
        sysUser.setPassword(CodingUtil.md5(DEFAULT_PWD));
        sysUser.setCreateTime(now);
        sysUser.setUpdateTime(now);

        sysUser.setEnabled(1L);
        sysUser.setLanguage("zh_CN");
        sysUser.setFrom(7);
        sysUser.setIsAdmin(false);
        sysUser.setSub(larkUserInfo.getUnion_id());
        sysUser.setPhone(larkUserInfo.getMobile());
        sysUserMapper.insert(sysUser);
        Optional.ofNullable(findOne(sysUser)).ifPresent(u -> saveAssist(u.getUserId(), null, null, null, u.getUsername()));
    }

    @Transactional
    public void saveCASUser(String name, String email) {
        long now = System.currentTimeMillis();
        SysUser sysUser = new SysUser();
        sysUser.setUsername(name);
        sysUser.setNickName(name);
        sysUser.setEmail(email);
        sysUser.setPassword(CodingUtil.md5(DEFAULT_PWD));
        sysUser.setCreateTime(now);
        sysUser.setUpdateTime(now);
        sysUser.setEnabled(1L);
        sysUser.setLanguage("zh_CN");
        sysUser.setFrom(3);
        sysUser.setIsAdmin(false);
        sysUserMapper.insert(sysUser);
        SysUser dbUser = findOne(sysUser);
        if (null != dbUser && null != dbUser.getUserId()) {
            // oidc默认角色是普通员工
            List<Long> roleIds = new ArrayList<Long>();
            roleIds.add(2L);
            saveUserRoles(dbUser.getUserId(), roleIds);
        }
    }

    public String defaultPWD() {
        return DEFAULT_PWD;
    }

    @Transactional
    public void saveLdapUsers(LdapAddRequest request) {
        long now = System.currentTimeMillis();

        List<XpackLdapUserEntity> users = request.getUsers();
        List<SysUser> sysUsers = users.stream().map(user -> {
            SysUser sysUser = BeanUtils.copyBean(new SysUser(), user);
            sysUser.setUsername(user.getUsername());
            sysUser.setNickName(user.getNickname());
            sysUser.setDeptId(request.getDeptId());
            sysUser.setPassword(CodingUtil.md5(DEFAULT_PWD));
            sysUser.setCreateTime(now);
            sysUser.setUpdateTime(now);
            sysUser.setEnabled(request.getEnabled());
            sysUser.setLanguage("zh_CN");
            sysUser.setIsAdmin(false);
            sysUser.setFrom(1);
            return sysUser;
        }).collect(Collectors.toList());

        sysUsers.forEach(sysUser -> {
            sysUserMapper.insert(sysUser);
            SysUser dbUser = findOne(sysUser);
            if (null != dbUser && null != dbUser.getUserId()) {
                saveUserRoles(dbUser.getUserId(), request.getRoleIds());
            }
        });
    }

    public boolean validateLoginType(Integer from, Integer loginType) {

        return ObjectUtils.isNotEmpty(from) && ObjectUtils.isNotEmpty(loginType) && from == loginType;
    }

    public List<String> ldapUserNames() {

        List<String> usernames = extSysUserMapper.ldapUserNames(1);
        return usernames;

    }

    /**
     * 修改用户密码清除缓存
     *
     * @param request
     * @return
     */
    @Transactional
    public int update(SysUserCreateRequest request) {
        checkUsername(request);
        checkEmail(request);
        checkNickName(request);
        if (StringUtils.isEmpty(request.getPassword())) {
            request.setPassword(null);
        }
        SysUser user = BeanUtils.copyBean(new SysUser(), request);
        long now = System.currentTimeMillis();
        user.setUpdateTime(now);
        deleteUserRoles(user.getUserId());//先删除用户角色关联
        saveUserRoles(user.getUserId(), request.getRoleIds());//再插入角色关联
        if (ObjectUtils.isEmpty(user.getDeptId())) user.setDeptId(0L);
        authUserService.clearCache(user.getUserId());
        int result = sysUserMapper.updateByPrimaryKeySelective(user);

        SysUserAssist sysUserAssist = request.getSysUserAssist();
        if (ObjectUtils.isNotEmpty(sysUserAssist) && (StringUtils.isNotBlank(sysUserAssist.getWecomId()) || StringUtils.isNotBlank(sysUserAssist.getDingtalkId()) || StringUtils.isNotBlank(sysUserAssist.getLarkId()))) {
            saveAssist(user.getUserId(), sysUserAssist.getWecomId(), sysUserAssist.getDingtalkId(), sysUserAssist.getLarkId(), sysUserAssist.getLarksuiteId());
        }
        return result;
    }

    /**
     * 用户修改个人信息
     *
     * @param request
     * @return
     */
    @CacheEvict(value = AuthConstants.USER_CACHE_NAME, key = "'user' + #request.userId")
    @Transactional
    public int updatePersonInfo(SysUserCreateRequest request) {
        SysUser user = BeanUtils.copyBean(new SysUser(), request);
        long now = System.currentTimeMillis();
        user.setUpdateTime(now);
        return sysUserMapper.updateByPrimaryKeySelective(user);

    }

    /**
     * 更新用户基本信息
     * 只允许修改 email, nickname, phone
     * 防止此接口被恶意利用更改不允许更改的信息，新建SysUser对象并只设置部分值
     *
     * @param request
     * @return
     */

    @Transactional
    public int updatePersonBasicInfo(SysUserCreateRequest request) {
        checkEmail(request);
        checkNickName(request);
        SysUser user = new SysUser();
        long now = System.currentTimeMillis();
        user.setUserId(request.getUserId());
        user.setUpdateTime(now);
        user.setEmail(request.getEmail());
        user.setNickName(request.getNickName());
        user.setPhone(request.getPhone());
        authUserService.clearCache(request.getUserId());
        return sysUserMapper.updateByPrimaryKeySelective(user);
    }

    @CacheEvict(value = AuthConstants.USER_CACHE_NAME, key = "'user' + #request.userId")
    public int updateStatus(SysUserStateRequest request) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(request.getUserId());
        sysUser.setEnabled(request.getEnabled());
        return sysUserMapper.updateByPrimaryKeySelective(sysUser);
    }

    /**
     * 修改用户密码清除缓存
     *
     * @param request
     * @return
     */
    @CacheEvict(value = AuthConstants.USER_CACHE_NAME, key = "'user' + #request.userId")
    public int updatePwd(SysUserPwdRequest request) {
        request.setPassword(new String(java.util.Base64.getDecoder().decode(request.getPassword())));
        request.setNewPassword(new String(java.util.Base64.getDecoder().decode(request.getNewPassword())));
        CurrentUserDto user = AuthUtils.getUser();

        if (ObjectUtils.isEmpty(user)) {
            String msg = "I18N_USER_DONOT_EXIST";
            DEException.throwException(Translator.get(msg));
        }
        if (!StringUtils.equals(CodingUtil.md5(request.getPassword()), user.getPassword())) {
            String msg = "I18N_USER_SOURCE_PWD_ERROR";
            DEException.throwException(Translator.get(msg));
        }
        SysUser sysUser = new SysUser();
        sysUser.setUserId(user.getUserId());
        if (!request.getNewPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,30}$")) {
            String msg = "I18N_USER_PWD_FORMAT_ERROR";
            DEException.throwException(Translator.get(msg));
        }
        sysUser.setPassword(CodingUtil.md5(request.getNewPassword()));
        return sysUserMapper.updateByPrimaryKeySelective(sysUser);
    }

    @CacheEvict(value = AuthConstants.USER_CACHE_NAME, key = "'user' + #request.userId")
    public int adminUpdatePwd(SysUserPwdRequest request) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(request.getUserId());
        sysUser.setPassword(CodingUtil.md5(new String(java.util.Base64.getDecoder().decode(request.getNewPassword()))));
        return sysUserMapper.updateByPrimaryKeySelective(sysUser);
    }


    /**
     * 删除用户角色关联
     *
     * @param userId
     * @return
     */
    private int deleteUserRoles(Long userId) {
        SysUsersRolesExample example = new SysUsersRolesExample();
        example.createCriteria().andUserIdEqualTo(userId);
        return sysUsersRolesMapper.deleteByExample(example);
    }

    /**
     * 保存用户角色关联
     *
     * @param userId
     * @param roleIds
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        roleIds.forEach(roleId -> {
            SysUsersRolesKey sysUsersRolesKey = new SysUsersRolesKey();
            sysUsersRolesKey.setUserId(userId);
            sysUsersRolesKey.setRoleId(roleId);
            sysUsersRolesMapper.insert(sysUsersRolesKey);
        });
    }

    @CacheEvict(value = AuthConstants.USER_CACHE_NAME, key = "'user' + #userId")
    @Transactional
    public int delete(Long userId) {
        extAuthService.clearUserResource(userId);
        deleteUserRoles(userId);
        sysUserAssistMapper.deleteByPrimaryKey(userId);
        return sysUserMapper.deleteByPrimaryKey(userId);
    }

    public SysUser findOne(SysUser user) {
        if (ObjectUtils.isEmpty(user)) return null;
        if (ObjectUtils.isNotEmpty(user.getUserId())) {
            return sysUserMapper.selectByPrimaryKey(user.getUserId());
        }
        SysUserExample example = new SysUserExample();
        SysUserExample.Criteria criteria = example.createCriteria();
        if (ObjectUtils.isNotEmpty(user.getUsername())) {
            criteria.andUsernameEqualTo(user.getUsername());
            List<SysUser> sysUsers = sysUserMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(sysUsers)) return sysUsers.get(0);
        }
        return null;
    }

    public void validateCasUser(String userName) {
        SysUserExample example = new SysUserExample();
        example.createCriteria().andUsernameEqualTo(userName);
        List<SysUser> users = sysUserMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(users)) {
            throw new RuntimeException("用户ID【" + userName + "】已存在,请联系管理员");
        }
    }

    public void validateExistUser(String userName, String nickName, String email) {
        SysUserExample example = new SysUserExample();
        if (StringUtils.isNotBlank(userName)) {
            example.createCriteria().andUsernameEqualTo(userName);
            List<SysUser> users = sysUserMapper.selectByExample(example);
            example.clear();
            if (CollectionUtils.isNotEmpty(users)) {
                throw new RuntimeException("用户ID【" + userName + "】已存在,请联系管理员");
            }
        }

        if (StringUtils.isNotBlank(nickName)) {
            example.createCriteria().andNickNameEqualTo(nickName);
            List<SysUser> users = sysUserMapper.selectByExample(example);
            example.clear();
            if (CollectionUtils.isNotEmpty(users)) {
                throw new RuntimeException("用户姓名【" + nickName + "】已存在,请联系管理员");
            }
        }

        if (StringUtils.isNotBlank(email)) {
            example.createCriteria().andEmailEqualTo(email);
            List<SysUser> users = sysUserMapper.selectByExample(example);
            example.clear();
            if (CollectionUtils.isNotEmpty(users)) {
                throw new RuntimeException("用户邮箱【" + email + "】已存在,请联系管理员");
            }
        }
    }


    public List<SysUser> users(List<Long> userIds) {
        return userIds.stream().map(sysUserMapper::selectByPrimaryKey).collect(Collectors.toList());
    }

    @CacheEvict(value = AuthConstants.USER_CACHE_NAME, key = "'user' + #userId")
    public void setLanguage(Long userId, String language) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLanguage(language);
        sysUserMapper.updateByPrimaryKeySelective(sysUser);
    }

    private void checkUsername(SysUserCreateRequest request) {
        SysUserExample sysUserExample = new SysUserExample();
        SysUserExample.Criteria criteria = sysUserExample.createCriteria();
        if (request.getUserId() != null) {
            criteria.andUserIdNotEqualTo(request.getUserId());
        }
        criteria.andUsernameEqualTo(request.getUsername());
        List<SysUser> sysUsers = sysUserMapper.selectByExample(sysUserExample);
        if (CollectionUtils.isNotEmpty(sysUsers)) {
            throw new RuntimeException(Translator.get("i18n_username_exists"));
        }
    }

    private void checkEmail(SysUserCreateRequest request) {
        SysUserExample sysUserExample = new SysUserExample();
        SysUserExample.Criteria criteria = sysUserExample.createCriteria();
        if (request.getUserId() != null) {
            criteria.andUserIdNotEqualTo(request.getUserId());
        }
        criteria.andEmailEqualTo(request.getEmail());
        List<SysUser> sysUsers = sysUserMapper.selectByExample(sysUserExample);
        if (CollectionUtils.isNotEmpty(sysUsers)) {
            throw new RuntimeException(Translator.get("i18n_email_exists"));
        }
    }

    private void checkNickName(SysUserCreateRequest request) {
        SysUserExample sysUserExample = new SysUserExample();
        SysUserExample.Criteria criteria = sysUserExample.createCriteria();
        if (request.getUserId() != null) {
            criteria.andUserIdNotEqualTo(request.getUserId());
        }
        criteria.andNickNameEqualTo(request.getNickName());
        List<SysUser> sysUsers = sysUserMapper.selectByExample(sysUserExample);
        if (CollectionUtils.isNotEmpty(sysUsers)) {
            throw new RuntimeException(Translator.get("i18n_nickname_exists"));
        }
    }

    public boolean needPwdNoti(Long userId) {
        SysUserAssist userAssist = sysUserAssistMapper.selectByPrimaryKey(userId);
        return ObjectUtils.isEmpty(userAssist) || ObjectUtils.isEmpty(userAssist.getNeedFirstNoti()) || userAssist.getNeedFirstNoti();
    }

    public void saveUserAssist(Boolean noti) {
        Long userId = AuthUtils.getUser().getUserId();
        SysUserAssist existAssist = sysUserAssistMapper.selectByPrimaryKey(userId);
        if (ObjectUtils.isNotEmpty(existAssist)) {
            existAssist.setNeedFirstNoti(noti);
            sysUserAssistMapper.updateByPrimaryKey(existAssist);
            return;
        }
        SysUserAssist sysUserAssist = new SysUserAssist();
        sysUserAssist.setUserId(userId);
        sysUserAssist.setNeedFirstNoti(noti);
        sysUserAssistMapper.insertSelective(sysUserAssist);
    }

    public void saveAssist(Long userId, String wecomId, String dingtlkId, String larkId, String larksuiteId) {
        SysUserAssist existAssist = sysUserAssistMapper.selectByPrimaryKey(userId);
        if (ObjectUtils.isNotEmpty(existAssist)) {
            existAssist.setWecomId(wecomId);
            existAssist.setDingtalkId(dingtlkId);
            existAssist.setLarkId(larkId);
            existAssist.setLarksuiteId(larksuiteId);
            sysUserAssistMapper.updateByPrimaryKey(existAssist);
            return;
        }
        SysUserAssist sysUserAssist = new SysUserAssist();
        sysUserAssist.setUserId(userId);
        sysUserAssist.setWecomId(wecomId);
        sysUserAssist.setDingtalkId(dingtlkId);
        sysUserAssist.setLarkId(larkId);
        sysUserAssist.setLarksuiteId(larksuiteId);
        sysUserAssistMapper.insert(sysUserAssist);
    }

    public SysUserAssist assistInfo(Long userId) {
        return sysUserAssistMapper.selectByPrimaryKey(userId);
    }

    public void changeUserFrom(Long userId, Integer from) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setFrom(from);
        sysUserMapper.updateByPrimaryKeySelective(sysUser);
    }

}
