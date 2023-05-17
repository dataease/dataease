package io.dataease.xpack.permissions.auth.manage;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.dataease.constant.AuthEnum;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.IDUtils;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthBusiUser;
import io.dataease.xpack.permissions.auth.dao.auto.mapper.PerAuthBusiUserMapper;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.BusiAuthExtMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 负责用户业务资源权限
 */
@Component
public class UserAuthManage extends ServiceImpl<PerAuthBusiUserMapper, PerAuthBusiUser> {

    @Resource
    private PerAuthBusiUserMapper perAuthBusiUserMapper;

    @Resource
    private BusiAuthExtMapper busiAuthExtMapper;

    @Resource
    private AuthWeightService authWeightService;

    /**
     * 对rootWay有权限的
     *
     * @param rootWay
     * @return
     */
    public List<PerAuthBusiUser> uidForRootWay(String rootWay) {
        if (StringUtils.isBlank(rootWay)) return ListUtil.empty();
        List<String> ids = Arrays.stream(rootWay.split(",")).toList();
        if (CollectionUtil.isEmpty(ids)) return ListUtil.empty();
        List<PerAuthBusiUser> perAuthBusiUsers = perAuthBusiUserMapper.selectBatchIds(ids);
        return perAuthBusiUsers;
    }

    public PerAuthBusiUser curUserPer(Long resourceId, Integer rt) {
        Long userId = AuthUtils.getUser().getUserId();
        PerAuthBusiUser busiUser = new PerAuthBusiUser();
        busiUser.setId(IDUtils.snowID());
        busiUser.setUid(userId);
        busiUser.setResourceType(rt);
        busiUser.setResourceId(resourceId);
        busiUser.setWeight(AuthEnum.AUTH.getWeight());
        return busiUser;
    }

    public void syncCascade(List<PerAuthBusiUser> pers, Long resourceId) {
        if (CollectionUtil.isEmpty(pers)) return;
        List<PerAuthBusiUser> busiUsers = pers.stream().map(per -> {
            per.setResourceId(resourceId);
            per.setId(IDUtils.snowID());
            return per;
        }).toList();
        saveBatch(busiUsers);
    }

    @Cacheable(value = "user_busi_pers", key = "#uid.toString() + #oid.toString() + #flag.toString()")
    public List<PermissionItem> permissionItems(Long uid, Long oid, Integer flag) {
        List<PermissionItem> permissionItems = busiAuthExtMapper.userPermission(uid, flag);
        permissionItems = authWeightService.filterValid(permissionItems);
        return permissionItems;
    }
}
