package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CoreDataVersion2 implements CoreSqlBlock {

    @Resource
    private CoreSysSettingRepository coreSysSettingRepository;

    @Override
    public Version getVersion() {
        return new Version("2.41");
    }

    @Override
    public void execute() {
        if (coreSysSettingRepository.findByPkey("basic.ignoreSslVerify").isEmpty()) {
            coreSysSettingRepository.saveAndFlush(new CoreSysSetting(
                    1048232869488627722L, "basic.ignoreSslVerify", "false", "text", 16));
        }
    }
}
