package io.dataease.xpack.permissions.auth.manage;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.dataease.api.permissions.auth.vo.PermissionItem;
import io.dataease.xpack.permissions.auth.dao.auto.entity.PerAuthMenu;
import io.dataease.xpack.permissions.auth.dao.auto.mapper.PerAuthMenuMapper;
import io.dataease.xpack.permissions.auth.dao.ext.mapper.MenuAuthExtMapper;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MenuAuthManage extends ServiceImpl<PerAuthMenuMapper, PerAuthMenu> {

    @Resource
    private MenuAuthExtMapper menuAuthExtMapper;

    @Resource
    private AuthWeightService authWeightService;

    @Cacheable(value = "role_menu_pers", key = "#rid.toString()")
    public List<PermissionItem> permissionItems(Long rid) {
        List<PermissionItem> permissionItems = menuAuthExtMapper.rolePermission(rid);
        return authWeightService.filterValid(permissionItems);
    }

}
