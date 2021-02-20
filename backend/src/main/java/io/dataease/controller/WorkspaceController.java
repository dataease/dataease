package io.dataease.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.base.domain.Workspace;
import io.dataease.commons.constants.RoleConstants;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.commons.utils.SessionUtils;
import io.dataease.controller.request.WorkspaceRequest;
import io.dataease.dto.WorkspaceDTO;
import io.dataease.dto.WorkspaceMemberDTO;
import io.dataease.service.OrganizationService;
import io.dataease.service.UserService;
import io.dataease.service.WorkspaceService;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("workspace")
@RestController
public class WorkspaceController {
    @Resource
    private WorkspaceService workspaceService;
    @Resource
    private OrganizationService organizationService;
    @Resource
    private UserService userService;

    @PostMapping("add")
    @RequiresRoles(RoleConstants.ORG_ADMIN)
    public Workspace addWorkspace(@RequestBody Workspace workspace) {
        String currentOrganizationId = SessionUtils.getCurrentOrganizationId();
        organizationService.checkOrgOwner(currentOrganizationId);
        return workspaceService.saveWorkspace(workspace);
    }

    @GetMapping("/list")
    public List<Workspace> getWorkspaceList() {
        return workspaceService.getWorkspaceList(new WorkspaceRequest());
    }

    @PostMapping("special/add")
    @RequiresRoles(RoleConstants.ADMIN)
    public Workspace addWorkspaceByAdmin(@RequestBody Workspace workspace) {
        return workspaceService.addWorkspaceByAdmin(workspace);
    }

    @PostMapping("update")
    @RequiresRoles(RoleConstants.ORG_ADMIN)
    public Workspace updateWorkspace(@RequestBody Workspace workspace) {
        workspaceService.checkWorkspaceOwnerByOrgAdmin(workspace.getId());
        return workspaceService.saveWorkspace(workspace);
    }

    @PostMapping("special/update")
    @RequiresRoles(RoleConstants.ADMIN)
    public void updateWorkspaceByAdmin(@RequestBody Workspace workspace) {
        workspaceService.updateWorkspaceByAdmin(workspace);
    }

    @GetMapping("special/delete/{workspaceId}")
    @RequiresRoles(RoleConstants.ADMIN)
    public void deleteWorkspaceByAdmin(@PathVariable String workspaceId) {
        userService.refreshSessionUser("workspace", workspaceId);
        workspaceService.deleteWorkspace(workspaceId);
    }

    @GetMapping("delete/{workspaceId}")
    @RequiresRoles(RoleConstants.ORG_ADMIN)
    public void deleteWorkspace(@PathVariable String workspaceId) {
        workspaceService.checkWorkspaceOwnerByOrgAdmin(workspaceId);
        userService.refreshSessionUser("workspace", workspaceId);
        workspaceService.deleteWorkspace(workspaceId);
    }

    @PostMapping("list/{goPage}/{pageSize}")
    @RequiresRoles(RoleConstants.ORG_ADMIN)
    public Pager<List<Workspace>> getWorkspaceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody WorkspaceRequest request) {
        request.setOrganizationId(SessionUtils.getCurrentOrganizationId());
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, workspaceService.getWorkspaceList(request));
    }

    @PostMapping("list/all/{goPage}/{pageSize}")
    @RequiresRoles(RoleConstants.ADMIN)
    public Pager<List<WorkspaceDTO>> getAllWorkspaceList(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody WorkspaceRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, workspaceService.getAllWorkspaceList(request));
    }

    @GetMapping("/list/userworkspace/{userId}")
    public List<Workspace> getWorkspaceListByUserId(@PathVariable String userId) {
        return workspaceService.getWorkspaceListByUserId(userId);
    }

    @GetMapping("/list/orgworkspace/")
    public List<Workspace> getWorkspaceListByOrgIdAndUserId() {
        String currentOrganizationId = SessionUtils.getCurrentOrganizationId();
        return workspaceService.getWorkspaceListByOrgIdAndUserId(currentOrganizationId);
    }

    @PostMapping("/member/update")
    @RequiresRoles(value = {RoleConstants.ADMIN, RoleConstants.ORG_ADMIN, RoleConstants.TEST_MANAGER}, logical = Logical.OR)
    public void updateOrgMember(@RequestBody WorkspaceMemberDTO memberDTO) {
        workspaceService.updateWorkspaceMember(memberDTO);
    }
}
