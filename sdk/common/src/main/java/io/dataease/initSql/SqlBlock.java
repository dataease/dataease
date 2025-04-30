package io.dataease.initSql;

public interface SqlBlock {
    Version getVersion();
    void execute();
    String getVersionGroup();
}
