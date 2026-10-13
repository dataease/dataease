package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import io.dataease.utils.IDUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CoreDataVersion3 implements CoreSqlBlock {

    @Resource
    private CoreSysSettingRepository coreSysSettingRepository;

    @Override
    public Version getVersion() {
        return new Version("2.42");
    }

    @Override
    @Transactional
    public void execute() {
        // 2.41 可能已删除配置；无法恢复原始选择时使用严格校验，保留现存明确值。
        CoreSysSetting setting = coreSysSettingRepository.findByPkey("basic.ignoreSslVerify").orElse(null);
        if (setting == null) {
            coreSysSettingRepository.saveAndFlush(new CoreSysSetting(
                    IDUtils.snowID(), "basic.ignoreSslVerify", "false", "text", 16));
        } else if (StringUtils.isBlank(setting.getPval())) {
            setting.setPval("false");
            coreSysSettingRepository.saveAndFlush(setting);
        }
    }
}
