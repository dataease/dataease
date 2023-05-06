package io.dataease.xpack.permissions.user.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.permissions.role.dto.RoleCopyRequest;
import io.dataease.api.permissions.role.dto.UnmountUserRequest;
import io.dataease.api.permissions.role.dto.RoleCreator;
import io.dataease.api.permissions.role.vo.RoleDetailVO;
import io.dataease.api.permissions.role.dto.RoleEditor;
import io.dataease.api.permissions.role.vo.RoleVO;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.exception.DEException;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerRole;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerUserRole;
import io.dataease.xpack.permissions.user.dao.auto.mapper.PerRoleMapper;
import io.dataease.xpack.permissions.user.dao.auto.mapper.PerUserRoleMapper;
import io.dataease.xpack.permissions.user.dao.ext.entity.RolePO;
import io.dataease.xpack.permissions.user.dao.ext.mapper.RoleExtMapper;
import io.dataease.xpack.permissions.user.entity.RoleInfo;
import io.dataease.xpack.permissions.user.entity.UserRole;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Lazy
public class RoleManage {

    private static final String ORG_ADMIN = "组织管理员";
    private static final String ORG_READONLY = "普通用户";

    @Resource
    private PerRoleMapper perRoleMapper;

    @Resource
    private RoleExtMapper roleExtMapper;

    @Resource
    private PerUserRoleMapper perUserRoleMapper;

    @Resource
    private RoleBatchManage roleBatchManage;

    @Resource
    private UserPageManage userPageManage;

    public void create(RoleCreator creator) {
        Long oid = AuthUtils.getUser().getDefaultOid();
        QueryWrapper<PerRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("org_id", oid);
        queryWrapper.eq("pid", 0L);
        queryWrapper.eq("readonly", creator.getTypeCode() != 1);
        PerRole rootRole = perRoleMapper.selectOne(queryWrapper);
        PerRole po = new PerRole();
        po.setId(IDUtils.snowID());
        po.setName(creator.getName());
        po.setDesc(creator.getDesc());
        po.setLevel(2);
        po.setReadonly(creator.getTypeCode() != 1);
        po.setOrgId(oid);
        po.setPid(rootRole.getId());
        perRoleMapper.insert(po);
        // 需要验证名称是否重复
    }

    public void copy(RoleCopyRequest request) {
        Long copyId = request.getCopyId();
        PerRole role = perRoleMapper.selectById(copyId);
        Long sourceId = role.getId();
        Long sourcePid = role.getPid();
        role.setName(request.getName());
        role.setDesc(request.getDesc());
        role.setId(IDUtils.snowID());
        if (sourcePid.equals(0L)) {
            role.setPid(sourceId);
        } else {
            role.setPid(sourcePid);
        }
        perRoleMapper.insert(role);
    }

    @Transactional
    public void saveWithOrg(Long oid) {
        PerRole po = new PerRole();
        po.setId(IDUtils.snowID());
        po.setName(ORG_ADMIN);
        po.setDesc(ORG_ADMIN);
        po.setLevel(2);
        po.setReadonly(false);
        po.setOrgId(oid);
        po.setPid(0L);
        perRoleMapper.insert(po);

        PerRole poReadonly = new PerRole();
        poReadonly.setId(IDUtils.snowID());
        poReadonly.setName(ORG_READONLY);
        poReadonly.setDesc(ORG_READONLY);
        poReadonly.setLevel(2);
        poReadonly.setReadonly(true);
        poReadonly.setOrgId(oid);
        poReadonly.setPid(0L);
        perRoleMapper.insert(poReadonly);

        mountOrgAdmin(po.getId(), oid);
    }

    private void mountOrgAdmin(Long rid, Long oid) {
        if (AuthUtils.isSysAdmin()) {
            return;
        }
        TokenUserBO user = AuthUtils.getUser();
        Long userId = user.getUserId();
        PerUserRole userRole = new PerUserRole();
        userRole.setUid(userId);
        userRole.setRid(rid);
        userRole.setId(IDUtils.snowID());
        userRole.setOid(oid);
        userRole.setCreateTime(System.currentTimeMillis());
        perUserRoleMapper.insert(userRole);
    }

    public void deleteWithOrg(Long oid) {
        roleExtMapper.deleteUserRoleWithOrg(oid);
        QueryWrapper<PerRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("org_id", oid);
        perRoleMapper.delete(queryWrapper);
        userPageManage.deleteByEmptyRole();
        userPageManage.updateDefaultOid(oid);
    }

    public void deleteRole(Long rid) {
        QueryWrapper<PerUserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("rid", rid);
        Long count = perUserRoleMapper.selectCount(queryWrapper);
        if (count > 0) {
            perUserRoleMapper.delete(queryWrapper);
        }
        perRoleMapper.deleteById(rid);
        userPageManage.deleteByEmptyRole();
    }

    public void mountUser(Long rid, List<Long> uids) {
        Long defaultOid = AuthUtils.getUser().getDefaultOid();
        List<PerUserRole> mappings = uids.stream().map(id -> {
            PerUserRole userRole = new PerUserRole();
            userRole.setRid(rid);
            userRole.setUid(id);
            userRole.setId(IDUtils.snowID());
            userRole.setOid(defaultOid);
            userRole.setCreateTime(System.currentTimeMillis());
            return userRole;
        }).toList();
        roleBatchManage.save(mappings);
    }

    public void mountWithUserSave(Long uid, List<Long> rids) {
        Long defaultOid = AuthUtils.getUser().getDefaultOid();
        roleExtMapper.deleteUserRole(defaultOid, uid);
        List<PerUserRole> mappings = rids.stream().map(rid -> {
            PerUserRole userRole = new PerUserRole();
            userRole.setRid(rid);
            userRole.setUid(uid);
            userRole.setId(IDUtils.snowID());
            userRole.setOid(defaultOid);
            userRole.setCreateTime(System.currentTimeMillis());
            return userRole;
        }).toList();
        roleBatchManage.save(mappings);
    }

