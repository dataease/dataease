package io.dataease.service.sys;

import io.dataease.base.domain.SysUser;
import io.dataease.base.domain.SysUserExample;
import io.dataease.base.domain.SysUsersRolesExample;
import io.dataease.base.domain.SysUsersRolesKey;
import io.dataease.base.mapper.SysUserMapper;
import io.dataease.base.mapper.SysUsersRolesMapper;
import io.dataease.base.mapper.ext.ExtSysUserMapper;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.CodingUtil;
import io.dataease.controller.sys.request.SysUserCreateRequest;
import io.dataease.controller.sys.request.UserGridRequest;
import io.dataease.controller.sys.response.SysUserGridResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysUserService {

    private final static String DEFAULT_PWD = "DataEase123..";

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private SysUsersRolesMapper sysUsersRolesMapper;

    @Resource
    private ExtSysUserMapper extSysUserMapper;

    public List<SysUserGridResponse> query(UserGridRequest request){
        return extSysUserMapper.query(request);
    }

    @Transactional
    public int save(SysUserCreateRequest request){
        SysUser user = BeanUtils.copyBean(new SysUser(), request);
        long now = System.currentTimeMillis();
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setIsAdmin(false);
        user.setPassword(CodingUtil.md5(DEFAULT_PWD));
        int insert = sysUserMapper.insert(user);
        SysUser dbUser = findOne(user);
        saveUserRoles(dbUser.getUserId(), request.getRoleIds());//插入用户角色关联
        return insert;
    }

    @Transactional
    public int update(SysUserCreateRequest request){
        SysUser user = BeanUtils.copyBean(new SysUser(), request);
        long now = System.currentTimeMillis();
        user.setUpdateTime(now);
        deleteUserRoles(user.getUserId());//先删除用户角色关联
        saveUserRoles(user.getUserId(), request.getRoleIds());//再插入角色关联
        return sysUserMapper.updateByPrimaryKey(user);
    }

    /**
     * 删除用户角色关联
     * @param userId
     * @return
     */
    private int deleteUserRoles(Long userId){
        SysUsersRolesExample example = new SysUsersRolesExample();
        example.createCriteria().andUserIdEqualTo(userId);
        return sysUsersRolesMapper.deleteByExample(example);
    }

    /**
     * 保存用户角色关联
     * @param userId
     * @param roleIds
     */
    private void saveUserRoles(Long userId, List<Long> roleIds){
        roleIds.forEach(roleId -> {
            SysUsersRolesKey sysUsersRolesKey = new SysUsersRolesKey();
            sysUsersRolesKey.setUserId(userId);
            sysUsersRolesKey.setRoleId(roleId);
            sysUsersRolesMapper.insert(sysUsersRolesKey);
        });
    }

    @Transactional
    public int delete(Long userId){
        deleteUserRoles(userId);
        return sysUserMapper.deleteByPrimaryKey(userId);
    }

    public SysUser findOne(SysUser user){
        if (ObjectUtils.isEmpty(user)) return null;
        if (ObjectUtils.isNotEmpty(user.getUserId())){
            return sysUserMapper.selectByPrimaryKey(user.getUserId());
        }
        SysUserExample example = new SysUserExample();
        SysUserExample.Criteria criteria = example.createCriteria();
        if (ObjectUtils.isNotEmpty(user.getUsername())){
            criteria.andUsernameEqualTo(user.getUsername());
            List<SysUser> sysUsers = sysUserMapper.selectByExample(example);
            if (CollectionUtils.isNotEmpty(sysUsers))return sysUsers.get(0);
        }
        return null;
    }

}
