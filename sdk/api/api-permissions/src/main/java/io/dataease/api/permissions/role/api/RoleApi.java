package io.dataease.api.permissions.role.api;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.permissions.role.dto.*;
import io.dataease.api.permissions.role.vo.ExternalUserVO;
import io.dataease.api.permissions.role.vo.RoleDetailVO;
import io.dataease.api.permissions.role.vo.RoleVO;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.model.KeywordRequest;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.ROLE;

@Tag(name = "角色")
@ApiSupport(order = 887, author = "fit2cloud-someone")
@DeApiPath(value = "/role", rt = ROLE)
public interface RoleApi {

    @Operation(summary = "查询")
    @DePermit("m:read")
    @PostMapping("/query")
    List<RoleVO> query(@RequestBody KeywordRequest request);

    @Operation(summary = "创建")
    @DePermit("m:read")
    @PostMapping("/create")
    void create(@RequestBody RoleCreator creator);

    @Operation(summary = "编辑")
    @DePermit({"m:read", "#p0.id + ':manage'"})
    @PostMapping("/edit")
    void edit(@RequestBody RoleEditor editor);

    @Operation(summary = "绑定用户")
    @DePermit({"m:read", "#p0.rid + ':manage'"})
    @PostMapping("/mountUser")
    void mountUser(@RequestBody MountUserRequest request);

    @Operation(summary = "绑定组织外用户")
    @DePermit({"m:read", "#p0.rid + ':manage'"})
    @PostMapping("/mountExternalUser")
    void mountExternalUser(@RequestBody MountExternalUserRequest request);

    @Operation(summary = "查询组织外用户")
    @GetMapping("/searchExternalUser/{keyword}")
    ExternalUserVO searchExternalUser(@PathVariable("keyword") String keyword);

    @Operation(summary = "解绑用户")
    @DePermit({"m:read", "#p0.rid + ':manage'"})
    @PostMapping("/unMountUser")
    void unMountUser(@RequestBody UnmountUserRequest request);

    @Operation(summary = "用户可选角色")
    @PostMapping("/user/option")
    List<RoleVO> optionForUser(@RequestBody RoleRequest request);

    @Operation(summary = "用户已选角色")
    @PostMapping("/user/selected")
    List<RoleVO> selectedForUser(@RequestBody RoleRequest request);

    @Operation(summary = "角色详情")
    @Parameter(name = "rid", description = "角色ID", required = true, in = ParameterIn.PATH)
    @GetMapping("/detail/{rid}")
    RoleDetailVO detail(@PathVariable("rid") Long rid);

    @Operation(summary = "删除角色")
    @Parameter(name = "rid", description = "角色ID", required = true, in = ParameterIn.PATH)
    @DePermit({"m:manage", "#p0 + ':manage'"})
    @PostMapping("/delete/{rid}")
    void delete(@PathVariable("rid") Long rid);

    @Operation(summary = "解绑用户询问")
    @PostMapping("/beforeUnmountInfo")
    Integer beforeUnmountInfo(@RequestBody UnmountUserRequest request);

    @Operation(summary = "复制", hidden = true)
    @PostMapping("/copy")
    void copy(@RequestBody RoleCopyRequest request);

    @Operation(summary = "查询组织内角色")
    @PostMapping("/byCurOrg")
    List<RoleVO> byCurOrg(@RequestBody KeywordRequest request);

    @Hidden
    @GetMapping("/queryWithOid/{oid}")
    List<RoleVO> queryWithOid(@PathVariable("oid") Long oid);
}
