package io.dataease.xpack.permissions.user.server;

import io.dataease.api.permissions.role.api.RoleApi;
import io.dataease.api.permissions.role.vo.RoleCreator;
import io.dataease.api.permissions.role.vo.RoleEditor;
import io.dataease.api.permissions.role.vo.RoleVO;
import io.dataease.model.KeywordRequest;
import io.dataease.utils.AuthUtils;
import io.dataease.xpack.permissions.user.manage.RoleManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleServer implements RoleApi {

    @Resource
    private RoleManage roleManage;

    @Override
    public List<RoleVO> query(KeywordRequest request) {
        return roleManage.query(request.getKeyword(), AuthUtils.getUser().getDefaultOid());
    }

    @Override
    public void create(RoleCreator creator) {
        roleManage.create(creator);
    }

    @Override
    public void edit(RoleEditor editor) {
        roleManage.edit(editor);
    }

    @Override
    public void mountUser(List<Long> uIds) {
        // 先验证uIds是否属于当前组织

    }

    public void mountExternalUser(Long uId) {

    }

    @Override
    public void unMountUser(Long uId) {

    }

    @Override
    public List<RoleVO> optionForUser(Long id) {
        return null;
    }

    @Override
    public List<RoleVO> selectedForUser(Long id) {
        return null;
    }
}
