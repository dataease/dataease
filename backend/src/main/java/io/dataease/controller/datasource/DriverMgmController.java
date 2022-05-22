package io.dataease.controller.datasource;


import io.dataease.dto.DriverDTO;
import io.dataease.plugins.common.base.domain.DeDriver;
import io.dataease.plugins.common.base.domain.DeDriverDetails;
import io.dataease.service.datasource.DriverService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.security.core.parameters.P;
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
    public void delete(@RequestBody DeDriver deDriver) throws Exception{
        driverService.delete(deDriver.getId());
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
    public DeDriver save(@RequestBody DeDriver deDriver) throws Exception{
        return driverService.save(deDriver);
    }

    @RequiresPermissions("datasource:read")
    @ApiOperation("更新驱动")
    @PostMapping("/update")
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
    @PostMapping("/deleteDriverFile/{id}")
    public void deleteDriverFile(@PathVariable String id) throws Exception{
        driverService.deleteDriverFile(id);
    }

    @RequiresPermissions("datasource:read")
    @ApiOperation("驱动文件上传")
    @PostMapping("file/upload")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "file", value = "文件", required = true, dataType = "MultipartFile"),
            @ApiImplicitParam(name = "id", value = "驱动D", required = true, dataType = "String")
    })
    public void excelUpload(@RequestParam("file") MultipartFile file, @RequestParam("id") String id) throws Exception {
        driverService.saveJar(file, id);
    }



}
