package io.dataease.controller.sys;

import io.dataease.base.domain.SysAuth;
import io.dataease.base.domain.SysAuthDetail;
import io.dataease.base.domain.SysDept;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.handler.annotation.I18n;
import io.dataease.controller.request.BaseTreeRequest;
import io.dataease.controller.request.SysAuthDetailRequest;
import io.dataease.controller.request.SysAuthRequest;
import io.dataease.dto.BaseAuthDetail;
import io.dataease.dto.VAuthModelDTO;
import io.dataease.service.sys.SysAuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Author: wangjiahao
 * Date: 2021-05-11
 * Description:
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：权限管理")
@RequestMapping("/api/sys_auth")
public class SysAuthController {

    @Resource
    private SysAuthService sysAuthService;

    @ApiOperation("查询视图")
    @PostMapping("/authModels")
    public  List<VAuthModelDTO> authModels(@RequestBody BaseTreeRequest request){
        return sysAuthService.searchAuthModelTree(request);
    }


    @ApiOperation("查询授权")
    @PostMapping("/authDetails")
    public Map<String,List<SysAuthDetail>> authDetails(@RequestBody SysAuthRequest request){
        return sysAuthService.searchAuthDetails(request);
    }


    @ApiOperation("查询授权模板")
    @GetMapping("/authDetailsModel/{authType}")
    @I18n
    public List<SysAuthDetail>authDetailsModel(@PathVariable String authType){
        return sysAuthService.searchAuthDetailsModel(authType);
    }


    @ApiOperation("修改权限")
    @PostMapping("/authChange")
    public void authChange(@RequestBody SysAuthRequest request){
        sysAuthService.authChange(request);
    }
}
