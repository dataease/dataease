package io.dataease.xpack.permissions.auth.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.dataease.utils.IDUtils;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthBusiRole;
import io.dataease.xpack.permissions.auth.dao.auto.mapper.PerAuthBusiRoleMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RoleAuthManage extends ServiceImpl<PerAuthBusiRoleMapper, PerAuthBusiRole> {

    @Resource
    private PerAuthBusiRoleMapper perAuthBusiRoleMapper;

    public List<PerAuthBusiRole> ridForRootWay(String rootWay) {
        if (StringUtils.isBlank(rootWay)) return null;
        List<String> ids = Arrays.stream(rootWay.split(",")).toList();
        if (CollectionUtil.isEmpty(ids)) return null;
        return perAuthBusiRoleMapper.selectBatchIds(ids);
    }

    public void syncCascade(List<PerAuthBusiRole> pers, Long resourceId) {
        if (CollectionUtil.isEmpty(pers)) return;
        List<PerAuthBusiRole> perAuthBusiRoles = pers.stream().map(per -> {
            per.setId(IDUtils.snowID());
            per.setResourceId(resourceId);
            return per;
        }).toList();
        saveBatch(perAuthBusiRoles);
    }
}
