package io.dataease.controller.sys;

import io.dataease.base.domain.SysDept;
import io.dataease.controller.ResultHolder;
import io.dataease.service.sys.DeptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Api(tags = "系统：部门管理")
@RequestMapping("/api/dept")
public class SysDeptController extends ResultHolder {


    @Resource
    private DeptService deptService;

    @ApiOperation("查询部门")
    @PostMapping("/root")
    public ResultHolder rootData(){
        List<SysDept> root = deptService.root();
        return success(root);
    }

    @ApiOperation("新增部门")
    @PostMapping("/create")
    public void create(@RequestBody SysDept dept){
        deptService.add(dept);
    }

}
