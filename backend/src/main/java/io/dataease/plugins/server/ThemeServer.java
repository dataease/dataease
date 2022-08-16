package io.dataease.plugins.server;

import java.util.List;

import io.dataease.plugins.xpack.theme.dto.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.LogUtil;
import io.dataease.i18n.Translator;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.theme.service.ThemeXpackService;

@Api(tags = "xpack：系统主题")
@RequestMapping("/plugin/theme")
@RestController
public class ThemeServer {

    @ApiOperation("查询所有")
    @PostMapping("/themes")
    public List<ThemeBaseDTO> themes(@RequestBody List<String> keys) {

        ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);

        return themeXpackService.themes(keys);
    }

    @ApiOperation("查询配置项")
    @PostMapping("/items/{themeId}")
    public List<ThemeItem> themeItems(@PathVariable("themeId") int themeId) {
        ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);
        return themeXpackService.queryItems(themeId);
    }

    @ApiOperation("保存")
    @RequiresPermissions("sysparam:read")
    @PostMapping("/save")
    public void save(@RequestBody ThemeCreateRequest request) {
        ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);
        try {
            themeXpackService.addTheme(request);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            if (ObjectUtils.isNotEmpty(e.getMessage()) && e.getMessage().indexOf("theme_name_repeat") != -1) {
                DEException.throwException(Translator.get("theme_name_repeat"));
            } else if (ObjectUtils.isNotEmpty(e.getMessage()) && e.getMessage().indexOf("theme_name_empty") != -1) {
                DEException.throwException(Translator.get("theme_name_empty"));
            } else {
                DEException.throwException(e);
            }
        }

    }

    @ApiOperation("重命名")
    @PostMapping("/rename")
    public void renameTheme(@RequestBody ThemeRenameRequest request) {
        if (request.getId() < 3) {
            throw new RuntimeException("default theme can not execute rename");
        }
        try {
            ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);
            themeXpackService.renameTheme(request);
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            if (ObjectUtils.isNotEmpty(e.getMessage()) && e.getMessage().indexOf("theme_name_repeat") != -1) {
                DEException.throwException(Translator.get("theme_name_repeat"));
            } else if (ObjectUtils.isNotEmpty(e.getMessage()) && e.getMessage().indexOf("theme_name_empty") != -1) {
                DEException.throwException(Translator.get("theme_name_empty"));
            } else {
                DEException.throwException(e);
            }
        }

    }

    @ApiOperation("切换高级开关")
    @PostMapping("/enableSenior/{themeId}")
    public void enableSenior(@PathVariable("themeId") Integer themeId) {
        ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);
        themeXpackService.switchSenior(themeId);
    }

    @ApiOperation("应用")
    @PostMapping("/activeTheme/{themeId}")
    public void activeTheme(@PathVariable("themeId") Integer themeId) {
        ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);
        themeXpackService.switchStatus(themeId);
    }

    @ApiOperation("删除")
    @RequiresPermissions("sysparam:read")
    @PostMapping("/delete/{themeId}")
    public void delete(@PathVariable("themeId") int themeId) {
        ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);
        themeXpackService.deleteTheme(themeId);
    }

    @ApiOperation("保存配置项")
    @PostMapping("/saveThemeItems")
    public void saveThemeItems(@RequestBody ThemeRequest request) {
        ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);
        themeXpackService.saveThemeItems(request);
    }

}
