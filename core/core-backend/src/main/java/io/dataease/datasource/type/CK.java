package io.dataease.datasource.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component("ck")
public class CK extends DatasourceConfiguration {
    private String driver = "com.clickhouse.jdbc.ClickHouseDriver";
    private String extraParams = "";
    private String compressAlgorithm = "none"; // 默认设置为none以避免HTTP压缩问题
    private String sslCA;
    private String sslCert;
    private String sslKey;

    @JsonIgnore
    private List<String> ILLEGAL_PARAMETERS = Arrays.asList("jndi:", "rmi:", "ldap:", "ldaps:", "dns:", "nis:", "corba:",
            "java.naming.factory.initial", "java.naming.provider.url");

    public String getJdbc() {
        if (StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")) {
            if (!getJdbcUrl().startsWith("jdbc:clickhouse")) {
                DEException.throwException("Illegal jdbcUrl: " + getJdbcUrl());
            }
            return getJdbcUrl();
        }
        if (StringUtils.isEmpty(extraParams.trim())) {
            return "jdbc:clickhouse://HOSTNAME:PORT/DATABASE"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim());
        } else {
            return "jdbc:clickhouse://HOSTNAME:PORT/DATABASE?EXTRA_PARAMS"
                    .replace("HOSTNAME", getLHost().trim())
                    .replace("PORT", getLPort().toString().trim())
                    .replace("DATABASE", getDataBase().trim())
                    .replace("EXTRA_PARAMS", getExtraParams().trim());
        }
    }
}
