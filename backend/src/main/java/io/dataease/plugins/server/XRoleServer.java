package io.dataease.plugins.server;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.auth.annotation.DeLog;
import io.dataease.auth.annotation.DePermission;
import io.dataease.auth.annotation.SqlInjectValidator;
import io.dataease.auth.service.ExtAuthService;
import io.dataease.commons.constants.AuthConstants;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.DeLogUtils;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.dto.SysLogDTO;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.entity.XpackGridRequest;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.role.dto.request.RoleUserMappingRequest;
import io.dataease.plugins.xpack.role.dto.request.RoleUserRequest;
import io.dataease.plugins.xpack.role.dto.response.RoleUserItem;
import io.dataease.plugins.xpack.role.dto.response.XpackRoleDto;
import io.dataease.plugins.xpack.role.dto.response.XpackRoleItemDto;
import io.dataease.plugins.xpack.role.service.RoleXpackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import static io.dataease.commons.constants.SysLogConstants.OPERATE_TYPE;
import static io.dataease.commons.constants.SysLogConstants.SOURCE_TYPE;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
@Api(tags = "xpack：角色管理")
@RequestMapping("/plugin/role")
@RestController
public class XRoleServer {

    @Autowired
    private ExtAuthService extAuthService;

    @RequiresPermissions("role:add")
    @ApiOperation("新增角色")
    @PostMapping("/create")
    @DeLog(
        operatetype = SysLogConstants.OPERATE_TYPE.CREATE,
        sourcetype = SysLogConstants.SOURCE_TYPE.ROLE,
        value = "roleId"
    )
    public void create(@RequestBody XpackRoleDto role){
        RoleXpackService roleXpackService = SpringContextUtil.getBean(RoleXpackService.class);
        roleXpackService.save(role);
    }


    @RequiresPermissions("role:del")
    @ApiOperation("删除角色")
    @PostMapping("/delete/{roleId}")
    @DeLog(
        operatetype = SysLogConstants.OPERATE_TYPE.DELETE,
        sourcetype = SysLogConstants.SOURCE_TYPE.ROLE
    )
    public void delete(@PathVariable("roleId") Long roleId){
        RoleXpackService roleXpackService = SpringContextUtil.getBean(RoleXpackService.class);
        extAuthService.clearRoleResource(roleId);
        roleXpackService.delete(roleId);
    }


    @RequiresPermissions("role:edit")
    @ApiOperation("更新角色")
    @PostMapping("/update")
    @DeLog(
        operatetype = SysLogConstants.OPERATE_TYPE.MODIFY,
        sourcetype = SysLogConstants.SOURCE_TYPE.ROLE,
        value = "roleId"
    )
    public void update(@RequestBody XpackRoleDto role){
        RoleXpackService roleXpackService = SpringContextUtil.getBean(RoleXpackService.class);
        roleXpackService.update(role);
    }

    @RequiresPermissions("role:read")
    @ApiOperation("分页查询")
    @PostMapping("/roleGrid/{goPage}/{pageSize}")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "path", name = "goPage", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(paramType = "path", name = "pageSize", value = "页容量", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "request", value = "查询条件", required = true)
    })
    @SqlInjectValidator(value = {"create_time", "name"})
    public Pager<List<XpackRoleDto>> roleGrid(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody XpackGridRequest request) {
        RoleXpackService roleXpackService = SpringContextUtil.getBean(RoleXpackService.class);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        Pager<List<XpackRoleDto>> listPager = PageUtils.setPageInfo(page, roleXpackService.query(request));
        return listPager;
    }

    @ApiIgnore
    @PostMapping("/all")
    public List<XpackRoleItemDto> all() {
        RoleXpackService roleXpackService = SpringContextUtil.getBean(RoleXpackService.class);
        return roleXpackService.allRoles();
    }

    @RequiresPermissions({"role:read", "user:read"})
    @ApiOperation("查询角色下用户")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "path", name = "goPage", value = "页码", required = true, dataType = "Integer"),
        @ApiImplicitParam(paramType = "path", name = "pageSize", value = "页容量", required = true, dataType = "Integer"),
        @ApiImplicitParam(name = "request", value = "查询条件", required = true)
    })
    @PostMapping("/userGrid/{goPage}/{pageSize}")
    public Pager<List<RoleUserItem>> userGrid(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody RoleUserRequest request) {
        RoleXpackService roleXpackService = SpringContextUtil.getBean(RoleXpackService.class);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        List<RoleUserItem> userItems = roleXpackService.userItems(request);
        Pager<List<RoleUserItem>> setPageInfo = PageUtils.setPageInfo(page, userItems);
        return setPageInfo;
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("查询角色下用户")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "goPage", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "页容量", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "request", value = "查询条件", required = true)
    })
    @PostMapping("/userGrid/{datasetId}")
    public Pager<List<RoleUserItem>> userGrids(@PathVariable String datasetId, @RequestBody RoleUserRequest request) {
        return userGrid(0,0, request);
    }

    @RequiresPermissions({"role:edit", "user:edit"})
    @ApiOperation("绑定用户")
    @PostMapping("/bindUser")
    public void bindUser(@RequestBody RoleUserMappingRequest request) {
        RoleXpackService roleXpackService = SpringContextUtil.getBean(RoleXpackService.class);
        if (CollectionUtils.isNotEmpty(request.getUserIds())) {
            request.getUserIds().forEach(userId -> {
                SysLogDTO sysLogDTO = DeLogUtils.buildBindRoleUserLog(request.getRoleId(), userId, OPERATE_TYPE.BIND, SOURCE_TYPE.ROLE);
                DeLogUtils.save(sysLogDTO);
                CacheUtils.remove( AuthConstants.USER_CACHE_NAME, "user" + userId);
            });
        }
        roleXpackService.addUser(request);

    }

    @RequiresPermissions({"role:edit", "user:edit"})
    @ApiOperation("解绑用户")
    @PostMapping("/unBindUsers")
    public void unBindUsers(@RequestBody RoleUserMappingRequest request) {
        RoleXpackService roleXpackService = SpringContextUtil.getBean(RoleXpackService.class);
        if (CollectionUtils.isNotEmpty(request.getUserIds())) {
            request.getUserIds().forEach(userId -> {
                SysLogDTO sysLogDTO = DeLogUtils.buildBindRoleUserLog(request.getRoleId(), userId, OPERATE_TYPE.UNBIND, SOURCE_TYPE.ROLE);
                DeLogUtils.save(sysLogDTO);
                CacheUtils.remove( AuthConstants.USER_CACHE_NAME, "user" + userId);
            });
        }
        roleXpackService.batchDelUser(request);
    }
}
