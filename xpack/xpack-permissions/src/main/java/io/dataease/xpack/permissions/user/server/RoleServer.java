package io.dataease.xpack.permissions.user.server;

import io.dataease.api.permissions.role.api.RoleApi;
import io.dataease.api.permissions.role.dto.MountUserRequest;
import io.dataease.api.permissions.role.dto.RoleRequest;
import io.dataease.api.permissions.role.dto.UnmountUserRequest;
import io.dataease.api.permissions.role.vo.RoleCreator;
import io.dataease.api.permissions.role.vo.RoleDetailVO;
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
    public void mountUser(MountUserRequest request) {
        // 先验证uIds是否属于当前组织
        roleManage.mountUser(request.getRid(), request.getUids());
    }

    public void mountExternalUser(Long uId) {

    }

    @Override
    public void unMountUser(UnmountUserRequest request) {
        roleManage.unMountUser(request);
    }

    @Override
    public List<RoleVO> optionForUser(RoleRequest request) {
        Long defaultOid = AuthUtils.getUser().getDefaultOid();
        return roleManage.optionForUser(request.getKeyword(), defaultOid, request.getUid());
    }

    @Override
    public List<RoleVO> selectedForUser(RoleRequest request) {
        return null;
    }

    @Override
    public RoleDetailVO detail(Long rid) {
        return roleManage.queryDetail(rid);
    }

    @Override
    public void delete(Long rid) {
        roleManage.deleteRole(rid);
    }

    @Override
    public Integer beforeUnmountInfo(UnmountUserRequest request) {
        return roleManage.beforeUnmountInfo(request);
    }
}
