package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import io.dataease.menu.dao.auto.mapper.CoreMenuRepository;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CoreSqlBlockV2_5 implements CoreSqlBlock {

    @Resource
    private DataVisualizationInfoRepository dataVisualizationInfoRepository;
    @Resource
    private CoreSysSettingRepository coreSysSettingRepository;

    @Override
    public Version getVersion() {
        return new Version("2.5");
    }

    @Override
    public void execute() {
        CoreSysSetting sysSetting = new CoreSysSetting();
        sysSetting.setId(3L);
        sysSetting.setPkey("ai.baseUrl");
        sysSetting.setPval("https://maxkb.fit2cloud.com/ui/chat/2ddd8b594ce09dbb");
        sysSetting.setType("text");
        sysSetting.setSort(0);
        coreSysSettingRepository.saveAndFlush(sysSetting);
        dataVisualizationInfoRepository.updateMobileLayout();

    }

}
