package io.dataease.controller.sys;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.sys.request.SysUserCreateRequest;
import io.dataease.controller.sys.request.UserGridRequest;
import io.dataease.controller.sys.response.SysUserGridResponse;
import io.dataease.service.sys.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

@RestController
@Api(tags = "系统：用户管理")
@RequestMapping("/api/user")
public class SysUserController {

    @Resource
    private SysUserService sysUserService;

    @ApiOperation("查询用户")
    @PostMapping("/userGrid/{goPage}/{pageSize}")
    public Pager<List<SysUserGridResponse>> userGrid(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody UserGridRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, sysUserService.query(request));
    }

    @ApiOperation("创建用户")
    @PostMapping("/create")
    public void create(@RequestBody SysUserCreateRequest request){
        sysUserService.save(request);
    }

    @ApiOperation("更新用户")
    @PostMapping("/updat")
    public void update(@RequestBody SysUserCreateRequest request){
        sysUserService.update(request);
    }

    @ApiOperation("更新用户")
    @PostMapping("/updat/{userId}")
    public void delete(@PathVariable("userId") Long userId){
        sysUserService.delete(userId);
    }
}
