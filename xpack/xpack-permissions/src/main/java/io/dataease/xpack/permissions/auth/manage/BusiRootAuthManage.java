package io.dataease.xpack.permissions.auth.manage;


import cn.hutool.core.collection.CollectionUtil;
import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.BusiAuthExtMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
public class BusiRootAuthManage {

    @Resource
    private BusiAuthExtMapper busiAuthExtMapper;

    public int userRootPer(int rtid, Long oid, Long uid) {
        Integer weight = busiAuthExtMapper.userRootWeight(uid, rtid, oid);
        if (ObjectUtils.isEmpty(weight)) return 0;
        return weight;
    }

    public int roleRootPer(int rtid, List<Long> rids) {
        List<PermissionItem> permissionItems = busiAuthExtMapper.roleRootWeight(rids, rtid);
        if (CollectionUtil.isEmpty(permissionItems)) return 0;
        return permissionItems.stream().sorted().findFirst().get().getWeight();
    }
}
