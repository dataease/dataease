package io.dataease.api.permissions.auth.api;


import io.dataease.api.permissions.auth.dto.*;
import io.dataease.api.permissions.auth.vo.PermissionVO;
import io.dataease.api.permissions.auth.vo.ResourceVO;
import io.dataease.auth.DeApiPath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
@Api(tags = "系统：权限管理")
@DeApiPath("/auth")
public interface AuthApi {

    @ApiOperation("查询资源树")
    @GetMapping("/busiResource/{flag}")
    List<ResourceVO> busiResource(@PathVariable("flag") String flag);

    @ApiOperation("查询资源权限")
    @PostMapping("/busiPermission")
    List<PermissionVO> busiPermission(@RequestBody BusiPermissionRequest request);

    @ApiOperation("查询菜单树")
    @GetMapping("/menuResource")
    List<ResourceVO> menuResource();

    @ApiOperation("查询菜单权限")
    @PostMapping("/menuPermission")
    List<PermissionVO> menuPermission(@RequestBody MenuPermissionRequest request);

    @ApiOperation("保存资源权限")
    @PostMapping("/saveBusiPer")
    void saveBusiPer(@RequestBody BusiPerEditor editor);

    @ApiOperation("保存菜单权限")
    @PostMapping("/saveMenuPer")
    void saveMenuPer(@RequestBody MenuPerEditor editor);

    @ApiIgnore
    @ApiOperation("保存业务资源")
    @PostMapping("/resource/create")
    void saveResource(@RequestBody BusiResourceCreator creator);

}
