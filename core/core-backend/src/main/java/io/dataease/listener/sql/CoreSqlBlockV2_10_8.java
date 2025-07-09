package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import io.dataease.startup.dao.auto.entity.CoreSysStartupJob;
import io.dataease.startup.dao.auto.mapper.CoreSysStartupJobRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CoreSqlBlockV2_10_8 implements CoreSqlBlock {


    @Resource
    private CoreSysStartupJobRepository coreSysStartupJobRepository;

    @Override
    public Version getVersion() {
        return new Version("2.10.8");
    }

    @Override
    public void execute() {
        CoreSysStartupJob coreSysStartupJob = new CoreSysStartupJob();
        coreSysStartupJob.setId("datasetCrossListener");
        coreSysStartupJob.setName("datasetCrossListener");
        coreSysStartupJob.setStatus("ready");
        coreSysStartupJobRepository.saveAndFlush(coreSysStartupJob);

    }

}
