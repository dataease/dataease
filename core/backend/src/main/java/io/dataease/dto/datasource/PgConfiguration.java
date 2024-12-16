package io.dataease.dto.datasource;

import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class PgConfiguration extends JdbcConfiguration {

    private String driver = "org.postgresql.Driver";
    private String extraParams = "";
    private List<String> illegalParameters = Arrays.asList("socketFactory", "socketFactoryArg", "sslfactory", "sslfactoryarg", "loggerLevel", "loggerFile", "allowUrlInLocalInfile", "allowLoadLocalInfileInPath");


    public String getJdbc() {
        String jdbcUrl = "";
        if (StringUtils.isEmpty(extraParams.trim())) {
            if (StringUtils.isEmpty(getSchema())) {
                jdbcUrl = "jdbc:postgresql://HOSTNAME:PORT/DATABASE"
                        .replace("HOSTNAME", getHost().trim())
                        .replace("PORT", getPort().toString().trim())
                        .replace("DATABASE", getDataBase().trim());
            } else {
                jdbcUrl = "jdbc:postgresql://HOSTNAME:PORT/DATABASE?currentSchema=SCHEMA"
                        .replace("HOSTNAME", getHost().trim())
                        .replace("PORT", getPort().toString().trim())
                        .replace("DATABASE", getDataBase().trim())
                        .replace("SCHEMA", getSchema().trim());
            }
        } else {
            jdbcUrl = "jdbc:postgresql://HOSTNAME:PORT/DATABASE?EXTRA_PARAMS"
                    .replace("HOSTNAME", getHost().trim())
                    .replace("PORT", getPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());
        }

        for (String illegalParameter : illegalParameters) {
            if (jdbcUrl.toLowerCase().contains(illegalParameter.toLowerCase()) || URLDecoder.decode(jdbcUrl).contains(illegalParameter.toLowerCase())) {
                throw new RuntimeException("Illegal parameter: " + illegalParameter);
            }
        }
        return jdbcUrl;
    }
}
