package io.dataease.datasource.type;

import io.dataease.datasource.security.JdbcUrlSecurityPolicy;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Data
@Component("oracle")
public class Oracle extends DatasourceConfiguration {
    private String driver = "oracle.jdbc.driver.OracleDriver";
    private String extraParams = "";

    /**
     * 仅允许 Oracle thin 的 host:port 形式（SID 或 service name），
     * 拒绝 ldap 等目录服务描述符及 TNS 描述符。
     */
    private static final Pattern ORACLE_THIN_URL_PATTERN = Pattern.compile(
            "(?i)^jdbc:oracle:thin:@(?://)?(?:\\[[0-9A-Fa-f:]+\\]|[A-Za-z0-9._~-]+):\\d+(?:[/:][A-Za-z0-9._$#-]+)(?:[?;].*)?$");

    public String getJdbc() {
        if (StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")) {
            return validateOracleJdbcUrl(getJdbcUrl());
        }
        if (StringUtils.isNotEmpty(getConnectionType()) && getConnectionType().equalsIgnoreCase("serviceName")) {
            if (StringUtils.isEmpty(extraParams.trim())) {
                return validateOracleJdbcUrl("jdbc:oracle:thin:@HOSTNAME:PORT/DATABASE"
                        .replace("HOSTNAME", getLHost().trim())
                        .replace("PORT", getLPort().toString().trim())
                        .replace("DATABASE", getDataBase().trim()));
            } else {
                return validateOracleJdbcUrl("jdbc:oracle:thin:@HOSTNAME:PORT/DATABASE?EXTRA_PARAMS"
                        .replace("HOSTNAME", getLHost().trim())
                        .replace("PORT", getLPort().toString().trim())
                        .replace("DATABASE", getDataBase().trim())
                        .replace("EXTRA_PARAMS", getExtraParams().trim()));
            }
        } else {
            if (StringUtils.isEmpty(extraParams.trim())) {
                return validateOracleJdbcUrl("jdbc:oracle:thin:@HOSTNAME:PORT:DATABASE"
                        .replace("HOSTNAME", getLHost().trim())
                        .replace("PORT", getLPort().toString().trim())
                        .replace("DATABASE", getDataBase().trim()));
            } else {
                return validateOracleJdbcUrl("jdbc:oracle:thin:@HOSTNAME:PORT:DATABASE?EXTRA_PARAMS"
                        .replace("HOSTNAME", getLHost().trim())
                        .replace("PORT", getLPort().toString().trim())
                        .replace("DATABASE", getDataBase().trim())
                        .replace("EXTRA_PARAMS", getExtraParams().trim()));
            }
        }
    }

    private String validateOracleJdbcUrl(String jdbcUrl) {
        JdbcUrlSecurityPolicy.validate("oracle", getDriver(), jdbcUrl, getExtraParams());
        if (!ORACLE_THIN_URL_PATTERN.matcher(jdbcUrl).matches()) {
            DEException.throwException("Illegal jdbcUrl: " + jdbcUrl);
        }
        return jdbcUrl;
    }
}
