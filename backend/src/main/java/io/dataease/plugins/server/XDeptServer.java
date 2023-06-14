package io.dataease.plugins.server;


import cn.hutool.core.collection.CollectionUtil;
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
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.commons.utils.DeLogUtils;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.sys.response.DeptNodeResponse;
import io.dataease.dto.SysLogDTO;
import io.dataease.listener.util.CacheUtils;
import io.dataease.plugins.common.entity.XpackGridRequest;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.dept.dto.request.*;
import io.dataease.plugins.xpack.dept.dto.response.DeptUserItemDTO;
import io.dataease.plugins.xpack.dept.dto.response.XpackDeptTreeNode;
import io.dataease.plugins.xpack.dept.dto.response.XpackSysDept;
import io.dataease.plugins.xpack.dept.service.DeptXpackService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.stream.Collectors;
@Api(tags = "xpack：部门管理")
@RequestMapping("/plugin/dept")
@RestController
public class XDeptServer {

    @Autowired
    private ExtAuthService extAuthService;

    @ApiOperation("查询子节点")
    @PostMapping("/childNodes/{pid}")
    public List<DeptNodeResponse> childNodes(@PathVariable("pid") Long pid){
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        List<XpackSysDept> nodes = deptService.nodesByPid(pid);
        List<DeptNodeResponse> nodeResponses = nodes.stream().map(node -> {
            DeptNodeResponse deptNodeResponse = BeanUtils.copyBean(new DeptNodeResponse(), node);
            deptNodeResponse.setHasChildren(node.getSubCount() > 0);
            deptNodeResponse.setLeaf(node.getSubCount() == 0);
            deptNodeResponse.setTop(node.getPid() == 0L);
            return deptNodeResponse;
        }).collect(Collectors.toList());
        return nodeResponses;
    }

    @ApiOperation("搜索组织树")
    @PostMapping("/search")
    public List<DeptNodeResponse> search(@RequestBody XpackGridRequest request){
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        List<XpackSysDept> nodes = deptService.nodesTreeByCondition(request);
        List<DeptNodeResponse> nodeResponses = nodes.stream().map(node -> {
            DeptNodeResponse deptNodeResponse = BeanUtils.copyBean(new DeptNodeResponse(), node);
            deptNodeResponse.setHasChildren(node.getSubCount() > 0);
            deptNodeResponse.setLeaf(node.getSubCount() == 0);
            deptNodeResponse.setTop(node.getPid() == 0L);
            return deptNodeResponse;
        }).collect(Collectors.toList());
        return nodeResponses;
    }

    @ApiIgnore
    @PostMapping("/root")
    public  List<XpackSysDept> rootData(){
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        List<XpackSysDept> nodes = deptService.nodesByPid(null);
        return nodes;
    }

    @RequiresPermissions("dept:add")
    @ApiOperation("创建")
    @PostMapping("/create")
    @DeLog(
        operatetype = SysLogConstants.OPERATE_TYPE.CREATE,
        sourcetype = SysLogConstants.SOURCE_TYPE.DEPT,
        positionIndex = 0,positionKey = "pid",
        value = "deptId"
    )
    public int create(@RequestBody XpackCreateDept dept){
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        return deptService.add(dept);
    }

    @RequiresPermissions("dept:del")
    @ApiOperation("删除")
    @PostMapping("/delete")
    @DeLog(
        operatetype = SysLogConstants.OPERATE_TYPE.DELETE,
        sourcetype = SysLogConstants.SOURCE_TYPE.DEPT,
        positionIndex = 0,positionKey = "pid",
        value = "deptId"
    )
    public void delete(@RequestBody List<XpackDeleteDept> requests){
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        requests.forEach(request -> {
            extAuthService.clearDeptResource(request.getDeptId());
        });
        deptService.batchDelete(requests);
    }

    @RequiresPermissions("dept:edit")
    @ApiOperation("更新")
    @PostMapping("/update")
    @DeLog(
        operatetype = SysLogConstants.OPERATE_TYPE.MODIFY,
        sourcetype = SysLogConstants.SOURCE_TYPE.DEPT,
        positionIndex = 0,positionKey = "pid",
        value = "deptId"
    )
    public int update(@RequestBody XpackCreateDept dept){
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        return deptService.update(dept);
    }



    @ApiIgnore
    @PostMapping("/nodesByDeptId/{deptId}")
    public List<XpackDeptTreeNode> nodesByDeptId(@PathVariable("deptId") Long deptId){
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        return deptService.searchTree(deptId);
    }


    @RequiresPermissions("dept:edit")
    @ApiOperation("移动")
    @PostMapping("/move")
    public void move(@RequestBody XpackMoveDept xpackMoveDept){
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        deptService.move(xpackMoveDept);
    }

    @RequiresPermissions({"dept:read", "user:read"})
    @ApiOperation("查询用户")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "goPage", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "页容量", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "request", value = "查询条件", required = true)
    })
    @PostMapping("/userGrid/{goPage}/{pageSize}")
    @SqlInjectValidator(value = {"dept_name", "nick_name"})
    public Pager<List<DeptUserItemDTO>> userGrid(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody XpackDeptUserRequest request) {
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        List<DeptUserItemDTO> userItems = deptService.queryBinded(request, true);
        Pager<List<DeptUserItemDTO>> setPageInfo = PageUtils.setPageInfo(page, userItems);
        return setPageInfo;
    }

    @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("查询用户")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "goPage", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "页容量", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "request", value = "查询条件", required = true)
    })
    @PostMapping("/userGrid/{datasetId}")
    public Pager<List<DeptUserItemDTO>> userGrids(@PathVariable String datasetId, @RequestBody XpackDeptUserRequest request) {
        return userGrid(0,0,  request);
    }

    @RequiresPermissions({"dept:edit", "user:edit"})
    @PostMapping("/bindUser")
    public void bindUser(@RequestBody XpackDeptBindRequest request) {
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        request.getUserIds().forEach(userId -> {
            SysLogDTO sysLogDTO = DeLogUtils.buildBindRoleUserLog(request.getDeptId(), userId, SysLogConstants.OPERATE_TYPE.BIND, SysLogConstants.SOURCE_TYPE.DEPT);
            DeLogUtils.save(sysLogDTO);
            CacheUtils.remove( AuthConstants.USER_CACHE_NAME, "user" + userId);
        });
        deptService.bindUser(request);
    }

    @RequiresPermissions({"dept:edit", "user:edit"})
    @PostMapping("/unBindUser")
    public void unBindUser(@RequestBody XpackDeptBindRequest request) {
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        if (CollectionUtil.isEmpty(request.getUserIds())) {
            DEException.throwException("userIds can not be empty");
        }
        request.getUserIds().forEach(userId -> {
            SysLogDTO sysLogDTO = DeLogUtils.buildBindRoleUserLog(request.getDeptId(), userId, SysLogConstants.OPERATE_TYPE.UNBIND, SysLogConstants.SOURCE_TYPE.DEPT);
            DeLogUtils.save(sysLogDTO);
            CacheUtils.remove( AuthConstants.USER_CACHE_NAME, "user" + userId);
        });
        deptService.unBindUsers(request);
    }
}
