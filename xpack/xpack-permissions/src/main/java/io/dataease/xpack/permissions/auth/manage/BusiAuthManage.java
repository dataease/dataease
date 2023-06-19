package io.dataease.xpack.permissions.auth.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.utils.AuthUtils;
import io.dataease.xpack.permissions.auth.dao.ext.entity.BusiResourcePO;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.BusiAuthExtMapper;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BusiAuthManage {

    @Resource
    private BusiAuthExtMapper busiAuthExtMapper;

    @Cacheable(cacheNames = "all_oid_flag_resource", key = "#oid.toString() + #resourceFlag.toString()")
    public List<BusiResourcePO> resourceWithOid(Integer resourceFlag, Long oid) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("org_id", oid);
        queryWrapper.eq("rt_id", resourceFlag);
        List<BusiResourcePO> resourcePOS = busiAuthExtMapper.query(queryWrapper);
        return resourcePOS;
    }
}
