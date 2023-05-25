package io.dataease.api.permissions.auth.api;


import io.dataease.api.permissions.auth.dto.*;
import io.dataease.api.permissions.auth.vo.PermissionVO;
import io.dataease.api.permissions.auth.vo.ResourceVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@Api(tags = "系统：权限管理")
public interface AuthApi {

    @ApiOperation("查询资源树")
    @GetMapping("/busiResource/{flag}")
    List<ResourceVO> busiResource(@PathVariable("flag") String flag);

    @ApiOperation("查询对象已授权资源")
    @PostMapping("/busiPermission")
    PermissionVO busiPermission(@RequestBody BusiPermissionRequest request);

    @ApiOperation("查询资源已授权对象")
    @PostMapping("/busiTargetPermission")
    PermissionVO busiTargetPermission(@RequestBody BusiPermissionRequest request);

    @ApiOperation("查询菜单树")
    @GetMapping("/menuResource")
    List<ResourceVO> menuResource();

    @ApiOperation("查询对象已授权菜单")
    @PostMapping("/menuPermission")
    PermissionVO menuPermission(@RequestBody MenuPermissionRequest request);

    @ApiOperation("查询菜单已授权对象")
    @PostMapping("/menuTargetPermission")
    PermissionVO menuTargetPermission(@RequestBody MenuPermissionRequest request);

    @ApiOperation("保存资源权限")
    @PostMapping("/saveBusiPer")
    void saveBusiPer(@RequestBody BusiPerEditor editor);

    @ApiOperation("资源维度保存权限")
    @PostMapping("/saveBusiTargetPer")
    void saveBusiTargetPer(@RequestBody BusiTargetPerCreator creator);

    @ApiOperation("保存菜单权限")
    @PostMapping("/saveMenuPer")
    void saveMenuPer(@RequestBody MenuPerEditor editor);

    @ApiOperation("菜单维度保存权限")
    @PostMapping("/saveMenuTargetPer")
    void saveMenuTargetPer(@RequestBody MenuTargetPerCreator creator);

    /**
     * 下面3个接口为内部调用接口不对外开放
     *
     * @param creator
     */
    @ApiIgnore
    @ApiOperation("保存业务资源")
    @PostMapping("/resource/create")
    void saveResource(@RequestBody BusiResourceCreator creator);

    @ApiIgnore
    @ApiOperation("更新业务资源")
    @PostMapping("/resource/edit")
    void editResource(@RequestBody BusiResourceEditor editor);

    @ApiIgnore
    @ApiOperation("删除业务资源")
    @GetMapping("/resource/del/{id}")
    void delResource(@PathVariable("id") Long id);

}
