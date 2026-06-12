package io.dataease.listener.sql;

import io.dataease.initSql.UpgradeSqlBlock;
import io.dataease.initSql.Version;
import io.dataease.menu.dao.auto.mapper.CoreMenuRepository;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class CoreUpgrade implements UpgradeSqlBlock {

    @Resource
    private CoreDataInit coreDataInit;
    @Resource
    private CoreMenuRepository coreMenuRepository;
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public Version getVersion() {
        return new Version("upgrade.0");
    }

    @Override
    public void execute() {
        // coreDataInit.execute();
        fixCoreMenu();
        fixSchema();
    }

    private void fixCoreMenu() {
        coreMenuRepository.findById(15L).ifPresent(menu -> {
            menu.setAuth(true);
            coreMenuRepository.save(menu);
        });
        coreMenuRepository.findById(16L).ifPresent(menu -> {
            menu.setMenuSort(4);
            coreMenuRepository.save(menu);
        });
    }

    private void fixSchema() {
        jdbcTemplate.execute("ALTER TABLE xpack_log MODIFY COLUMN oid bigint NULL COMMENT '组织ID'");
    }

}
