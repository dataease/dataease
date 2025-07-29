package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import io.dataease.menu.dao.auto.mapper.CoreMenuRepository;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import io.dataease.template.dao.auto.mapper.VisualizationTemplateRepository;
import io.dataease.visualization.dao.auto.entity.VisualizationWatermark;
import io.dataease.visualization.dao.auto.mapper.VisualizationWatermarkRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CoreSqlBlockV2_3 implements CoreSqlBlock {

    @Resource
    private VisualizationWatermarkRepository visualizationWatermarkRepository;
    @Resource
    private CoreSysSettingRepository coreSysSettingRepository;
    @Resource
    private VisualizationTemplateRepository visualizationTemplateRepository;

    @Override
    public Version getVersion() {
        return new Version("2.3");
    }

    @Override
    public void execute() {

        String settingContent = "{\"enable\":false,\"enablePanelCustom\":true,\"type\":\"custom\",\"content\":\"水印\",\"watermark_color\":\"#DD1010\",\"watermark_x_space\":12,\"watermark_y_space\":36,\"watermark_fontsize\":15}";
        VisualizationWatermark watermark = new VisualizationWatermark(
                "system_default",
                "1.0",
                settingContent,
                "admin",
                null
        );
        visualizationWatermarkRepository.saveAndFlush(watermark);

        CoreSysSetting sysSetting = new CoreSysSetting();
        sysSetting.setId(9L);
        sysSetting.setPkey("basic.frontTimeOut");
        sysSetting.setPval("60");
        sysSetting.setType("text");
        sysSetting.setSort(1);
        coreSysSettingRepository.saveAndFlush(sysSetting);

        visualizationTemplateRepository.updateUseCountToZero();
    }

}
