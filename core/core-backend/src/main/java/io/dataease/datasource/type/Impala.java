package io.dataease.datasource.type;

import io.dataease.datasource.security.JdbcUrlSecurityPolicy;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Data
@Component("impala")
public class Impala extends DatasourceConfiguration {
    private String driver = "com.cloudera.impala.jdbc.Driver";
    private String extraParams = "";
    private List<String> showTableSqls = Arrays.asList("show tables");

    public String getJdbc() {
        if (StringUtils.isNoneEmpty(getUrlType()) && !getUrlType().equalsIgnoreCase("hostName")) {
            return JdbcUrlSecurityPolicy.validate("impala", getDriver(), getJdbcUrl(), getExtraParams());
        }
        String jdbcUrl = "";
        if (StringUtils.isEmpty(extraParams.trim())) {
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
}
