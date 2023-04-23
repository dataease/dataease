package io.dataease.xpack.permissions.auth;

import io.dataease.api.permissions.auth.api.AuthApi;
import io.dataease.api.permissions.auth.dto.*;
import io.dataease.api.permissions.auth.vo.PermissionVO;
import io.dataease.api.permissions.auth.vo.ResourceVO;
import io.dataease.i18n.I18n;
import io.dataease.xpack.permissions.auth.manage.AuthManage;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Primary;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("auth")
@Primary
public class AuthServer implements AuthApi {

    @Resource
    private AuthManage authManage;
    @Override
    public List<ResourceVO> busiResource(String flag) {
        return authManage.resourceTree(flag);
    }

    @Override
    public List<PermissionVO> busiPermission(BusiPermissionRequest request) {
        return null;
    }

    @I18n
    @Override
    public List<ResourceVO> menuResource() {
        return authManage.menuTree();
    }

    @Override
    public List<PermissionVO> menuPermission(MenuPermissionRequest request) {
        return null;
    }

    @Override
    public void saveBusiPer(BusiPerEditor editor) {

    }

    @Override
    public void saveMenuPer(MenuPerEditor editor) {

    }

    @Override
    public void saveResource(BusiResourceCreator creator) {

    }
}
