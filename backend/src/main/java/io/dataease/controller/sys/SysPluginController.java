package io.dataease.controller.sys;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.auth.annotation.SqlInjectValidator;
import io.dataease.plugins.common.base.domain.MyPlugin;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.service.sys.PluginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Map;

@ApiIgnore
@RestController
@Api(tags = "系统：插件管理")
@RequestMapping("/api/plugin")
public class SysPluginController {

    @Autowired
    private PluginService pluginService;

    @ApiOperation("查询已安装插件")
    @PostMapping("/pluginGrid/{goPage}/{pageSize}")
    @RequiresPermissions("plugin:read")
    @SqlInjectValidator(value = {"install_time"})
    public Pager<List<MyPlugin>> pluginGrid(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody BaseGridRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, pluginService.query(request));
    }

    @ApiOperation("安装插件")
    @PostMapping("upload")
    @RequiresPermissions("plugin:upload")
    public Map<String, Object> localUpload(@RequestParam("file") MultipartFile file) throws Exception {
        return pluginService.localInstall(file);
    }

    @ApiOperation("卸载插件")
    @PostMapping("/uninstall/{pluginId}")
    @RequiresPermissions("plugin:uninstall")
    public Boolean unInstall(@PathVariable Long pluginId) {
        return pluginService.uninstall(pluginId);
    }

    @ApiOperation("更新插件")
    @PostMapping("/update/{pluginId}")
    @RequiresPermissions("plugin:upload")
    public Map<String, Object> update(@PathVariable("pluginId") Long pluginId, @RequestParam("file") MultipartFile file) throws Exception{
        if (pluginService.uninstall(pluginId)) {
            return pluginService.localInstall(file);
        }
        return null;
    }

}
