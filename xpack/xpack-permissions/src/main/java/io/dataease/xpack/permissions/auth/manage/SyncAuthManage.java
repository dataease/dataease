package io.dataease.xpack.permissions.auth.manage;

import cn.hutool.core.collection.CollectionUtil;
import io.dataease.api.permissions.auth.dto.BusiResourceCreator;
import io.dataease.api.permissions.auth.dto.BusiResourceEditor;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthBusiRole;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthBusiUser;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerBusiResource;
import io.dataease.xpack.permissions.auth.dao.auto.mapper.PerBusiResourceMapper;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.BusiAuthExtMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class SyncAuthManage {

    @Resource
    private PerBusiResourceMapper perBusiResourceMapper;

    @Resource
    private BusiAuthExtMapper busiAuthExtMapper;

    @Resource
    private UserAuthManage userAuthManage;

    @Resource
    private RoleAuthManage roleAuthManage;

    /**
     * 从业务系统同步资源到权限系统
     *
     * @param creator
     */
    @Transactional
    public void syncResource(BusiResourceCreator creator) {
        Long pid = creator.getPid();
        Long oid = AuthUtils.getUser().getDefaultOid();

        PerBusiResource perBusiResource = new PerBusiResource();
        perBusiResource.setId(creator.getId());
        perBusiResource.setName(creator.getName());
        perBusiResource.setOrgId(oid);
        perBusiResource.setPid(creator.getPid());
        if (ObjectUtils.isNotEmpty(creator.getLeaf()) && creator.getLeaf()) {
            perBusiResource.setLeaf(creator.getLeaf());
        }
        BusiResourceEnum resourceEnum = BusiResourceEnum.valueOf(creator.getFlag().toUpperCase());
        perBusiResource.setRtId(resourceEnum.getFlag());
        PerBusiResource parent = null;
        if (ObjectUtils.isNotEmpty(pid) && !pid.equals(0L) && ObjectUtils.isNotEmpty(parent = perBusiResourceMapper.selectById(pid))) {
            String rootWay = StringUtils.isBlank(parent.getRootWay()) ? parent.getId().toString() : (parent.getRootWay() + "," + parent.getId());
            perBusiResource.setRootWay(rootWay);
        }
        perBusiResourceMapper.insert(perBusiResource);
        proxy().syncAuthForResource(perBusiResource.getId(), perBusiResource.getRtId(), perBusiResource.getRootWay());
    }

    // 把同步的资源授权给当前用户
    // 获取root_way查询root_way已授权的角色以及用户 并把当前同步的资源授权给这些角色和用户
    // 这是保障权限继承机制
    @Transactional
    public void syncAuthForResource(Long id, Integer rt, String rootWay) {
        List<PerAuthBusiUser> perAuthBusiUsers = userAuthManage.uidForRootWay(rootWay);
        PerAuthBusiUser authBusiUser = userAuthManage.curUserPer(id, rt);
        perAuthBusiUsers = ObjectUtils.isEmpty(perAuthBusiUsers) ? new ArrayList<>() : perAuthBusiUsers;
        perAuthBusiUsers.add(authBusiUser);
        userAuthManage.syncCascade(perAuthBusiUsers, id);

        List<PerAuthBusiRole> perAuthBusiRoles = roleAuthManage.ridForRootWay(rootWay);
        if (CollectionUtil.isNotEmpty(perAuthBusiRoles))
            roleAuthManage.syncCascade(perAuthBusiRoles, id);
    }

    private SyncAuthManage proxy() {
        return CommonBeanFactory.getBean(SyncAuthManage.class);
    }

    public void editResourceName(BusiResourceEditor editor) {
        /*QueryWrapper<PerBusiResource> queryWrapper = new QueryWrapper<>();
        BusiResourceEnum resourceEnum = BusiResourceEnum.valueOf(editor.getFlag().toUpperCase());
        queryWrapper.eq("id", editor.getId());
        queryWrapper.eq("rt_id", resourceEnum.getFlag());
        PerBusiResource perBusiResource = perBusiResourceMapper.selectOne(queryWrapper);*/
        Optional.ofNullable(perBusiResourceMapper.selectById(editor.getId())).ifPresent(resource -> {
            resource.setName(editor.getName());
            perBusiResourceMapper.updateById(resource);
        });
    }

    @Transactional
    public void delResource(Long id) {
        // 暂未做级联处理，待需求明确再做不迟
        busiAuthExtMapper.delUserPerByResource(id);
        busiAuthExtMapper.delRolePerByResource(id);
        perBusiResourceMapper.deleteById(id);
    }

}
