package io.dataease.xpack.permissions.auth.manage;

import io.dataease.api.permissions.auth.dto.BusiResourceCreator;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.IDUtils;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthBusiRole;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthBusiUser;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerBusiResource;
import io.dataease.xpack.permissions.auth.dao.auto.mapper.PerBusiResourceMapper;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.BusiAuthExtMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
     * @param creator
     */
    @Transactional
    public void syncResource(BusiResourceCreator creator) {
        Long pid = creator.getPid();
        Long oid = AuthUtils.getUser().getDefaultOid();

        PerBusiResource perBusiResource = new PerBusiResource();
        perBusiResource.setId(IDUtils.snowID());
        perBusiResource.setName(creator.getName());
        perBusiResource.setOrgId(oid);
        perBusiResource.setPid(creator.getPid());
        BusiResourceEnum resourceEnum = BusiResourceEnum.valueOf(creator.getFlag());
        perBusiResource.setRtId(resourceEnum.getFlag());
        if (ObjectUtils.isEmpty(pid) || pid.equals(0L)) {
            busiAuthExtMapper.queryRootWay(pid);
        }
        PerBusiResource parent = perBusiResourceMapper.selectById(pid);
        perBusiResource.setRootWay(parent.getRootWay() + "," + parent.getId());
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
        perAuthBusiUsers.add(authBusiUser);
        userAuthManage.syncCascade(perAuthBusiUsers, id);

        List<PerAuthBusiRole> perAuthBusiRoles = roleAuthManage.ridForRootWay(rootWay);
        roleAuthManage.syncCascade(perAuthBusiRoles, id);
    }

    private SyncAuthManage proxy() {
        return CommonBeanFactory.getBean(SyncAuthManage.class);
    }

}
