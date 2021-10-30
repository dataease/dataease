package io.dataease.plugins.server;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.theme.dto.ThemeDto;
import io.dataease.plugins.xpack.theme.dto.ThemeItem;
import io.dataease.plugins.xpack.theme.dto.ThemeRequest;
import io.dataease.plugins.xpack.theme.service.ThemeXpackService;

@RequestMapping("/plugin/theme")
@RestController
public class ThemeServer {

    

    @PostMapping("/themes")
    public List<ThemeDto> themes(){

        ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);
        return themeXpackService.themes();
    }

    @PostMapping("/items/{themeId}")
    public List<ThemeItem> themeItems(@PathVariable("themeId") int themeId) {
        ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);
        return themeXpackService.queryItems(themeId);
    }

    @PostMapping("/save")
    public void save(@RequestPart("request") ThemeRequest request, @RequestPart(value = "file", required = false) MultipartFile bodyFile) {
        ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);
        themeXpackService.save(request, bodyFile);
    }

    @PostMapping("/delete/{themeId}")
    public void save(@PathVariable("themeId") int themeId) {
        ThemeXpackService themeXpackService = SpringContextUtil.getBean(ThemeXpackService.class);
        themeXpackService.deleteTheme(themeId);
    }
    
}
