package io.dataease.datasource.type;

import io.dataease.datasource.security.JdbcUrlSecurityPolicy;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component("sqlServer")
public class Sqlserver extends DatasourceConfiguration {
    private String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private String extraParams = "";
    private List<String> showTableSqls = Arrays.asList("show tables");

    public String getJdbc() {
        String jdbcUrl;
        if (StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")) {
            jdbcUrl = getJdbcUrl();
        } else {
            jdbcUrl = "";
            if (StringUtils.isEmpty(extraParams.trim())) {
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
            if (StringUtils.isNotEmpty(getSchema()) && !getExtraParams().contains("currentSchema")) {
                jdbcUrl += ";currentSchema=\"" + getSchema().trim() + "\"";
            }
        }
        return JdbcUrlSecurityPolicy.validate("sqlserver", getDriver(), jdbcUrl, getExtraParams());
    }
}
