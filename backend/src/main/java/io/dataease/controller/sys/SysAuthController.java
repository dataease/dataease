package io.dataease.controller.sys;

import io.dataease.base.domain.SysAuthDetail;
import io.dataease.controller.handler.annotation.I18n;
import io.dataease.controller.request.SysAuthRequest;
import io.dataease.dto.SysAuthDetailDTO;
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


    @ApiOperation("查询授权")
    @PostMapping("/authDetails")
    public Map<String,List<SysAuthDetailDTO>> authDetails(@RequestBody SysAuthRequest request){
        return sysAuthService.searchAuthDetails(request);
    }


    @ApiOperation("查询授权模板")
    @GetMapping("/authDetailsModel/{authType}")
    @I18n
    public List<SysAuthDetail>authDetailsModel(@PathVariable String authType){
        return sysAuthService.searchAuthDetailsModel(authType);
    }

}