    public void edit(RoleEditor editor) {
        Long id = editor.getId();
        // 验证名称是否重复
        PerRole perRole = perRoleMapper.selectById(id);
        if (ObjectUtils.isEmpty(perRole)) {
            DEException.throwException("role not exist");
        }
        perRole.setName(editor.getName());
        perRole.setDesc(editor.getDesc());
        perRoleMapper.updateById(perRole);
    }


    public List<RoleVO> query(String keyword, Long oid) {
        QueryWrapper<RolePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(keyword), "name", keyword);
        queryWrapper.eq("org_id", oid);
        List<RolePO> rolePOS = roleExtMapper.selectList(queryWrapper);
        return rolePOS.stream().map(po -> {
            RoleVO vo = BeanUtils.copyBean(new RoleVO(), po);
            vo.setRoot(po.getPid().equals(0L));
            return vo;
        }).toList();
    }

    public List<RoleVO> optionForUser(String keyword, Long oid, Long uid) {
        QueryWrapper<RolePO> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(keyword), "name", keyword);
        queryWrapper.eq("org_id", oid);
        List<RolePO> rolePOS = null;
        if (ObjectUtils.isEmpty(uid)) {
            rolePOS = roleExtMapper.selectOptionForOrg(queryWrapper);
        } else {
            rolePOS = roleExtMapper.selectOptionForUser(uid, queryWrapper);
        }
        return rolePOS.stream().map(po -> BeanUtils.copyBean(new RoleVO(), po)).toList();
    }

    public List<RoleVO> selectedForUser(String keyword, Long oid, Long uid) {
        QueryWrapper<PerUserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(keyword), "pr.name", keyword);
        queryWrapper.eq("pr.org_id", oid);
        queryWrapper.eq("pur.uid", uid);
        List<RolePO> rolePOS = roleExtMapper.selectSelectedForUser(queryWrapper);
        return rolePOS.stream().map(po -> BeanUtils.copyBean(new RoleVO(), po)).toList();
    }

    public RoleDetailVO queryDetail(Long rid) {
        PerRole perRole = perRoleMapper.selectById(rid);
        RoleDetailVO roleDetailVO = new RoleDetailVO();
        BeanUtils.copyBean(roleDetailVO, perRole);
        roleDetailVO.setTypeCode(perRole.getReadonly() ? 0 : 1);
        return roleDetailVO;
    }

    public Integer beforeUnmountInfo(UnmountUserRequest request) {
        QueryWrapper<PerUserRole> queryWrapper = new QueryWrapper<>();
        // queryWrapper.eq("rid", request.getRid());
        queryWrapper.eq("uid", request.getUid());
        List<PerUserRole> perUserRoles = perUserRoleMapper.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(perUserRoles) || perUserRoles.size() == 1) {
            // clear in system
            return 2;
        }
        Long oid = AuthUtils.getUser().getDefaultOid();
        Map<Boolean, List<PerUserRole>> listMap = perUserRoles.stream().collect(Collectors.groupingBy(item -> innerOrg(item, oid)));
        if (CollectionUtil.isEmpty(listMap.get(true))) {
            // clear in org
            return 1;
        }
        // just can unmount
        return 0;
    }

    private Boolean innerOrg(PerUserRole userRole, Long oid) {
        return oid.equals(userRole.getOid());
    }

    @Transactional
    public void unMountUser(UnmountUserRequest request) {
        QueryWrapper<PerUserRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("rid", request.getRid());
        queryWrapper.eq("uid", request.getUid());
        perUserRoleMapper.delete(queryWrapper);

        queryWrapper.clear();
        queryWrapper.eq("uid", request.getUid());
        queryWrapper.orderByAsc("create_time");
        List<PerUserRole> perUserRoles = perUserRoleMapper.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(perUserRoles)) {
            // 删除无角色用户
            userPageManage.delete(request.getUid());
        }

        Long oid = AuthUtils.getUser().getDefaultOid();
        Map<Boolean, List<PerUserRole>> listMap = perUserRoles.stream().collect(Collectors.groupingBy(item -> innerOrg(item, oid)));
        if (CollectionUtil.isEmpty(listMap.get(true))) {
            List<PerUserRole> outOrgMappings = listMap.get(false);
            if (CollectionUtil.isEmpty(outOrgMappings)) {
                DEException.throwException("system error");
            }
            Long newOid = outOrgMappings.get(0).getOid();
            userPageManage.switchOrg(request.getUid(), newOid);
        }
    }

    public RoleInfo roleInfo(Long rid) {
        PerRole role = roleExtMapper.selectRoleInfo(rid);
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setId(role.getId());
        roleInfo.setReadonly(role.getReadonly());
        roleInfo.setRoot(role.getPid().equals(0L));
        return roleInfo;
    }

    public List<UserRole> userRole(Long uid) {
        List<PerRole> perRoles = roleExtMapper.roleInfoByUid(uid, AuthUtils.getUser().getDefaultOid());
        if (CollectionUtil.isEmpty(perRoles)) return null;
        return perRoles.stream().map(role -> {
            UserRole roleInfo = new UserRole();
            roleInfo.setId(role.getId());
            roleInfo.setReadonly(role.getReadonly());
            roleInfo.setRoot(role.getPid().equals(0L));
            roleInfo.setName(role.getName());
            return roleInfo;
        }).toList();
    }

    public Long adminId(Long oid) {
        return roleExtMapper.adminRoleId(oid);
    }
    public Long readonlyId(Long oid) {
        return roleExtMapper.readonlyRoleId(oid);
    }
}
