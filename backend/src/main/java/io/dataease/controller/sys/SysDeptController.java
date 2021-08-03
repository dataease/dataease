package io.dataease.controller.sys;

import io.dataease.base.domain.SysDept;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.request.DeptCreateRequest;
import io.dataease.controller.sys.request.DeptDeleteRequest;
import io.dataease.controller.sys.request.DeptStatusRequest;
import io.dataease.controller.sys.response.DeptNodeResponse;
import io.dataease.controller.sys.response.DeptTreeNode;
import io.dataease.service.sys.DeptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.stream.Collectors;

@ApiIgnore
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：部门管理")
@RequestMapping("/api/dept")
public class SysDeptController extends ResultHolder {

    @Autowired
    private DeptService deptService;

    @PostMapping("/childNodes/{pid}")
    public List<DeptNodeResponse> childNodes(@PathVariable("pid") Long pid){
        List<SysDept> nodes = deptService.nodesByPid(pid);
        List<DeptNodeResponse> nodeResponses = nodes.stream().map(node -> {
            DeptNodeResponse deptNodeResponse = BeanUtils.copyBean(new DeptNodeResponse(), node);
            deptNodeResponse.setHasChildren(node.getSubCount() > 0);
            deptNodeResponse.setLeaf(node.getSubCount() == 0);
            deptNodeResponse.setTop(node.getPid() == deptService.DEPT_ROOT_PID);
            return deptNodeResponse;
        }).collect(Collectors.toList());
        return nodeResponses;
    }

    @PostMapping("/search")
    public List<DeptNodeResponse> search(@RequestBody BaseGridRequest request){
        List<SysDept> nodes = deptService.nodesTreeByCondition(request);
        //List<SysDept> nodes = deptService.nodesByPid(pid);
        List<DeptNodeResponse> nodeResponses = nodes.stream().map(node -> {
            DeptNodeResponse deptNodeResponse = BeanUtils.copyBean(new DeptNodeResponse(), node);
            deptNodeResponse.setHasChildren(node.getSubCount() > 0);
            deptNodeResponse.setLeaf(node.getSubCount() == 0);
            deptNodeResponse.setTop(node.getPid() == deptService.DEPT_ROOT_PID);
            return deptNodeResponse;
        }).collect(Collectors.toList());
        return nodeResponses;
    }

    @ApiOperation("查询部门")
    @PostMapping("/root")
    public ResultHolder rootData(){
        List<SysDept> root = deptService.nodesByPid(null);
        return success(root);
    }

    @ApiOperation("新增部门")
    @PostMapping("/create")
    public void create(@RequestBody DeptCreateRequest dept){
        deptService.add(dept);
    }

    @ApiOperation("删除部门")
    @PostMapping("/delete")
    public void delete(@RequestBody List<DeptDeleteRequest> requests){
        deptService.batchDelete(requests);
    }
    @ApiOperation("更新部门")
    @PostMapping("/update")
    public void update(@RequestBody DeptCreateRequest dept){
        deptService.update(dept);
    }
    @ApiOperation("更新状态")
    @PostMapping("/updateStatus")
    public void updateStatus(@RequestBody DeptStatusRequest request){
        deptService.updateStatus(request);
    }

    @PostMapping("/nodesByDeptId/{deptId}")
    public List<DeptTreeNode> nodesByDeptId(@PathVariable("deptId") Long deptId){
        return deptService.searchTree(deptId);
    }

}
