package io.dataease.controller.sys;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.base.domain.MyPlugin;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.sys.base.BaseGridRequest;
import io.dataease.controller.sys.request.PluginStatus;
import io.dataease.service.sys.PluginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
    public Pager<List<MyPlugin>> pluginGrid(@PathVariable int goPage, @PathVariable int pageSize, @RequestBody BaseGridRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        return PageUtils.setPageInfo(page, pluginService.query(request));
    }

    @ApiOperation("安装插件")
    @PostMapping("upload")
    public Map<String, Object> localUpload(@RequestParam("file") MultipartFile file) throws Exception {
        return pluginService.localInstall(file);
    }

    @ApiOperation("卸载插件")
    @PostMapping("/uninstall/{pluginId}")
    public Boolean unInstall(@PathVariable Long pluginId) {
        return pluginService.uninstall(pluginId);
    }

    @ApiOperation("切换插件状态")
    @PostMapping("/changeStatus")
    public Boolean changeStatus(@RequestBody PluginStatus pluginStatus) {
        return pluginService.changeStatus(pluginStatus.getPluginId(), pluginStatus.getStatus());
    }
}
