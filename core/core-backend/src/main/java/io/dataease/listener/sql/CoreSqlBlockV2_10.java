package io.dataease.listener.sql;

import io.dataease.copilot.dao.auto.entity.CoreCopilotConfig;
import io.dataease.copilot.dao.auto.entity.CoreCopilotToken;
import io.dataease.copilot.dao.auto.mapper.CoreCopilotConfigRepository;
import io.dataease.copilot.dao.auto.mapper.CoreCopilotTokenRepository;
import io.dataease.font.dao.auto.entity.CoreFont;
import io.dataease.font.dao.auto.mapper.CoreFontRepository;
import io.dataease.initSql.Version;
import io.dataease.menu.dao.auto.entity.CoreMenu;
import io.dataease.menu.dao.auto.mapper.CoreMenuRepository;
import io.dataease.startup.dao.auto.entity.CoreSysStartupJob;
import io.dataease.startup.dao.auto.mapper.CoreSysStartupJobRepository;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import io.dataease.visualization.dao.auto.mapper.VisualizationBackgroundRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CoreSqlBlockV2_10 implements CoreSqlBlock {

    @Resource
    private CoreSysStartupJobRepository coreSysStartupJobRepository;
    @Resource
    private CoreCopilotTokenRepository coreCopilotTokenRepository;
    @Resource
    private CoreCopilotConfigRepository coreCopilotConfigRepository;
    @Resource
    private CoreFontRepository coreFontRepository;
    @Resource
    private CoreMenuRepository coreMenuRepository;

    @Override
    public Version getVersion() {
        return new Version("2.10");
    }

    @Override
    public void execute() {
        CoreSysStartupJob coreSysStartupJob = new CoreSysStartupJob();
        coreSysStartupJob.setId("chartFilterDynamic");
        coreSysStartupJob.setName("chartFilterDynamic");
        coreSysStartupJob.setStatus("ready");
        coreSysStartupJobRepository.saveAndFlush(coreSysStartupJob);

        List<CoreMenu> coreMenus = Arrays.asList(
                createCoreMenu(64L, 15L, 2, "font", "system/font", 10, "icon_font", "/font", false, true, false)
        );
        coreMenuRepository.saveAllAndFlush(coreMenus);
        CoreFont coreFont = new CoreFont();
        coreFont.setId(1L);
        coreFont.setName("PingFang");
        coreFont.setIsDefault(true);
        coreFont.setIsBuiltin(true);
        coreFont.setUpdateTime(0L);
        coreFontRepository.saveAndFlush(coreFont);
    }

    private CoreMenu createCoreMenu(Long id, Long pid, Integer type, String name, String component, Integer menuSort, String icon, String path, Boolean hidden, Boolean inLayout, Boolean auth) {
        CoreMenu menu = new CoreMenu();
        menu.setId(id);
        menu.setPid(pid);
        menu.setType(type);
        menu.setName(name);
        menu.setComponent(component);
        menu.setMenuSort(menuSort);
        menu.setIcon(icon);
        menu.setPath(path);
        menu.setHidden(hidden);
        menu.setInLayout(inLayout);
        menu.setAuth(auth);
        return menu;
    }

}
