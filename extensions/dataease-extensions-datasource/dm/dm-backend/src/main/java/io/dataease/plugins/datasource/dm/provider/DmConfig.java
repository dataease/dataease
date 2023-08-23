package io.dataease.plugins.datasource.dm.provider;

import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DmConfig extends JdbcConfiguration {

    private String driver = "dm.jdbc.driver.DmDriver";
    private String extraParams;


    public String getJdbc() {
        return "jdbc:dm://HOST:PORT/DATABASE"
                .replace("HOST", getHost().trim())
                .replace("PORT", getPort().toString())
                .replace("DATABASE", getDataBase().trim());
    }
}
