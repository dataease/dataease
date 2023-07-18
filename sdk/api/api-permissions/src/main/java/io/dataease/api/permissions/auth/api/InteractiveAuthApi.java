package io.dataease.api.permissions.auth.api;


import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.permissions.auth.dto.BusiResourceCreator;
import io.dataease.api.permissions.auth.dto.BusiResourceEditor;
import io.dataease.api.permissions.auth.dto.OutAuthPlatformLoginRequest;
import io.dataease.api.permissions.auth.vo.BusiPerVO;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "内部资源交互")
@ApiSupport(order = 998)
// @Hidden
public interface InteractiveAuthApi {

    @Operation(summary = "查询菜单ID")
    @ApiOperationSupport(order = 1)
    @GetMapping("/menuIds")
    List<Long> menuIds();


    @Operation(summary = "查询资源树")
    @ApiOperationSupport(order = 2)
    @Parameter(name = "flag", description = "类型")
    @GetMapping("/resource/{flag}")
    List<BusiPerVO> resource(@PathVariable("flag") String flag);

    /**
     * 下面3个接口为内部调用接口不对外开放
     *
     * @param creator
     */

    @Operation(summary = "同步保存资源")
    @ApiOperationSupport(order = 3)
    @PostMapping("/resource/create")
    void saveResource(@RequestBody BusiResourceCreator creator);

    @Operation(summary = "同步更新资源")
    @ApiOperationSupport(order = 4)
    @PostMapping("/resource/edit")
    void editResource(@RequestBody BusiResourceEditor editor);

    @Operation(summary = "同步删除资源")
    @ApiOperationSupport(order = 5)
    @GetMapping("/resource/del/{id}")
    void delResource(@PathVariable("id") Long id);

    @Operation(summary = "删除检测")
    @ApiOperationSupport(order = 6)
    @GetMapping("/resource/checkDel/{id}")
    boolean checkDel(@PathVariable("id") Long id);

    @Operation(hidden = true)
    String outAuthPlatformLogin(@RequestBody OutAuthPlatformLoginRequest request);

}
