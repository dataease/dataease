package io.dataease.xpack.permissions.auth.manage;

import io.dataease.xpack.permissions.auth.dao.ext.mapper.BusiAuthExtMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BusiAuthManage {

    @Resource
    private BusiAuthExtMapper busiAuthExtMapper;

    public List<Long> resourceIdsByRt(Integer flag, Long oid) {
        return busiAuthExtMapper.resourceIdsByRt(flag, oid);
    }
}
