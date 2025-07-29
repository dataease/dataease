package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import io.dataease.menu.dao.auto.mapper.CoreMenuRepository;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class CoreSqlBlockV2_10_5 implements CoreSqlBlock {

    @Resource
    private CoreSysSettingRepository coreSysSettingRepository;

    @Override
    public Version getVersion() {
        return new Version("2.10.5");
    }

    @Override
    public void execute() {
        coreSysSettingRepository.updatePvalByPkey("template.url", "https://cdn0-templates-dataease-cn.fit2cloud.com");

    }

}
