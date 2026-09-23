package io.dataease.datasource.type;

import io.dataease.datasource.security.JdbcUrlSecurityPolicy;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Data
@Component("impala")
public class Impala extends DatasourceConfiguration {
    private String driver = "com.cloudera.impala.jdbc.Driver";
    private String extraParams = "";

    public String getJdbc() {
        String jdbcUrl = "";
        if (StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")) {
            jdbcUrl = getJdbcUrl();
        } else if (StringUtils.isEmpty(extraParams.trim())) {
            jdbcUrl = "jdbc:impala://HOSTNAME:PORT/DATABASE"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        } else {
            jdbcUrl = "jdbc:impala://HOSTNAME:PORT/DATABASE;EXTRA_PARAMS"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());
        }
        return JdbcUrlSecurityPolicy.validate("impala", getDriver(), jdbcUrl, getExtraParams());
    }

    private static final Pattern DB_NAME_PATTERN = Pattern.compile("//[^/]+/([^?;]+)");

    @Override
    protected Pattern getDatabasePattern() {
        return DB_NAME_PATTERN;
    }

    @Override
    protected void parseParameters(String jdbcUrl) {
        if (jdbcUrl.contains(";")) {
            String[] parts = jdbcUrl.split(";");
            for (int i = 1; i < parts.length; i++) {
                String[] keyValue = parts[i].split("=", 2);
                if (keyValue.length == 2) {
                    getParameters().put(keyValue[0], keyValue[1]);
                }
            }
            if (getParameters().containsKey("AuthMech")) {
                getParameters().put("authentication", getParameters().get("AuthMech"));
            }
        }
    }
}
