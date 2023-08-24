package io.dataease.controller.panel.api;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.annotation.DePermission;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.controller.request.chart.ChartExtRequest;
import io.dataease.controller.request.panel.PanelViewLogRequest;
import io.dataease.controller.request.panel.link.*;
import io.dataease.dto.panel.link.GenerateDto;
import io.dataease.dto.panel.link.ValidateDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

@Api(tags = "仪表板：链接管理")
@ApiSupport(order = 200)
@RequestMapping("/api/link")
public interface LinkApi {

    @DePermission(type = DePermissionType.PANEL, value = "resourceId")
    @ApiOperation("重置密码")
    @PostMapping("/resetPwd")
    void replacePwd(PasswordRequest request);

    @DePermission(type = DePermissionType.PANEL, value = "resourceId")
    @ApiOperation("启用密码")
    @PostMapping("/enablePwd")
    void enablePwd(EnablePwdRequest request);

    @DePermission(type = DePermissionType.PANEL, value = "resourceId")
    @ApiOperation("过期时间")
    @PostMapping("/resetOverTime")
    void resetOverTime(OverTimeRequest request);

    @DePermission(type = DePermissionType.PANEL, value = "resourceId")
    @ApiOperation("启用/禁用链接分享")
    @PostMapping("/switchLink")
    void switchLink(LinkRequest request);

    @DePermission(type = DePermissionType.PANEL)
    @ApiOperation("当前链接信息")
    @PostMapping("/currentGenerate/{resourceId}")
    GenerateDto currentGenerate(String resourceId);

    @ApiOperation("验证访问")
    @PostMapping("/validate")
    ValidateDto validate(LinkValidateRequest request) throws Exception;

    @ApiOperation("验证密码")
    @PostMapping("/validatePwd")
    boolean validatePwd(PasswordRequest request) throws Exception;

    @ApiOperation("资源详细信息")
    @GetMapping("/resourceDetail/{resourceId}/{userId}")
    Object resourceDetail(@PathVariable String resourceId,@PathVariable String userId);

    @ApiOperation("视图详细信息")
    @PostMapping("/viewDetail/{viewId}/{panelId}")
    Object viewDetail(@PathVariable("viewId") String viewId, @PathVariable("panelId") String panelId,
                      @RequestBody ChartExtRequest requestList) throws Exception;

    @ApiOperation("压缩链接")
    @PostMapping("/shortUrl")
    String shortUrl(@RequestBody Map<String, String> param);

    @ApiIgnore
    @PostMapping("/viewLog")
    void viewLinkLog(@RequestBody LinkViewLogRequest request);


}
