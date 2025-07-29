package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import io.dataease.menu.dao.auto.mapper.CoreMenuRepository;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CoreSqlBlockV2_4 implements CoreSqlBlock {

    @Resource
    private CoreMenuRepository coreMenuRepository;
    @Resource
    private CoreSysSettingRepository coreSysSettingRepository;

    @Override
    public Version getVersion() {
        return new Version("2.4");
    }

    @Override
    public void execute() {


    }

}
