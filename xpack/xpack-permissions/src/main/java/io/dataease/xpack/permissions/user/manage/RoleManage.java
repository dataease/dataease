package io.dataease.xpack.permissions.user.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.permissions.role.vo.RoleCreator;
import io.dataease.api.permissions.role.vo.RoleEditor;
import io.dataease.api.permissions.role.vo.RoleVO;
import io.dataease.exception.DEException;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerRole;
import io.dataease.xpack.permissions.user.dao.auto.mapper.PerRoleMapper;
import io.dataease.xpack.permissions.user.dao.ext.entity.RolePO;
import io.dataease.xpack.permissions.user.dao.ext.mapper.RoleExtMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleManage {

    @Resource
    private PerRoleMapper perRoleMapper;

    @Resource
    private RoleExtMapper roleExtMapper;

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
}
