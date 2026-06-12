package io.dataease.initSql;

public interface UpgradeSqlBlock extends SqlBlock {
    @Override
    default String getVersionGroup() {
        return "upgrade";
    }
}
