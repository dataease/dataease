package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import io.dataease.menu.dao.auto.mapper.CoreMenuRepository;
import io.dataease.startup.dao.auto.entity.CoreSysStartupJob;
import io.dataease.startup.dao.auto.mapper.CoreSysStartupJobRepository;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CoreSqlBlockV2_7 implements CoreSqlBlock {

    @Resource
    private CoreMenuRepository coreMenuRepository;
    @Resource
    private CoreSysStartupJobRepository coreSysStartupJobRepository;

    @Override
    public Version getVersion() {
        return new Version("2.7");
    }

    @Override
    public void execute() {
        CoreSysStartupJob coreSysStartupJob = new CoreSysStartupJob();
        coreSysStartupJob.setId("chartFilterMerge");
        coreSysStartupJob.setName("chartFilterMerge");
        coreSysStartupJob.setStatus("ready");
        coreSysStartupJobRepository.saveAndFlush(coreSysStartupJob);

    }

}
