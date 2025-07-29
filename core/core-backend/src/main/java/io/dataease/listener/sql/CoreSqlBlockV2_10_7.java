package io.dataease.listener.sql;

import io.dataease.initSql.Version;
import org.springframework.stereotype.Component;

@Component
public class CoreSqlBlockV2_10_7 implements CoreSqlBlock {

    @Override
    public Version getVersion() {
        return new Version("2.10.1");
    }

    @Override
    public void execute() {


    }

}
