package io.dataease.controller.datasource;


import io.dataease.auth.annotation.DeLog;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.dto.DriverDTO;
import io.dataease.plugins.common.base.domain.DeDriver;
import io.dataease.plugins.common.base.domain.DeDriverDetails;
import io.dataease.service.datasource.DriverService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@ApiIgnore
@RequestMapping("driver")
@RestController
public class DriverMgmController {

    @Resource
    private DriverService driverService;

    @RequiresPermissions("datasource:read")
    @ApiOperation("驱动列表")
    @PostMapping("/list")
    public List<DriverDTO> listDeDriver() throws Exception{
        return driverService.list();
    }

    @RequiresPermissions("datasource:read")
    @ApiOperation("删除驱动")
    @PostMapping("/delete")
    @DeLog(
            operatetype = SysLogConstants.OPERATE_TYPE.DELETE,
            sourcetype = SysLogConstants.SOURCE_TYPE.DRIVER,
            positionIndex = 0,
            positionKey = "type",
            value = "id"
    )
    public void delete(@RequestBody DeDriver deDriver) throws Exception{
        driverService.delete(deDriver);
    }

    @RequiresPermissions("datasource:read")
    @ApiOperation("驱动列表")
    @GetMapping("/list/{type}")
    public List<DriverDTO> listDeDriver(@PathVariable String type) throws Exception{
        return listDeDriver().stream().filter(driverDTO -> driverDTO.getType().equalsIgnoreCase(type)).collect(Collectors.toList());
    }

    @RequiresPermissions("datasource:read")
    @ApiOperation("添加驱动")
    @PostMapping("/save")
    @DeLog(
            operatetype = SysLogConstants.OPERATE_TYPE.CREATE,
            sourcetype = SysLogConstants.SOURCE_TYPE.DRIVER,
            positionIndex = 0,
            positionKey = "type",
            value = "id"
    )
    public DeDriver save(@RequestBody DeDriver deDriver) throws Exception{
        return driverService.save(deDriver);
    }

    @RequiresPermissions("datasource:read")
    @ApiOperation("更新驱动")
    @PostMapping("/update")
    @DeLog(
            operatetype = SysLogConstants.OPERATE_TYPE.MODIFY,
            sourcetype = SysLogConstants.SOURCE_TYPE.DRIVER,
            positionIndex = 0,positionKey = "type",
            value = "id"
    )
    public DeDriver update(@RequestBody DeDriver deDriver) throws Exception{
        return driverService.update(deDriver);
    }

    @RequiresPermissions("datasource:read")
    @ApiOperation("驱动文件列表")
    @GetMapping("/listDriverDetails/{id}")
    public List<DeDriverDetails> listDriverDetails(@PathVariable String id) throws Exception{
        return driverService.listDriverDetails(id);
    }

    @RequiresPermissions("datasource:read")
    @ApiOperation("删除驱动文件")
    @PostMapping("/deleteDriverFile")
    public void deleteDriverFile(@RequestBody DeDriverDetails deDriverDetails) throws Exception{
        driverService.deleteDriverFile(deDriverDetails.getId());
    }

    @RequiresPermissions("datasource:read")
    @ApiOperation("驱动文件上传")
    @PostMapping("file/upload")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件", required = true, dataType = "MultipartFile"),
            @ApiImplicitParam(name = "id", value = "驱动D", required = true, dataType = "String")
    })
    public DeDriverDetails excelUpload(@RequestParam("id") String id, @RequestParam("file") MultipartFile file) throws Exception {
        return driverService.saveJar(file, id);
    }



}
