package io.dataease.datasource.type;

import io.dataease.datasource.security.JdbcUrlSecurityPolicy;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Data
@Component("StarRocks")
public class StarRocks extends Mysql {
    private String driver = "org.mariadb.jdbc.Driver";
    private String extraParams = "characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true";
    private Integer fePort = 8030;
    private String beIp = "";
    private Integer bePort = 8040;

    @Override
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
        return JdbcUrlSecurityPolicy.validate("starrocks", getDriver(), jdbcUrl, getExtraParams());
    }
}
