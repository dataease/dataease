package io.dataease.api.permissions.auth.api;


import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.permissions.auth.dto.*;
import io.dataease.api.permissions.auth.vo.PermissionVO;
import io.dataease.api.permissions.auth.vo.ResourceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "权限管理")
@ApiSupport(order = 885, author = "fit2cloud-someone")
public interface AuthApi {

    @Operation(summary = "查询资源树")
    @ApiOperationSupport(order = 1)
    @Parameter(name = "flag", description = "类型")
    @GetMapping("/busiResource/{flag}")
    List<ResourceVO> busiResource(@PathVariable("flag") String flag);

    @Operation(summary = "查询对象已授权资源")
    @ApiOperationSupport(order = 3)
    @PostMapping("/busiPermission")
    PermissionVO busiPermission(@RequestBody BusiPermissionRequest request);

    @Operation(summary = "查询资源已授权对象")
    @ApiOperationSupport(order = 5)
    @PostMapping("/busiTargetPermission")
    PermissionVO busiTargetPermission(@RequestBody BusiPermissionRequest request);

    @Operation(summary = "查询菜单树")
    @ApiOperationSupport(order = 2)
    @GetMapping("/menuResource")
    List<ResourceVO> menuResource();

    @Operation(summary = "查询对象已授权菜单")
    @ApiOperationSupport(order = 4)
    @PostMapping("/menuPermission")
    PermissionVO menuPermission(@RequestBody MenuPermissionRequest request);

    @Operation(summary = "查询菜单已授权对象")
    @ApiOperationSupport(order = 6)
    @PostMapping("/menuTargetPermission")
    PermissionVO menuTargetPermission(@RequestBody MenuPermissionRequest request);

    @Operation(summary = "保存资源权限")
    @ApiOperationSupport(order = 7)
    @PostMapping("/saveBusiPer")
    void saveBusiPer(@RequestBody BusiPerEditor editor);

    @Operation(summary = "资源维度保存权限")
    @ApiOperationSupport(order = 9)
    @PostMapping("/saveBusiTargetPer")
    void saveBusiTargetPer(@RequestBody BusiTargetPerCreator creator);

    @Operation(summary = "保存菜单权限")
    @ApiOperationSupport(order = 8)
    @PostMapping("/saveMenuPer")
    void saveMenuPer(@RequestBody MenuPerEditor editor);

    @Operation(summary = "菜单维度保存权限")
    @ApiOperationSupport(order = 10)
    @PostMapping("/saveMenuTargetPer")
    void saveMenuTargetPer(@RequestBody MenuTargetPerCreator creator);


}
