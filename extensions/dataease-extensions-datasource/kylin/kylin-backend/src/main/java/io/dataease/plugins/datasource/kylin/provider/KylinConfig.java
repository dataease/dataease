package io.dataease.plugins.datasource.kylin.provider;

import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KylinConfig extends JdbcConfiguration {

    private String driver = "org.apache.kylin.jdbc.Driver";

    public String getJdbc() {
        return "jdbc:kylin://HOSTNAME:PORT/DATABASE"
                .replace("HOSTNAME", getHost().trim())
                .replace("PORT", getPort().toString().trim())
                .replace("DATABASE", getDataBase().trim());
    }
}
