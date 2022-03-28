package io.dataease.controller.engine;

import io.dataease.base.domain.DeEngine;
import io.dataease.controller.ResultHolder;
import io.dataease.dto.DatasourceDTO;
import io.dataease.service.engine.EngineService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;

@ApiIgnore
@RequestMapping("engine")
@RestController
public class EngineController {

    @Resource
    private EngineService engineService;

    @ApiIgnore
    @GetMapping("/mode")
    public String  runMode() throws Exception{
        return engineService.mode();
    }

    @RequiresPermissions("sysparam:read")
    @ApiOperation("引擎信息")
    @GetMapping("/info")
    public DeEngine info() throws Exception{
        return engineService.info();
    }

    @ApiIgnore
    @PostMapping("/validate")
    public ResultHolder validate(@RequestBody DatasourceDTO datasource) throws Exception {
        return engineService.validate(datasource);
    }


    @RequiresPermissions("sysparam:read")
    @ApiOperation("新增/编辑")
    @PostMapping("/save")
    public ResultHolder save(@RequestBody DeEngine engine) throws Exception {
        return engineService.save(engine);
    }



}
