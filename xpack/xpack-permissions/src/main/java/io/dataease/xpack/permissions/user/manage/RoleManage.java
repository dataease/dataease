package io.dataease.xpack.permissions.user.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.permissions.role.vo.RoleCreator;
import io.dataease.api.permissions.role.vo.RoleDetailVO;
import io.dataease.api.permissions.role.vo.RoleEditor;
import io.dataease.api.permissions.role.vo.RoleVO;
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
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

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
        PerRole po = new PerRole();
        po.setId(IDUtils.snowID());
        po.setName(creator.getName());
        po.setDesc(creator.getDesc());
        po.setLevel(1);
        po.setReadonly(creator.getTypeCode() != 1);
        po.setOrgId(oid);
        perRoleMapper.insert(po);
        // 需要验证名称是否重复
    }

    public void saveWithOrg(Long oid) {
        PerRole po = new PerRole();
        po.setId(IDUtils.snowID());
        po.setName(ORG_ADMIN);
        po.setDesc(ORG_ADMIN);
        po.setLevel(1);
        po.setReadonly(false);
        po.setOrgId(oid);
        perRoleMapper.insert(po);

        PerRole poReadonly = new PerRole();
        poReadonly.setId(IDUtils.snowID());
        poReadonly.setName(ORG_READONLY);
        poReadonly.setDesc(ORG_READONLY);
        poReadonly.setLevel(1);
        poReadonly.setReadonly(true);
        poReadonly.setOrgId(oid);
        perRoleMapper.insert(po);

        mountOrgAdmin(po.getId());
    }

    private void mountOrgAdmin(Long rid) {
        Long userId = AuthUtils.getUser().getUserId();
        PerUserRole userRole = new PerUserRole();
        userRole.setUid(userId);
        userRole.setRid(rid);
        userRole.setId(IDUtils.snowID());
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
            userRole.setUid(IDUtils.snowID());
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
        return rolePOS.stream().map(po -> BeanUtils.copyBean(new RoleVO(), po)).toList();
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
}
