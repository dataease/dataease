package io.dataease.listener.sql;

import io.dataease.copilot.dao.auto.entity.CoreCopilotConfig;
import io.dataease.copilot.dao.auto.entity.CoreCopilotToken;
import io.dataease.copilot.dao.auto.mapper.CoreCopilotConfigRepository;
import io.dataease.copilot.dao.auto.mapper.CoreCopilotTokenRepository;
import io.dataease.initSql.Version;
import io.dataease.startup.dao.auto.entity.CoreSysStartupJob;
import io.dataease.startup.dao.auto.mapper.CoreSysStartupJobRepository;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import io.dataease.visualization.dao.auto.mapper.VisualizationBackgroundRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CoreSqlBlockV2_9 implements CoreSqlBlock {

    @Resource
    private CoreSysStartupJobRepository coreSysStartupJobRepository;
    @Resource
    private CoreCopilotTokenRepository coreCopilotTokenRepository;
    @Resource
    private CoreCopilotConfigRepository coreCopilotConfigRepository;
    @Resource
    private CoreSysSettingRepository coreSysSettingRepository;
    @Resource
    private VisualizationBackgroundRepository visualizationBackgroundRepository;

    @Override
    public Version getVersion() {
        return new Version("2.9");
    }

    @Override
    public void execute() {
        CoreSysStartupJob coreSysStartupJob = new CoreSysStartupJob();
        coreSysStartupJob.setId("chartFilterMerge");
        coreSysStartupJob.setName("chartFilterMerge");
        coreSysStartupJob.setStatus("ready");
        coreSysStartupJobRepository.saveAndFlush(coreSysStartupJob);

        CoreCopilotToken copilotToken1 = new CoreCopilotToken();
        copilotToken1.setId(1L);
        copilotToken1.setType("free");
        copilotToken1.setToken(null);
        copilotToken1.setUpdateTime(null);

        CoreCopilotToken copilotToken2 = new CoreCopilotToken();
        copilotToken2.setId(2L);
        copilotToken2.setType("license");
        copilotToken2.setToken(null);
        copilotToken2.setUpdateTime(null);
        coreCopilotTokenRepository.saveAndFlush(copilotToken1);
        coreCopilotTokenRepository.saveAndFlush(copilotToken2);


        CoreCopilotConfig coreCopilotConfig = new CoreCopilotConfig();
        coreCopilotConfig.setId(1L);
        coreCopilotConfig.setCopilotUrl("https://copilot.dataease.cn");
        coreCopilotConfig.setUsername("xlab");
        coreCopilotConfig.setPwd("Q2Fsb25nQDIwMTU=");
        coreCopilotConfigRepository.saveAndFlush(coreCopilotConfig);


        coreSysSettingRepository.findById(3L).ifPresent(setting -> {
            setting.setPkey("ai.baseUrl");
            setting.setPval("https://maxkb.fit2cloud.com/ui/chat/2ddd8b594ce09dbb?mode=embed");
            setting.setType("text");
            setting.setSort(0);
            coreSysSettingRepository.saveAndFlush(setting);
        });

        CoreSysSetting coreSysSetting = new CoreSysSetting();
        coreSysSetting.setId(10L);
        coreSysSetting.setPkey("basic.exportFileLiveTime");
        coreSysSetting.setPval("30");
        coreSysSetting.setType("text");
        coreSysSetting.setSort(2);
        coreSysSettingRepository.saveAndFlush(coreSysSetting);

        visualizationBackgroundRepository.deleteById("dark_1");
    }

}
