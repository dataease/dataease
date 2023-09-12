package io.dataease.plugins.xpack.role.service;

import io.dataease.plugins.common.request.KeywordRequest;
import io.dataease.plugins.common.service.PluginMenuService;
import io.dataease.plugins.xpack.role.dto.request.RoleUserMappingRequest;
import io.dataease.plugins.xpack.role.dto.request.RoleUserRequest;
import io.dataease.plugins.xpack.role.dto.response.RoleUserItem;
import io.dataease.plugins.xpack.role.dto.response.XpackRoleDto;
import io.dataease.plugins.xpack.role.dto.response.XpackRoleItemDto;

import java.util.List;


public abstract class RoleXpackService extends PluginMenuService {


    public abstract void save(XpackRoleDto dto);

    public abstract void delete(Long roleId);

    public abstract void update(XpackRoleDto dto);

    public abstract List<XpackRoleDto> query(KeywordRequest request);

    public abstract List<XpackRoleItemDto> allRoles();

    public abstract List<RoleUserItem> userItems(RoleUserRequest request);

    public abstract List<Long> addUser(RoleUserMappingRequest request);

    public abstract void batchDelUser(RoleUserMappingRequest request);

}
