package io.dataease.service;

import io.dataease.base.domain.*;
import io.dataease.base.mapper.ProjectMapper;
import io.dataease.base.mapper.UserMapper;
import io.dataease.base.mapper.UserRoleMapper;
import io.dataease.base.mapper.WorkspaceMapper;
import io.dataease.base.mapper.ext.ExtOrganizationMapper;
import io.dataease.base.mapper.ext.ExtUserRoleMapper;
import io.dataease.base.mapper.ext.ExtWorkspaceMapper;
import io.dataease.commons.constants.RoleConstants;
import io.dataease.commons.exception.MSException;
import io.dataease.commons.user.SessionUser;
import io.dataease.commons.utils.SessionUtils;
import io.dataease.controller.request.WorkspaceRequest;
import io.dataease.dto.UserDTO;
import io.dataease.dto.UserRoleHelpDTO;
import io.dataease.dto.WorkspaceDTO;
import io.dataease.dto.WorkspaceMemberDTO;
import io.dataease.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class WorkspaceService {
    @Resource
    private WorkspaceMapper workspaceMapper;
    @Resource
    private ExtWorkspaceMapper extWorkspaceMapper;
    @Resource
    private ExtUserRoleMapper extUserRoleMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ExtOrganizationMapper extOrganizationMapper;
    @Resource
    private ProjectMapper projectMapper;
    @Resource
    private UserService userService;

    public Workspace saveWorkspace(Workspace workspace) {
        if (StringUtils.isBlank(workspace.getName())) {
            MSException.throwException(Translator.get("workspace_name_is_null"));
        }
        // set organization id
        String currentOrgId = SessionUtils.getCurrentOrganizationId();
        workspace.setOrganizationId(currentOrgId);

        long currentTime = System.currentTimeMillis();

        checkWorkspace(workspace);

        if (StringUtils.isBlank(workspace.getId())) {
            workspace.setId(UUID.randomUUID().toString());
            workspace.setCreateTime(currentTime);
            workspace.setUpdateTime(currentTime);
            workspaceMapper.insertSelective(workspace);
        } else {
            workspace.setUpdateTime(currentTime);
            workspaceMapper.updateByPrimaryKeySelective(workspace);
        }
        return workspace;
    }

    public List<Workspace> getWorkspaceList(WorkspaceRequest request) {
        WorkspaceExample example = new WorkspaceExample();
        WorkspaceExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(request.getOrganizationId())) {
            criteria.andOrganizationIdEqualTo(request.getOrganizationId());
        }
        if (StringUtils.isNotBlank(request.getName())) {
            criteria.andNameLike(StringUtils.wrapIfMissing(request.getName(), "%"));
        }
        example.setOrderByClause("update_time desc");
        return workspaceMapper.selectByExample(example);
    }

    public List<WorkspaceDTO> getAllWorkspaceList(WorkspaceRequest request) {
        if (StringUtils.isNotBlank(request.getName())) {
            request.setName(StringUtils.wrapIfMissing(request.getName(), "%"));
        }
        return extWorkspaceMapper.getWorkspaceWithOrg(request);
    }

    public void deleteWorkspace(String workspaceId) {
        // delete project
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andWorkspaceIdEqualTo(workspaceId);
        List<Project> projectList = projectMapper.selectByExample(projectExample);
        List<String> projectIdList = projectList.stream().map(Project::getId).collect(Collectors.toList());
        projectIdList.forEach(projectId -> {
//            projectService.deleteProject(projectId);
        });

        // delete workspace member
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andSourceIdEqualTo(workspaceId);
        userRoleMapper.deleteByExample(userRoleExample);

        // delete workspace
        workspaceMapper.deleteByPrimaryKey(workspaceId);
    }

    /**
     * ORG_ADMIN需要检查是否有操作此工作空间的权限
     */
    public void checkWorkspaceOwnerByOrgAdmin(String workspaceId) {
        checkWorkspaceIsExist(workspaceId);
        WorkspaceExample example = new WorkspaceExample();
        SessionUser sessionUser = SessionUtils.getUser();
        UserDTO user = userService.getUserDTO(sessionUser.getId());
        List<String> orgIds = user.getUserRoles().stream()
                .filter(ur -> RoleConstants.ORG_ADMIN.equals(ur.getRoleId()))
                .map(UserRole::getSourceId)
                .collect(Collectors.toList());
        example.createCriteria()
                .andOrganizationIdIn(orgIds)
                .andIdEqualTo(workspaceId);
        if (workspaceMapper.countByExample(example) == 0) {
            MSException.throwException(Translator.get("workspace_does_not_belong_to_user"));
        }
    }

    public void checkWorkspaceOwner(String workspaceId) {
        checkWorkspaceIsExist(workspaceId);
        int size = 0;
        WorkspaceExample example = new WorkspaceExample();
        SessionUser sessionUser = SessionUtils.getUser();
        UserDTO user = userService.getUserDTO(sessionUser.getId());
        List<String> orgIds = user.getUserRoles().stream()
                .filter(ur -> RoleConstants.ORG_ADMIN.equals(ur.getRoleId()))
                .map(UserRole::getSourceId)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(orgIds)) {
            example.createCriteria()
                    .andOrganizationIdIn(orgIds)
                    .andIdEqualTo(workspaceId);
            size = (int) workspaceMapper.countByExample(example);
        }
        List<String> wsIds = user.getUserRoles().stream()
                .filter(ur -> RoleConstants.TEST_MANAGER.equals(ur.getRoleId()))
                .map(UserRole::getSourceId)
                .collect(Collectors.toList());
        boolean contains = wsIds.contains(workspaceId);
        if (size == 0 && !contains) {
            MSException.throwException(Translator.get("workspace_does_not_belong_to_user"));
        }
    }

    public void checkWorkspaceIsExist(String workspaceId) {
        WorkspaceExample example = new WorkspaceExample();
        example.createCriteria().andIdEqualTo(workspaceId);
        if (workspaceMapper.countByExample(example) == 0) {
            MSException.throwException(Translator.get("workspace_not_exists"));
        }
    }

    public List<Workspace> getWorkspaceListByUserId(String userId) {
        List<UserRoleHelpDTO> userRoleHelpList = extUserRoleMapper.getUserRoleHelpList(userId);
        List<String> workspaceIds = new ArrayList<>();
        userRoleHelpList.forEach(r -> {
            if (!StringUtils.isEmpty(r.getParentId())) {
                workspaceIds.add(r.getSourceId());
            }
        });
        WorkspaceExample workspaceExample = new WorkspaceExample();
        workspaceExample.createCriteria().andIdIn(workspaceIds);
        return workspaceMapper.selectByExample(workspaceExample);
    }

    public List<Workspace> getWorkspaceListByOrgIdAndUserId(String orgId) {
        String useId = SessionUtils.getUser().getId();
        WorkspaceExample workspaceExample = new WorkspaceExample();
        workspaceExample.createCriteria().andOrganizationIdEqualTo(orgId);
        List<Workspace> workspaces = workspaceMapper.selectByExample(workspaceExample);
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andUserIdEqualTo(useId);
        List<UserRole> userRoles = userRoleMapper.selectByExample(userRoleExample);
        List<Workspace> resultWorkspaceList = new ArrayList<>();
        userRoles.forEach(userRole -> {
            workspaces.forEach(workspace -> {
                if (StringUtils.equals(userRole.getSourceId(), workspace.getId())) {
                    if (!resultWorkspaceList.contains(workspace)) {
                        resultWorkspaceList.add(workspace);
                    }
                }
            });
        });
        return resultWorkspaceList;
    }

    public List<String> getWorkspaceIdsOrgId(String orgId) {
       return extWorkspaceMapper.getWorkspaceIdsByOrgId(orgId);
    }

    public void updateWorkspaceMember(WorkspaceMemberDTO memberDTO) {
        String workspaceId = memberDTO.getWorkspaceId();
        String userId = memberDTO.getId();
        // 已有角色
        List<Role> memberRoles = extUserRoleMapper.getWorkspaceMemberRoles(workspaceId, userId);
        // 修改后的角色
        List<String> roles = memberDTO.getRoleIds();
        List<String> allRoleIds = memberRoles.stream().map(Role::getId).collect(Collectors.toList());
        // 更新用户时添加了角色
        for (int i = 0; i < roles.size(); i++) {
            if (checkSourceRole(workspaceId, userId, roles.get(i)) == 0) {
                UserRole userRole = new UserRole();
                userRole.setId(UUID.randomUUID().toString());
                userRole.setUserId(userId);
                userRole.setRoleId(roles.get(i));
                userRole.setSourceId(workspaceId);
                userRole.setCreateTime(System.currentTimeMillis());
                userRole.setUpdateTime(System.currentTimeMillis());
                userRoleMapper.insertSelective(userRole);
            }
        }
        allRoleIds.removeAll(roles);
        if (allRoleIds.size() > 0) {
            UserRoleExample userRoleExample = new UserRoleExample();
            userRoleExample.createCriteria().andUserIdEqualTo(userId)
                    .andSourceIdEqualTo(workspaceId)
                    .andRoleIdIn(allRoleIds);
            userRoleMapper.deleteByExample(userRoleExample);
        }
    }

    public Integer checkSourceRole(String workspaceId, String userId, String roleId) {
        return extOrganizationMapper.checkSourceRole(workspaceId, userId, roleId);
    }

    public void updateWorkspaceByAdmin(Workspace workspace) {
        checkWorkspace(workspace);
        workspace.setCreateTime(null);
        workspace.setUpdateTime(System.currentTimeMillis());
        workspaceMapper.updateByPrimaryKeySelective(workspace);
    }

    public Workspace addWorkspaceByAdmin(Workspace workspace) {
        checkWorkspace(workspace);
        workspace.setId(UUID.randomUUID().toString());
        workspace.setCreateTime(System.currentTimeMillis());
        workspace.setUpdateTime(System.currentTimeMillis());
        workspaceMapper.insertSelective(workspace);
        return workspace;
    }

    private void checkWorkspace(Workspace workspace) {
        if (StringUtils.isBlank(workspace.getName())) {
            MSException.throwException(Translator.get("workspace_name_is_null"));
        }
        if (StringUtils.isBlank(workspace.getOrganizationId())) {
            MSException.throwException(Translator.get("organization_id_is_null"));
        }

        WorkspaceExample example = new WorkspaceExample();
        WorkspaceExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(workspace.getName())
                .andOrganizationIdEqualTo(workspace.getOrganizationId());
        if (StringUtils.isNotBlank(workspace.getId())) {
            criteria.andIdNotEqualTo(workspace.getId());
        }

        if (workspaceMapper.countByExample(example) > 0) {
            MSException.throwException(Translator.get("workspace_name_already_exists"));
        }

    }

    public List<Project> getProjects(String workspaceId) {
        ProjectExample projectExample = new ProjectExample();
        projectExample.createCriteria().andWorkspaceIdEqualTo(workspaceId);
        return projectMapper.selectByExample(projectExample);
    }
}
