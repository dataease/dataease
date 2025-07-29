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
public class CoreSqlBlockV2_2 implements CoreSqlBlock {

    @Resource
    private CoreMenuRepository coreMenuRepository;
    @Resource
    private CoreSysSettingRepository coreSysSettingRepository;

    @Override
    public Version getVersion() {
        return new Version("2.2");
    }

    @Override
    public void execute() {


    }

}
