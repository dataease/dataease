package io.dataease.service.sys;

import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.base.domain.SysUser;
import io.dataease.base.domain.SysUserExample;
import io.dataease.base.domain.SysUsersRolesExample;
import io.dataease.base.domain.SysUsersRolesKey;
import io.dataease.base.mapper.SysUserMapper;
import io.dataease.base.mapper.SysUsersRolesMapper;
import io.dataease.base.mapper.ext.ExtSysUserMapper;
import io.dataease.base.mapper.ext.query.GridExample;
import io.dataease.commons.constants.AuthConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.CodingUtil;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.request.SysUserCreateRequest;
import io.dataease.controller.sys.request.SysUserPwdRequest;
import io.dataease.controller.sys.request.SysUserStateRequest;
import io.dataease.controller.sys.response.SysUserGridResponse;
import io.dataease.controller.sys.response.SysUserRole;
import io.dataease.i18n.Translator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserService {

    //private final static String DEFAULT_PWD = "DataEase123..";

    @Value("${dataease.init_password:DataEase123..}")
    private String DEFAULT_PWD;

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysUsersRolesMapper sysUsersRolesMapper;

    @Resource
    private ExtSysUserMapper extSysUserMapper;


    public List<SysUserGridResponse> query(BaseGridRequest request) {
       /* List<SysUser> sysUsers = sysUserMapper.selectByExample(new SysUserExample());
        List<SysUserGridResponse> lists = sysUsers.stream().map(ele -> {
            SysUserGridResponse response = new SysUserGridResponse();
            BeanUtils.copyBean(response, ele);
            return response;
        }).collect(Collectors.toList());*/
        GridExample gridExample = request.convertExample();
        List<SysUserGridResponse> lists = extSysUserMapper.query(gridExample);
        lists.forEach(item -> {
            /*for (SysUserGridResponse response : query) {
                if (item.getUserId().equals(response.getUserId())) {
                    item.setId(response.getId());
                    List<SysUserRole> roles = response.getRoles();
                    item.setRoles(roles);
                    List<Long> roleIds = roles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
                    item.setRoleIds(roleIds);
                    item.setDept(response.getDept());
                }
            }*/
            List<SysUserRole> roles = item.getRoles();
            List<Long> roleIds = roles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
            item.setRoleIds(roleIds);
        });
        return lists;
    }

    @Transactional
    public int save(SysUserCreateRequest request) {
        checkUsername(request);
        SysUser user = BeanUtils.copyBean(new SysUser(), request);
        long now = System.currentTimeMillis();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setIsAdmin(false);
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
        saveUserRoles(dbUser.getUserId(), request.getRoleIds());//插入用户角色关联
        return insert;
    }

    /**
     * 修改用户密码清楚缓存
     *
     * @param request
     * @return
     */
    @CacheEvict(value = AuthConstants.USER_CACHE_NAME, key = "'user' + #request.userId")
    @Transactional
    public int update(SysUserCreateRequest request) {
        checkUsername(request);
        if (StringUtils.isEmpty(request.getPassword())) {
            request.setPassword(null);
        }
        SysUser user = BeanUtils.copyBean(new SysUser(), request);
        long now = System.currentTimeMillis();
        user.setUpdateTime(now);
        deleteUserRoles(user.getUserId());//先删除用户角色关联
        saveUserRoles(user.getUserId(), request.getRoleIds());//再插入角色关联
        if (ObjectUtils.isEmpty(user.getDeptId())) user.setDeptId(0L);
        return sysUserMapper.updateByPrimaryKeySelective(user);
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


    @CacheEvict(value = AuthConstants.USER_CACHE_NAME, key = "'user' + #request.userId")
    public int updateStatus(SysUserStateRequest request) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(request.getUserId());
        sysUser.setEnabled(request.getEnabled());
        return sysUserMapper.updateByPrimaryKeySelective(sysUser);
    }

    /**
     * 修改用户密码清楚缓存
     *
     * @param request
     * @return
     */
    @CacheEvict(value = AuthConstants.USER_CACHE_NAME, key = "'user' + #request.userId")
    public int updatePwd(SysUserPwdRequest request) {
        CurrentUserDto user = AuthUtils.getUser();

        if (ObjectUtils.isEmpty(user)) {
            throw new RuntimeException("用户不存在");
        }
        if (!StringUtils.equals(CodingUtil.md5(request.getPassword()), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        SysUser sysUser = new SysUser();
        sysUser.setUserId(user.getUserId());
        sysUser.setPassword(CodingUtil.md5(request.getNewPassword()));
        return sysUserMapper.updateByPrimaryKeySelective(sysUser);
    }

    @CacheEvict(value = AuthConstants.USER_CACHE_NAME, key = "'user' + #request.userId")
    public int adminUpdatePwd(SysUserPwdRequest request) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(request.getUserId());
        sysUser.setPassword(CodingUtil.md5(request.getNewPassword()));
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
        deleteUserRoles(userId);
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
}
