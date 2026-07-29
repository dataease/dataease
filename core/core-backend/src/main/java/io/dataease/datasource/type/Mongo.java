package io.dataease.datasource.type;

import io.dataease.datasource.security.JdbcUrlSecurityPolicy;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Data
@Component("mongo")
public class Mongo extends DatasourceConfiguration {
    private String driver = "org.mariadb.jdbc.Driver";
    private String extraParams = "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true&zeroDateTimeBehavior=convertToNull";

    public String getJdbc() {
        String jdbcUrl = "";
        if (StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")) {
            if (getJdbcUrl().trim().startsWith("jdbc:mariadb://")) {
                jdbcUrl = getJdbcUrl();
            } else {
                jdbcUrl = getJdbcUrl().contains("?") ? getJdbcUrl() + "&permitMysqlScheme=true" : getJdbcUrl() + "?permitMysqlScheme=true";
            }
        } else if (StringUtils.isEmpty(extraParams.trim())) {
            jdbcUrl = "jdbc:mariadb://HOSTNAME:PORT/DATABASE"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        } else {
            jdbcUrl = "jdbc:mariadb://HOSTNAME:PORT/DATABASE?EXTRA_PARAMS"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());
        }
        return JdbcUrlSecurityPolicy.validate("mongo", getDriver(), jdbcUrl, getExtraParams());
    }
}
