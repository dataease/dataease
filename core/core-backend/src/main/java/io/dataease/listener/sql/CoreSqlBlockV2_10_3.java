package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import io.dataease.menu.dao.auto.entity.CoreMenu;
import io.dataease.menu.dao.auto.mapper.CoreMenuRepository;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import io.dataease.visualization.dao.auto.mapper.VisualizationBackgroundRepository;
import io.dataease.visualization.dao.auto.mapper.VisualizationLinkJumpTargetViewInfoRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CoreSqlBlockV2_10_3 implements CoreSqlBlock {

    @Resource
    private CoreMenuRepository coreMenuRepository;
    @Resource
    private CoreSysSettingRepository coreSysSettingRepository;
    @Resource
    private VisualizationLinkJumpTargetViewInfoRepository visualizationLinkJumpTargetViewInfoRepository;
    @Resource
    private DataVisualizationInfoRepository dataVisualizationInfoRepository;
    @Resource
    private VisualizationBackgroundRepository visualizationBackgroundRepository;

    @Override
    public Version getVersion() {
        return new Version("2.10.3");
    }

    @Override
    public void execute() {
        CoreSysSetting coreSysSetting1 = new CoreSysSetting();
        coreSysSetting1.setId(1048232869488627719L);
        coreSysSetting1.setPkey("basic.defaultSort");
        coreSysSetting1.setPval("1");
        coreSysSetting1.setType("text");
        coreSysSetting1.setSort(13);
        CoreSysSetting coreSysSetting2 = new CoreSysSetting();
        coreSysSetting2.setId(1048232869488627720L);
        coreSysSetting2.setPkey("basic.defaultOpen");
        coreSysSetting2.setPval("false");
        coreSysSetting2.setType("text");
        coreSysSetting2.setSort(124);
        coreSysSettingRepository.saveAndFlush(coreSysSetting1);
        coreSysSettingRepository.saveAndFlush(coreSysSetting2);

        CoreMenu coreMenu = new CoreMenu();
        coreMenu.setId(70L);
        coreMenu.setPid(0L);
        coreMenu.setType(1);
        coreMenu.setName("msg");
        coreMenu.setComponent(null);
        coreMenu.setMenuSort(200);
        coreMenu.setIcon(null);
        coreMenu.setPath("/msg");
        coreMenu.setHidden(true);
        coreMenu.setInLayout(true);
        coreMenu.setAuth(false);
        coreMenuRepository.saveAndFlush(coreMenu);

        visualizationLinkJumpTargetViewInfoRepository.updateTargetType("view");
        dataVisualizationInfoRepository.updateCheckVersion("1");

        for(int i = 1; i <= 9; i++) {
            visualizationBackgroundRepository.updateNameById("board_" + i, "Board" + i);
        }

        coreSysSettingRepository.updateByPkey("basic.dsIntervalTime", 11);
    }

}
