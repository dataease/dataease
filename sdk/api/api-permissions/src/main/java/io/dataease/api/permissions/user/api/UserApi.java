package io.dataease.api.permissions.user.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.permissions.role.dto.UserRequest;
import io.dataease.api.permissions.user.dto.*;
import io.dataease.api.permissions.user.vo.*;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.auth.vo.TokenVO;
import io.dataease.model.KeywordRequest;
import io.dataease.request.BaseGridRequest;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.USER;


@Tag(name = "用户")
@ApiSupport(order = 888, author = "fit2cloud-someone")
@DeApiPath(value = "/user", rt = USER)
public interface UserApi {

    @Operation(summary = "查询用户列表")
    @Parameters({
            @Parameter(name = "goPage", description = "目标页码", required = true, in = ParameterIn.PATH),
            @Parameter(name = "pageSize", description = "每页容量", required = true, in = ParameterIn.PATH),
            @Parameter(name = "request", description = "过滤条件", required = true)
    })
    @DePermit("m:read")
    @PostMapping("/pager/{goPage}/{pageSize}")
    IPage<UserGridVO> pager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody BaseGridRequest request);

    @Operation(summary = "查询用户详情")
    @Parameter(name = "id", description = "ID", required = true, in = ParameterIn.PATH)
    @DePermit({"m:read", "#p0 + ':read'"})
    @GetMapping("/queryById/{id}")
    UserFormVO queryById(@PathVariable("id") Long id);


    @Operation(summary = "查询个人信息")
    @GetMapping("/personInfo")
    UserFormVO personInfo();

    @Operation(summary = "创建")
    @DePermit("m:read")
    @PostMapping("/create")
    void create(@RequestBody UserCreator creator);

    @Operation(summary = "编辑")
    @DePermit({"m:read", "#p0.id + ':manage'"})
    @PostMapping("/edit")
    void edit(@RequestBody UserEditor editor);

    @Operation(summary = "变更个人信息")
    @PostMapping("/personEdit")
    void personEdit(@RequestBody UserEditor editor);

    @Operation(summary = "删除")
    @Parameter(name = "id", description = "ID", required = true, in = ParameterIn.PATH)
    @DePermit({"m:read", "#p0 + ':manage'"})
    @PostMapping("/delete/{id}")
    void delete(@PathVariable("id") Long id);

    @Operation(summary = "批量删除")
    @DePermit({"m:read", "#p0 + ':manage'"})
    @PostMapping("/batchDel")
    void batchDel(@RequestBody List<Long> ids);

    @Operation(summary = "角色可绑用户")
    @PostMapping("/role/option")
    List<UserItemVO> optionForRole(@RequestBody UserRequest request);

    @Operation(summary = "角色已绑用户")
    @Parameters({
            @Parameter(name = "goPage", description = "目标页码", required = true, in = ParameterIn.PATH),
            @Parameter(name = "pageSize", description = "每页容量", required = true, in = ParameterIn.PATH),
            @Parameter(name = "request", description = "过滤条件", required = true)
    })
    @PostMapping("/role/selected/{goPage}/{pageSize}")
    IPage<UserItemVO> selectedForRole(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody UserRequest request);

    @Operation(summary = "切换组织")
    @Parameter(name = "oId", description = "目标组织ID", required = true, in = ParameterIn.PATH)
    @PostMapping("/switch/{oId}")
    TokenVO switchOrg(@PathVariable("oId") Long oId);

    @Operation(summary = "获取当前登录人信息")
    @GetMapping("/info")
    CurUserVO info();

    @Operation(summary = "查询当前组织内用户")
    @PostMapping("/byCurOrg")
    List<UserItem> byCurOrg(@RequestBody KeywordRequest request);

    @Operation(summary = "用户数量", hidden = true)
    @Hidden
    @GetMapping("/userCount")
    int userCount();

    @Operation(summary = "切换语言")
    @PostMapping("/switchLanguage")
    void switchLanguage(@RequestBody LangSwitchRequest request);

    @Operation(summary = "下载批量导入模版")
    @PostMapping("/excelTemplate")
    void excelTemplate();

    @Operation(summary = "批量导入")
    @PostMapping("/batchImport")
    UserImportVO batchImport(@RequestPart(value = "file") MultipartFile file);


    @Operation(summary = "下载批量导入失败记录")
    @Parameter(name = "key", description = "导入结果key", required = true, in = ParameterIn.PATH)
    @GetMapping("/errorRecord/{key}")
    void errorRecord(@PathVariable("key") String key);

    @Operation(summary = "清理批量导入失败记录")
    @Parameter(name = "key", description = "导入结果key", required = true, in = ParameterIn.PATH)
    @GetMapping("/clearErrorRecord/{key}")
    void clearErrorRecord(@PathVariable("key") String key);

    @Operation(summary = "查询默认密码")
    @DePermit({"m:read"})
    @GetMapping("/defaultPwd")
    String defaultPwd();

    @Operation(summary = "重置为默认密码")
    @Parameter(name = "id", description = "用户ID", required = true, in = ParameterIn.PATH)
    @DePermit({"m:read", "#p0 + ':manage'"})
    @PostMapping("/resetPwd/{id}")
    void resetPwd(@PathVariable("id") Long id);

    @Operation(summary = "切换用户状态")
    @DePermit({"m:read", "#p0.id + ':manage'"})
    @PostMapping("/enable")
    void enable(@RequestBody EnableSwitchRequest request);

    @Operation(summary = "修改个人密码")
    @PostMapping("/modifyPwd")
    void modifyPwd(@RequestBody ModifyPwdRequest request);

    @Hidden
    @GetMapping("/firstEchelon/{limit}")
    List<Long> firstEchelon(@PathVariable("limit") Long limit);

    @Hidden
    @GetMapping("/queryByAccount")
    CurUserVO queryByAccount(String account);

    @Hidden
    @PostMapping("/all")
    List<UserItem> allUser(@RequestBody KeywordRequest request);
}
