package io.dataease.dto.datasource;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MongodbConfiguration extends JdbcConfiguration {

    private String driver = "mongodb.jdbc.MongoDriver";
    private String connectionType;

    public String getJdbc() {
        return "jdbc:mongodb://HOSTNAME:PORT/DATABASE"
                .replace("HOSTNAME", getHost().trim())
                .replace("PORT", getPort().toString().trim())
                .replace("DATABASE", getDataBase().trim());
    }
}
