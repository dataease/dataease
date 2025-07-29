package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import io.dataease.menu.dao.auto.entity.CoreMenu;
import io.dataease.menu.dao.auto.mapper.CoreMenuRepository;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class CoreSqlBlockV2_1 implements CoreSqlBlock {

    @Resource
    private CoreMenuRepository coreMenuRepository;
    @Resource
    private CoreSysSettingRepository coreSysSettingRepository;

    @Override
    public Version getVersion() {
        return new Version("2.1");
    }

    @Override
    public void execute() {

        List<CoreMenu> coreMenus = Arrays.asList(
                createCoreMenu(19L, 0L, 2, "template-market", "template-market", 4, null, "/template-market", true, true, false),
                createCoreMenu(30L, 0L, 1, "toolbox", null, 7, "icon_template", "/toolbox", true, true, false),
                createCoreMenu(31L, 30L, 2, "template-setting", "toolbox/template-setting", 1, "icon_template", "/template-setting", false, true, true)
        );
        coreMenuRepository.saveAllAndFlush(coreMenus);

        List<CoreSysSetting> settings = Arrays.asList(
                new CoreSysSetting(1L, "basic.dsIntervalTime", "6", "text", 2),
                new CoreSysSetting(2L, "basic.dsExecuteTime", "minute", "text", 3),
                new CoreSysSetting(7L, "template.url", "https://templates.dataease.cn", "text", 0),
                new CoreSysSetting(8L, "template.accessKey", "dataease", "text", 1)
        );
        coreSysSettingRepository.saveAllAndFlush(settings);

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
