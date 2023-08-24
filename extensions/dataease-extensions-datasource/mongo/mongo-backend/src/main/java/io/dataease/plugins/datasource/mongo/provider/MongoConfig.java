package io.dataease.plugins.datasource.mongo.provider;

import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
public class MongoConfig extends JdbcConfiguration {

    private String driver = "com.mysql.jdbc.Driver";

    private String extraParams;

    public String getJdbc() {
        return "jdbc:mysql://HOSTNAME:PORT/DATABASE?EXTRA_PARAMS"
                .replace("HOSTNAME", getHost().trim())
                .replace("PORT", getPort().toString().trim())
                .replace("DATABASE", getDataBase().trim())
                .replace("EXTRA_PARAMS", getExtraParams().trim());
    }
}
