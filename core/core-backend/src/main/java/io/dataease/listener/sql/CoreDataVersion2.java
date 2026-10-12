package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
        Optional<CoreSysSetting> legacy = coreSysSettingRepository.findByPkey("basic.ignoreSslVerify");
        if (coreSysSettingRepository.findByPkey("basic.ignoreSslVerify").isEmpty()) {
            String pval = legacy
                    .map(item -> "true".equalsIgnoreCase(item.getPval()) ? "false" : "true")
                    .orElse("true");
            coreSysSettingRepository.saveAndFlush(new CoreSysSetting(
                    1048232869488627722L, "basic.ignoreSslVerify", pval, "text", 16));
        }
        legacy.ifPresent(coreSysSettingRepository::delete);
    }
}
