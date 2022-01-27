package io.dataease.plugins.server;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.dataease.commons.exception.DEException;
import io.dataease.commons.utils.LogUtil;
import io.dataease.i18n.Translator;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.theme.dto.ThemeDto;
import io.dataease.plugins.xpack.theme.dto.ThemeItem;
import io.dataease.plugins.xpack.theme.dto.ThemeRequest;
import io.dataease.plugins.xpack.theme.service.ThemeXpackService;

@RequestMapping("/plugin/theme")
@RestController
public class ThemeServer {

    @PostMapping("/themes")
    public List<ThemeDto> themes() {

        ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);
        return themeXpackService.themes();
    }

    @PostMapping("/items/{themeId}")
    public List<ThemeItem> themeItems(@PathVariable("themeId") int themeId) {
        ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);
        return themeXpackService.queryItems(themeId);
    }

    @PostMapping("/save")
    public void save(@RequestPart("request") ThemeRequest request,
            @RequestPart(value = "file", required = false) MultipartFile bodyFile) {
        ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);
        try {
            themeXpackService.save(request, bodyFile);
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

    @PostMapping("/delete/{themeId}")
    public void delete(@PathVariable("themeId") int themeId) {
        ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);
        themeXpackService.deleteTheme(themeId);
    }

}
