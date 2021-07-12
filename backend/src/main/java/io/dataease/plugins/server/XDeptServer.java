package io.dataease.plugins.server;


import io.dataease.commons.utils.BeanUtils;
import io.dataease.controller.sys.response.DeptNodeResponse;
import io.dataease.plugins.common.entity.XpackGridRequest;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.dept.dto.request.XpackCreateDept;
import io.dataease.plugins.xpack.dept.dto.request.XpackDeleteDept;
import io.dataease.plugins.xpack.dept.dto.request.XpackMoveDept;
import io.dataease.plugins.xpack.dept.dto.response.XpackDeptTreeNode;
import io.dataease.plugins.xpack.dept.dto.response.XpackSysDept;
import io.dataease.plugins.xpack.dept.service.DeptXpackService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/plugin/dept")
@RestController
public class XDeptServer {

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

    @PostMapping("/search")
    public List<DeptNodeResponse> search(@RequestBody XpackGridRequest request){
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        List<XpackSysDept> ndoes = deptService.nodesTreeByCondition(request);
        List<DeptNodeResponse> nodeResponses = ndoes.stream().map(node -> {
            DeptNodeResponse deptNodeResponse = BeanUtils.copyBean(new DeptNodeResponse(), node);
            deptNodeResponse.setHasChildren(node.getSubCount() > 0);
            deptNodeResponse.setLeaf(node.getSubCount() == 0);
            deptNodeResponse.setTop(node.getPid() == 0L);
            return deptNodeResponse;
        }).collect(Collectors.toList());
        return nodeResponses;
    }

    @PostMapping("/root")
    public  List<XpackSysDept> rootData(){
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        List<XpackSysDept> nodes = deptService.nodesByPid(null);
        return nodes;
    }

    @PostMapping("/create")
    public int create(@RequestBody XpackCreateDept dept){
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        return deptService.add(dept);
    }

    @PostMapping("/delete")
    public void delete(@RequestBody List<XpackDeleteDept> requests){
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        deptService.batchDelete(requests);
    }

    @PostMapping("/update")
    public int update(@RequestBody XpackCreateDept dept){
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        return deptService.update(dept);
    }


    @PostMapping("/nodesByDeptId/{deptId}")
    public List<XpackDeptTreeNode> nodesByDeptId(@PathVariable("deptId") Long deptId){
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        return deptService.searchTree(deptId);
    }

    @PostMapping("/move")
    public void move(@RequestBody XpackMoveDept xpackMoveDept){
        DeptXpackService deptService = SpringContextUtil.getBean(DeptXpackService.class);
        deptService.move(xpackMoveDept);
    }
}
