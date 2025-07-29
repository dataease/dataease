package io.dataease.listener.sql;

import io.dataease.initSql.SqlBlock;

public interface CoreSqlBlock extends SqlBlock {
    @Override
    default String getVersionGroup() {
        return "2";
    }
}
