package io.dataease.datasource.type;

import io.dataease.datasource.security.JdbcUrlSecurityPolicy;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Data
@Component("redshift")
public class Redshift extends DatasourceConfiguration {
    private String driver = "com.amazon.redshift.jdbc42.Driver";
    private String extraParams = "";

    public String getJdbc() {
        String jdbcUrl;
        if (StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")) {
            jdbcUrl = getJdbcUrl();
        } else {
            jdbcUrl = "jdbc:redshift://HOSTNAME:PORT/DATABASE"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        }
        return JdbcUrlSecurityPolicy.validate("redshift", getDriver(), jdbcUrl, getExtraParams());
    }

    private static final Pattern DB_NAME_PATTERN = Pattern.compile("//[^/]+/([^?]+)");

    @Override
    protected Pattern getDatabasePattern() {
        return DB_NAME_PATTERN;
    }

    @Override
    protected void convertParameters() {
        Map<String, String> parameters = getParameters();
        if (parameters.containsKey("UID")) {
            setUsername(parameters.get("UID"));
        }
        if (parameters.containsKey("PWD")) {
            setPassword(parameters.get("PWD"));
        }
    }
}
