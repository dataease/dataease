package io.dataease.dto.datasource;

import io.dataease.plugins.datasource.entity.JdbcConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class RedshiftConfiguration extends JdbcConfiguration {

    private String driver = "com.amazon.redshift.jdbc42.Driver";
    private List<String> illegalParameters = Arrays.asList("socketFactory", "socketFactoryArg", "sslfactory", "sslfactoryarg", "loggerLevel", "loggerFile", "allowUrlInLocalInfile", "allowLoadLocalInfileInPath");


    public String getJdbc() {

        String jdbcUrl = "jdbc:redshift://HOSTNAME:PORT/DATABASE"
                .replace("HOSTNAME", getHost().trim())
                .replace("PORT", getPort().toString().trim())
                .replace("DATABASE", getDataBase().trim());
        for (String illegalParameter : illegalParameters) {
            if (jdbcUrl.toLowerCase().contains(illegalParameter.toLowerCase())) {
                throw new RuntimeException("Illegal parameter: " + illegalParameter);
            }
        }
        return jdbcUrl;
    }
}
