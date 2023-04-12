package io.dataease.xpack.permissions.user.manage;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.dataease.xpack.permissions.user.dao.auto.entity.PerUserRole;
import io.dataease.xpack.permissions.user.dao.auto.mapper.PerUserRoleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleBatchManage extends ServiceImpl<PerUserRoleMapper, PerUserRole> {

    @Resource
    private PerUserRoleMapper perUserRoleMapper;

    public boolean save(List<PerUserRole> list) {
        return saveBatch(list);
    }
}
