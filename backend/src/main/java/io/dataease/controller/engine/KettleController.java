package io.dataease.controller.engine;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.ResultHolder;
import io.dataease.dto.KettleDTO;
import io.dataease.plugins.common.base.domain.DeEngine;
import io.dataease.service.kettle.KettleService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import java.util.List;

@ApiIgnore
@RequestMapping("kettle")
@RestController
public class KettleController {

    @Resource
    private KettleService kettleService;

    @RequiresPermissions("sysparam:read")
    @ApiOperation("新增/编辑")
    @PostMapping("save")
    public ResultHolder save(@RequestBody DeEngine engine) throws Exception{
        return kettleService.save(engine);
    }

    @ApiIgnore
    @PostMapping("validate")
    public  void validate(@RequestBody KettleDTO kettleDTO) throws Exception{
        kettleService.validate(kettleDTO);
    }

    @RequiresPermissions("sysparam:read")
    @ApiOperation("校验")
    @PostMapping("validate/{id}")
    public ResultHolder validate(@PathVariable String id) throws Exception{
        return kettleService.validate(id);
    }

    @RequiresPermissions("sysparam:read")
    @ApiOperation("查询")
    @PostMapping("/pageList/{goPage}/{pageSize}")
    public Pager<List<DeEngine>> pageList(@PathVariable int goPage, @PathVariable int pageSize) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, kettleService.pageList());
    }

    @RequiresPermissions("sysparam:read")
    @ApiOperation("删除")
    @DeleteMapping("delete/{id}")
    public void delete(@PathVariable String id) throws Exception{
        kettleService.delete(id);
    }
}
