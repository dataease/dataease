package io.dataease.datasource.type;

import io.dataease.datasource.security.JdbcUrlSecurityPolicy;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Data
@Component("sqlServer")
public class Sqlserver extends DatasourceConfiguration {
    private String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private String extraParams = "";

    public String getJdbc() {
        String jdbcUrl = "";
        if (StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")) {
            jdbcUrl = getJdbcUrl();
        } else if (StringUtils.isEmpty(extraParams.trim())) {
            jdbcUrl = "jdbc:sqlserver://HOSTNAME:PORT;DatabaseName=DATABASE"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        } else {
            jdbcUrl = "jdbc:sqlserver://HOSTNAME:PORT;DatabaseName=DATABASE;EXTRA_PARAMS"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());
        }
        return JdbcUrlSecurityPolicy.validate("sqlServer", getDriver(), jdbcUrl, getExtraParams());
    }

    private static final Pattern DB_NAME_PATTERN = Pattern.compile(";databaseName=([^;]+)");

    @Override
    protected Pattern getDatabasePattern() {
        return DB_NAME_PATTERN;
    }
}
