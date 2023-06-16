package io.dataease.xpack.permissions.auth.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.utils.AuthUtils;
import io.dataease.xpack.permissions.auth.dao.ext.entity.BusiResourcePO;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.BusiAuthExtMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BusiAuthManage {

    @Resource
    private BusiAuthExtMapper busiAuthExtMapper;

    public List<BusiResourcePO> resourceWithOid(BusiResourceEnum busiResourceEnum) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("org_id", AuthUtils.getUser().getDefaultOid());
        queryWrapper.eq("rt_id", busiResourceEnum.getFlag());
        List<BusiResourcePO> resourcePOS = busiAuthExtMapper.query(queryWrapper);
        return resourcePOS;
    }
}
