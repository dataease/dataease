package io.dataease.service;

import io.dataease.base.domain.*;
import io.dataease.base.mapper.OrganizationMapper;
import io.dataease.base.mapper.UserMapper;
import io.dataease.base.mapper.UserRoleMapper;
import io.dataease.base.mapper.WorkspaceMapper;
import io.dataease.base.mapper.ext.ExtOrganizationMapper;
import io.dataease.base.mapper.ext.ExtUserRoleMapper;
import io.dataease.commons.constants.RoleConstants;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.user.SessionUser;
import io.dataease.commons.utils.SessionUtils;
import io.dataease.controller.request.OrganizationRequest;
import io.dataease.dto.OrganizationMemberDTO;
import io.dataease.dto.UserDTO;
import io.dataease.dto.UserRoleHelpDTO;
import io.dataease.i18n.Translator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationService {

    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private ExtUserRoleMapper extUserRoleMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private ExtOrganizationMapper extOrganizationMapper;
    @Resource
    private WorkspaceMapper workspaceMapper;
    @Resource
    private WorkspaceService workspaceService;
    @Resource
    private UserService userService;

    public Organization addOrganization(Organization organization) {
        checkOrganization(organization);
        long currentTimeMillis = System.currentTimeMillis();
        organization.setId(UUID.randomUUID().toString());
        organization.setCreateTime(currentTimeMillis);
        organization.setUpdateTime(currentTimeMillis);
        organizationMapper.insertSelective(organization);
        return organization;
    }

    public List<Organization> getOrganizationList(OrganizationRequest request) {
        OrganizationExample example = new OrganizationExample();
        OrganizationExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(request.getName())) {
            criteria.andNameLike(StringUtils.wrapIfMissing(request.getName(), "%"));
        }
        example.setOrderByClause("update_time desc");
        return organizationMapper.selectByExample(example);
    }

    private void checkOrganization(Organization organization) {
        if (StringUtils.isBlank(organization.getName())) {
            DEException.throwException(Translator.get("organization_name_is_null"));
        }

        OrganizationExample example = new OrganizationExample();
        OrganizationExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(organization.getName());
        if (StringUtils.isNotBlank(organization.getId())) {
            criteria.andIdNotEqualTo(organization.getId());
        }

        if (organizationMapper.countByExample(example) > 0) {
            DEException.throwException(Translator.get("organization_name_already_exists"));
        }

    }

    public void deleteOrganization(String organizationId) {
        WorkspaceExample example = new WorkspaceExample();
        WorkspaceExample.Criteria criteria = example.createCriteria();
        criteria.andOrganizationIdEqualTo(organizationId);

        // delete workspace
        List<Workspace> workspaces = workspaceMapper.selectByExample(example);
        List<String> workspaceIdList = workspaces.stream().map(Workspace::getId).collect(Collectors.toList());
        for (String workspaceId : workspaceIdList) {
            workspaceService.deleteWorkspace(workspaceId);
        }

        // delete organization member
        UserRoleExample userRoleExample = new UserRoleExample();
        userRoleExample.createCriteria().andSourceIdEqualTo(organizationId);
        userRoleMapper.deleteByExample(userRoleExample);

        // delete org
        organizationMapper.deleteByPrimaryKey(organizationId);
    }

    public void updateOrganization(Organization organization) {
        checkOrganization(organization);
        organization.setCreateTime(null);
        organization.setUpdateTime(System.currentTimeMillis());
        organizationMapper.updateByPrimaryKeySelective(organization);
    }

    public List<Organization> getOrganizationListByUserId(String userId) {
        List<UserRoleHelpDTO> userRoleHelpList = extUserRoleMapper.getUserRoleHelpList(userId);
        List<String> list = new ArrayList<>();
        userRoleHelpList.forEach(r -> {
            if (StringUtils.isEmpty(r.getParentId())) {
                list.add(r.getSourceId());
            } else {
                list.add(r.getParentId());
            }
        });

        // ignore list size is 0
        list.add("no_such_id");

        OrganizationExample organizationExample = new OrganizationExample();
        organizationExample.createCriteria().andIdIn(list);
        return organizationMapper.selectByExample(organizationExample);
    }

    public void updateOrgMember(OrganizationMemberDTO memberDTO) {
        String orgId = memberDTO.getOrganizationId();
        String userId = memberDTO.getId();
        // 已有角色
        List<Role> memberRoles = extUserRoleMapper.getOrganizationMemberRoles(orgId, userId);
        // 修改后的角色
        List<String> roles = memberDTO.getRoleIds();
        List<String> allRoleIds = memberRoles.stream().map(Role::getId).collect(Collectors.toList());
        // 更新用户时添加了角色
        for (int i = 0; i < roles.size(); i++) {
            if (checkSourceRole(orgId, userId, roles.get(i)) == 0) {
                UserRole userRole = new UserRole();
                userRole.setId(UUID.randomUUID().toString());
                userRole.setUserId(userId);
                userRole.setRoleId(roles.get(i));
                userRole.setSourceId(orgId);
                userRole.setCreateTime(System.currentTimeMillis());
                userRole.setUpdateTime(System.currentTimeMillis());
                userRoleMapper.insertSelective(userRole);
            }
        }
        allRoleIds.removeAll(roles);
        if (allRoleIds.size() > 0) {
            UserRoleExample userRoleExample = new UserRoleExample();
            userRoleExample.createCriteria().andUserIdEqualTo(userId)
                    .andSourceIdEqualTo(orgId)
                    .andRoleIdIn(allRoleIds);
            userRoleMapper.deleteByExample(userRoleExample);
        }
    }

    public Integer checkSourceRole(String orgId, String userId, String roleId) {
        return extOrganizationMapper.checkSourceRole(orgId, userId, roleId);
    }

    public void checkOrgOwner(String organizationId) {
        SessionUser sessionUser = SessionUtils.getUser();
        UserDTO user = userService.getUserDTO(sessionUser.getId());
        List<String> collect = user.getUserRoles().stream()
                .filter(ur -> RoleConstants.ORG_ADMIN.equals(ur.getRoleId()) || RoleConstants.ORG_MEMBER.equals(ur.getRoleId()))
                .map(UserRole::getSourceId)
                .collect(Collectors.toList());
        if (!collect.contains(organizationId)) {
            DEException.throwException(Translator.get("organization_does_not_belong_to_user"));
        }
    }
}
